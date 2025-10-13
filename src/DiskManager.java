import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Gestionnaire de disque minimal
 * Stocke les pages dans BinData/Datax.bin et maintient une bitmap simple en mémoire
 */
public class DiskManager {
    private final DBConfig config;
    private final Path bindataDir;
    private final Map<Integer, BitSet> fileBitmaps = new HashMap<>();
    private final Map<Integer, RandomAccessFile> openFiles = new HashMap<>();
    private final int maxPagesPerFile;

    public DiskManager(DBConfig config) {
        this.config = config;
        this.bindataDir = Paths.get(config.getDbpath()).resolve("BinData");
        // Allow test override of per-file max pages via system property (helps tests force multi-file growth)
        int mp = Integer.MAX_VALUE;
        String s = System.getProperty("dm.maxpagesperfile");
        if (s != null) {
            try { mp = Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        this.maxPagesPerFile = mp;
    }

    /**
     * Initialisation : créer dossier BinData si nécessaire, ouvrir fichiers existants et charger bitmaps
     */
    public void Init() throws IOException {
        // Validation basique
        if (config.getPagesize() <= 0) throw new IOException("pagesize invalide dans DBConfig");

        if (!Files.exists(bindataDir)) {
            Files.createDirectories(bindataDir);
        }
        // Also create lowercase folder 'bindata' at project level if requested (some tests/tools expect it)
        Path lower = Paths.get(config.getDbpath()).resolve("bindata");
        if (!Files.exists(lower)) {
            try { Files.createDirectories(lower); } catch (Exception ignored) {}
        }

        // Charger les fichiers existants Data0.bin ... Data{n}.bin jusqu'à dm_maxfilecount
        for (int i = 0; i < config.getDm_maxfilecount(); i++) {
            Path p = bindataDir.resolve("Data" + i + ".bin");
            if (Files.exists(p)) {
                long length = Files.size(p);
                if (length % config.getPagesize() != 0) {
                    throw new IOException("Fichier " + p + " corrompu : taille non multiple de pagesize");
                }

                RandomAccessFile raf = new RandomAccessFile(p.toFile(), "rw");
                openFiles.put(i, raf);
                int pages = (int) (length / config.getPagesize());

                // Try to load existing bitmap metadata if present
                Path meta = bindataDir.resolve("Data" + i + ".meta");
                if (Files.exists(meta)) {
                    byte[] data = Files.readAllBytes(meta);
                    BitSet bs = BitSet.valueOf(data);
                    // If bitmap shorter than pages, expand and mark remaining pages used
                    if (bs.length() < pages) {
                        for (int j = bs.length(); j < pages; j++) bs.set(j);
                    }
                    fileBitmaps.put(i, bs);
                } else {
                    // No meta: assume all existing pages are used
                    BitSet bs = new BitSet(pages);
                    for (int j = 0; j < pages; j++) bs.set(j);
                    fileBitmaps.put(i, bs);
                }
            }
        }
    }

    /**
     * Finish : fermer fichiers et sauvegarder bitmaps si nécessaire
     */
    public void Finish() throws IOException {
        // Persist bitmaps per file then close files
        for (Map.Entry<Integer, RandomAccessFile> e : openFiles.entrySet()) {
            int idx = e.getKey();
            RandomAccessFile raf = e.getValue();
            // flush
            raf.getChannel().force(true);
            // persist bitmap
            BitSet bs = fileBitmaps.get(idx);
            Path meta = bindataDir.resolve("Data" + idx + ".meta");
            if (bs != null) {
                byte[] data = bs.toByteArray();
                Files.write(meta, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            raf.close();
        }
        openFiles.clear();
        fileBitmaps.clear();
    }

    /**
     * Alloue une page : réutilise une page libre si disponible sinon ajoute une page dans un fichier
     */
    public PageId AllocPage() throws IOException {
        // 1) Réutiliser une page désallouée existante (parcourir fichiers existants)
        for (int i = 0; i < config.getDm_maxfilecount(); i++) {
            Path p = bindataDir.resolve("Data" + i + ".bin");
            if (!Files.exists(p)) continue; // pas de fichier -> pas de pages libérées

            RandomAccessFile raf = openFiles.get(i);
            if (raf == null) {
                // ouvrir en lecture/écriture pour connaître la longueur
                raf = new RandomAccessFile(p.toFile(), "rw");
                openFiles.put(i, raf);
            }

            long length = raf.length();
                int pages = (int) (length / config.getPagesize());
            if (pages == 0) continue;

            BitSet bs = fileBitmaps.getOrDefault(i, new BitSet(pages));
            int free = bs.nextClearBit(0);
            if (free >= 0 && free < pages) {
                bs.set(free);
                fileBitmaps.put(i, bs);
                return new PageId(i, free);
            }
        }

        // 2) Aucun slot libre trouvé : ajouter une page (append) dans le premier fichier disponible
        for (int i = 0; i < config.getDm_maxfilecount(); i++) {
            ensureOpen(i); // crée le fichier si besoin
            RandomAccessFile raf = openFiles.get(i);
            if (raf == null) continue;
            long length = raf.length();
            int pages = (int) (length / config.getPagesize());

            // skip file if it already reached per-file max
            if (pages >= maxPagesPerFile) continue;

            // étendre le fichier d'une page
            raf.setLength(length + config.getPagesize());

            BitSet bs = fileBitmaps.getOrDefault(i, new BitSet());
            bs.set(pages);
            fileBitmaps.put(i, bs);
            return new PageId(i, pages);
        }

        throw new IOException("Nombre maximal de fichiers atteint");
    }

    private void ensureOpen(int fileIdx) throws IOException {
        if (openFiles.containsKey(fileIdx)) return;
        // Ensure BinData directory exists
        if (!Files.exists(bindataDir)) {
            Files.createDirectories(bindataDir);
        }
        Path p = bindataDir.resolve("Data" + fileIdx + ".bin");
        RandomAccessFile raf = new RandomAccessFile(p.toFile(), "rw");
        openFiles.put(fileIdx, raf);
        if (!fileBitmaps.containsKey(fileIdx)) fileBitmaps.put(fileIdx, new BitSet());
    }

    /**
     * Lit une page dans le buffer fourni
     */
    public void ReadPage(PageId pid, byte[] buff) throws IOException {
        if (buff.length < config.getPagesize()) throw new IllegalArgumentException("Buffer trop petit");
        ensureOpen(pid.getFileIdx());
        RandomAccessFile raf = openFiles.get(pid.getFileIdx());
        long offset = (long) pid.getPageIdx() * config.getPagesize();
        raf.seek(offset);
        int read = raf.read(buff, 0, config.getPagesize());
        if (read < config.getPagesize()) {
            // remplir le reste par zéro
            Arrays.fill(buff, read, config.getPagesize(), (byte)0);
        }
    }

    /**
     * Écrit une page depuis le buffer fourni
     */
    public void WritePage(PageId pid, byte[] buff) throws IOException {
        if (buff.length < config.getPagesize()) throw new IllegalArgumentException("Buffer trop petit");
        ensureOpen(pid.getFileIdx());
        RandomAccessFile raf = openFiles.get(pid.getFileIdx());
        long offset = (long) pid.getPageIdx() * config.getPagesize();
        raf.seek(offset);
        raf.write(buff, 0, config.getPagesize());
        // marquer comme utilisée
        BitSet bs = fileBitmaps.get(pid.getFileIdx());
        if (bs == null) {
            bs = new BitSet();
            fileBitmaps.put(pid.getFileIdx(), bs);
        }
        bs.set(pid.getPageIdx());
    }

    /**
     * Désalloue une page (marque libre)
     */
    public void DeallocPage(PageId pid) {
        BitSet bs = fileBitmaps.get(pid.getFileIdx());
        if (bs != null) bs.clear(pid.getPageIdx());
    }

    /**
     * Retourne le nombre actuel de pages stockées dans le fichier Data{fileIdx}.bin
     */
    public int getPageCount(int fileIdx) throws IOException {
        Path p = bindataDir.resolve("Data" + fileIdx + ".bin");
        if (!Files.exists(p)) return 0;
        long len = Files.size(p);
        return (int) (len / config.getPagesize());
    }

    /**
     * Retourne le nombre maximal de pages théorique par fichier en tenant compte
     * de la taille d'un int (utilisé pour PageId.pageIdx) et de pagesize.
     * En pratique la limite réelle peut être plus basse (limites FS, mémoire...)
     */
    public int getMaxPagesPerFile() {
        // Page index is stored in an int, so we cannot exceed Integer.MAX_VALUE pages
        return Integer.MAX_VALUE;
    }

    /**
     * Retourne le nombre maximal total de pages gérable par le DiskManager
     * (dm_maxfilecount * maxPagesPerFile). Le résultat est retourné en long
     * pour éviter overflow.
     */
    public long getTotalMaxPages() {
        return ((long) config.getDm_maxfilecount()) * ((long) getMaxPagesPerFile());
    }
}

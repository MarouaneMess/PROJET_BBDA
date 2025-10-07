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

    public DiskManager(DBConfig config) {
        this.config = config;
        this.bindataDir = Paths.get(config.getDbpath()).resolve("BinData");
    }

    /**
     * Initialisation : créer dossier BinData si nécessaire, ouvrir fichiers existants et charger bitmaps
     */
    public void Init() throws IOException {
        if (!Files.exists(bindataDir)) {
            Files.createDirectories(bindataDir);
        }

        // Charger les fichiers existants Data0.bin ... Data{n}.bin jusqu'à dm_maxfilecount
        for (int i = 0; i < config.getDm_maxfilecount(); i++) {
            Path p = bindataDir.resolve("Data" + i + ".bin");
            if (Files.exists(p)) {
                RandomAccessFile raf = new RandomAccessFile(p.toFile(), "rw");
                openFiles.put(i, raf);
                // compute number of pages
                long length = raf.length();
                int pages = (int) (length / config.getPagesize());
                BitSet bs = new BitSet(pages);
                // mark all existing pages as used
                for (int j = 0; j < pages; j++) bs.set(j);
                fileBitmaps.put(i, bs);
            }
        }
    }

    /**
     * Finish : fermer fichiers et sauvegarder bitmaps si nécessaire
     */
    public void Finish() throws IOException {
        for (RandomAccessFile raf : openFiles.values()) {
            raf.getChannel().force(true);
            raf.close();
        }
        openFiles.clear();
        fileBitmaps.clear();
    }

    /**
     * Alloue une page : réutilise une page libre si disponible sinon ajoute une page dans un fichier
     */
    public PageId AllocPage() throws IOException {
        // Chercher une page libre
        for (int fileIdx : fileBitmaps.keySet()) {
            BitSet bs = fileBitmaps.get(fileIdx);
            int free = bs.nextClearBit(0);
            if (free >= 0 && free < Integer.MAX_VALUE) {
                bs.set(free);
                ensureOpen(fileIdx);
                return new PageId(fileIdx, free);
            }
        }

        // Sinon, créer/étendre un fichier
        for (int i = 0; i < config.getDm_maxfilecount(); i++) {
            ensureOpen(i);
            RandomAccessFile raf = openFiles.get(i);
            if (raf == null) continue;
            long length = raf.length();
            int pages = (int) (length / config.getPagesize());
            // if file has space (always true), append one page
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
}

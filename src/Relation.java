import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Relation models a table schema and provides methods to serialize/deserialize
 * Record values to/from a ByteBuffer using a fixed-size layout per row.
 * Conventions:
 * - INT: 4 bytes (ByteBuffer.putInt/getInt)
 * - FLOAT: 4 bytes (ByteBuffer.putFloat/getFloat)
 * - CHAR(T): exactly T chars, each stored via putChar (2 bytes). Values are padded with '\0' if shorter; truncated if longer.
 * - VARCHAR(T): 4-byte length prefix L (0..T), followed by T-char slot; first L chars meaningful; remaining padded with '\0'.
 */
public class Relation {
    private final String name;
    private final List<ColumnInfo> columns;

    // --- AJOUTS TP5 ---
    private PageId headerPageId;           // Identifiant de la Header Page pour cette relation
    private int nbSlotsPerDataPage;        // Nombre de slots sur chaque page de données
    private DiskManager diskManager;       // Référence vers DiskManager
    private BufferManager bufferManager;   // Référence vers BufferManager

    // Constructeur enrichi
    public Relation(String name, List<ColumnInfo> columns, 
                    PageId headerPageId, int nbSlotsPerDataPage,
                    DiskManager diskManager, BufferManager bufferManager) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("relation name required");
        if (columns == null || columns.isEmpty()) throw new IllegalArgumentException("at least one column required");
        this.name = name;
        this.columns = new ArrayList<>(columns);
        // Ajouts TP5 :
        this.headerPageId = headerPageId;
        this.nbSlotsPerDataPage = nbSlotsPerDataPage;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
    }

    // Constructeur d'origine conservé si besoin
    public Relation(String name, List<ColumnInfo> columns) {
        this(name, columns, null, 0, null, null);
    }

    // --- GETTERS/SETTERS TP5 ---
    public PageId getHeaderPageId() { return headerPageId; }
    public void setHeaderPageId(PageId pid) { this.headerPageId = pid; }
    public int getNbSlotsPerDataPage() { return nbSlotsPerDataPage; }
    public void setNbSlotsPerDataPage(int n) { this.nbSlotsPerDataPage = n; }
    public DiskManager getDiskManager() { return diskManager; }
    public void setDiskManager(DiskManager dm) { this.diskManager = dm; }
    public BufferManager getBufferManager() { return bufferManager; }
    public void setBufferManager(BufferManager bm) { this.bufferManager = bm; }

    // --- FONCTIONNALITES d'origine ---
    public String getName() { return name; }
    public int getColumnCount() { return columns.size(); }
    public List<ColumnInfo> getColumns() { return new ArrayList<>(columns); }

    public int getFixedRecordSizeBytes() {
        int total = 0;
        for (ColumnInfo c : columns) total += c.fixedSizeBytes();
        return total;
    }

    private static int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    private static float parseFloat(String s) {
        return Float.parseFloat(s.trim());
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public void writeRecordToBuffer(Record record, ByteBuffer buff, int pos) {
        if (record == null || buff == null) throw new IllegalArgumentException("record/buffer required");
        if (record.size() != columns.size()) throw new IllegalArgumentException("record arity mismatch: got " + record.size() + ", expected " + columns.size());
        int base = pos;
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo ci = columns.get(i);
            String v = record.get(i);
            switch (ci.type) {
                case INT: {
                    int ival = parseInt(v);
                    buff.putInt(base, ival);
                    base += 4;
                    break;
                }
                case FLOAT: {
                    float fval = parseFloat(v);
                    buff.putFloat(base, fval);
                    base += 4;
                    break;
                }
                case CHAR: {
                    String s = safe(v);
                    // write exactly T chars via putChar
                    for (int k = 0; k < ci.lengthT; k++) {
                        char ch = (k < s.length()) ? s.charAt(k) : '\0';
                        buff.putChar(base, ch);
                        base += 2;
                    }
                    break;
                }
                case VARCHAR: {
                    String s = safe(v);
                    int L = Math.min(s.length(), ci.lengthT);
                    buff.putInt(base, L);
                    base += 4;
                    // write up to T chars, pad remainder
                    for (int k = 0; k < ci.lengthT; k++) {
                        char ch = (k < L) ? s.charAt(k) : '\0';
                        buff.putChar(base, ch);
                        base += 2;
                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown column type: " + ci.type);
            }
        }
    }

    public void readFromBuffer(Record record, ByteBuffer buff, int pos) {
        if (record == null || buff == null) throw new IllegalArgumentException("record/buffer required");
        record.clear();
        int base = pos;
        for (ColumnInfo ci : columns) {
            switch (ci.type) {
                case INT: {
                    int ival = buff.getInt(base);
                    base += 4;
                    record.add(Integer.toString(ival));
                    break;
                }
                case FLOAT: {
                    float fval = buff.getFloat(base);
                    base += 4;
                    record.add(Float.toString(fval));
                    break;
                }
                case CHAR: {
                    StringBuilder sb = new StringBuilder(ci.lengthT);
                    for (int k = 0; k < ci.lengthT; k++) {
                        char ch = buff.getChar(base);
                        base += 2;
                        if (ch != '\0') sb.append(ch);
                    }
                    record.add(sb.toString());
                    break;
                }
                case VARCHAR: {
                    int L = buff.getInt(base);
                    base += 4;
                    L = Math.max(0, Math.min(L, ci.lengthT));
                    StringBuilder sb = new StringBuilder(L);
                    for (int k = 0; k < ci.lengthT; k++) {
                        char ch = buff.getChar(base);
                        base += 2;
                        if (k < L) sb.append(ch);
                    }
                    record.add(sb.toString());
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown column type: " + ci.type);
            }
        }
    }
    public void addDataPage() {
    try {
        // 1. Alloue une nouvelle page via le DiskManager
        PageId newPageId = diskManager.AllocPage();

        // 2. Initialise la nouvelle page (remplit la bytemap à zéro)
        byte[] dataBuffer = bufferManager.GetPage(newPageId);
        // La bytemap est sur les nbSlotsPerDataPage premiers octets
        for (int i = 0; i < nbSlotsPerDataPage; i++) dataBuffer[i] = 0;

        // 3. La page pointe vers rien dans la liste chainée au début (valeurs fictives pour next/prev page)
        int base = nbSlotsPerDataPage;
        // Ici, on réserve 2 PageId (next/prev) stockés sur 2*8 octets (chaque int=4, donc 2 entiers par PageId)
        // Pour le chaînage de la liste "pages ayant de la place" : on met (-1,-1), de même pour "pleines"
        for (int i = 0; i < 4; i++) { // nextPageId (fileIdx=-1, pageIdx=-1)
            dataBuffer[base++] = (byte)0xFF;
        }
        for (int i = 0; i < 4; i++) { // prevPageId (fileIdx=-1, pageIdx=-1)
            dataBuffer[base++] = (byte)0xFF;
        }

        // 4. Libère la page, marque dirty
        bufferManager.FreePage(newPageId, true);

        // 5. Met à jour la liste des pages "ayant encore de la place" dans la Header Page
        // Accède à la Header Page via BufferManager
        byte[] headerBuffer = bufferManager.GetPage(headerPageId);

        // Extrait PageId courant de la première page "ayant de la place"
        int offset = 2 * 4; // Le premier PageId (pleines, 0-7), le deuxième est (avec place, 8-15)
        int freeListFileIdx = ByteBuffer.wrap(headerBuffer, offset, 4).getInt();
        int freeListPageIdx = ByteBuffer.wrap(headerBuffer, offset+4, 4).getInt();

        // Si la liste était vide, pointe vers la nouvelle page
        if (freeListFileIdx == -1 && freeListPageIdx == -1) {
            ByteBuffer.wrap(headerBuffer, offset, 4).putInt(newPageId.getFileIdx());
            ByteBuffer.wrap(headerBuffer, offset+4, 4).putInt(newPageId.getPageIdx());
        } else {
            // Sinon, chaîner la nouvelle page en tête de liste
            // NouvellePage.next = ancien_tete ; HeaderPage = nouvellePage

            // 1. nouvellePage.next = ancien_tete
            byte[] newPageBuf = bufferManager.GetPage(newPageId);
            ByteBuffer.wrap(newPageBuf, nbSlotsPerDataPage, 4).putInt(freeListFileIdx);     // next.fileIdx
            ByteBuffer.wrap(newPageBuf, nbSlotsPerDataPage + 4, 4).putInt(freeListPageIdx); // next.pageIdx

            // 2. HeaderPage = nouvellePage (on écrit le nouveau head dans la header)
            ByteBuffer.wrap(headerBuffer, offset, 4).putInt(newPageId.getFileIdx());
            ByteBuffer.wrap(headerBuffer, offset+4, 4).putInt(newPageId.getPageIdx());
            bufferManager.FreePage(newPageId, true);
        }
        // Libère la HeaderPage (dirty)
        bufferManager.FreePage(headerPageId, true);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public PageId getFreeDataPageId(int sizeRecord) {
    try {
        // On suppose que headerPageId est l'identifiant de la Header Page de la relation
        byte[] headerBuffer = bufferManager.GetPage(headerPageId);

        // Offset de la liste des pages "ayant de la place" dans la Header Page (deuxième PageId)
        int offset = 2 * 4; // Premier PageId (pleines), deuxième PageId (avec place)
        int freeListFileIdx = ByteBuffer.wrap(headerBuffer, offset, 4).getInt();
        int freeListPageIdx = ByteBuffer.wrap(headerBuffer, offset + 4, 4).getInt();

        // Parcours de la liste chaînée des pages "avec de la place"
        while (!(freeListFileIdx == -1 && freeListPageIdx == -1)) {
            PageId candidatePageId = new PageId(freeListFileIdx, freeListPageIdx);
            byte[] dataBuffer = bufferManager.GetPage(candidatePageId);

            // Vérifie la bytemap de la page (indice 0 à nbSlotsPerDataPage-1)
            for (int i = 0; i < nbSlotsPerDataPage; i++) {
                if (dataBuffer[i] == 0) {
                    // Slot libre trouvé, on suppose ici que sizeRecord rentre dans la page (vérif optionnelle)
                    bufferManager.FreePage(candidatePageId, false); // On libère rapidement la page
                    bufferManager.FreePage(headerPageId, false);    // Libère la header page
                    return candidatePageId;
                }
            }

            // Passe à la page suivante dans la liste chaînée
            int base = nbSlotsPerDataPage;
            int nextFileIdx = ByteBuffer.wrap(dataBuffer, base, 4).getInt();
            int nextPageIdx = ByteBuffer.wrap(dataBuffer, base + 4, 4).getInt();

            bufferManager.FreePage(candidatePageId, false); // Libère la page de données

            freeListFileIdx = nextFileIdx;
            freeListPageIdx = nextPageIdx;
        }
        bufferManager.FreePage(headerPageId, false);

    } catch (Exception e) {
        e.printStackTrace();
    }
    // Aucune page trouvée avec suffisamment d'espace
    return null;
}

public RecordId writeRecordToDataPage(Record record, PageId pageId) {
    try {
        // 1. Accède à la page cible via le BufferManager
        byte[] dataBuffer = bufferManager.GetPage(pageId);

        // 2. Trouve le premier slot libre dans la bytemap
        int slotIdx = -1;
        for (int i = 0; i < nbSlotsPerDataPage; i++) {
            if (dataBuffer[i] == 0) {
                slotIdx = i;
                break;
            }
        }
        if (slotIdx == -1)
            throw new RuntimeException("Aucun slot libre trouvé (la page aurait dû avoir de la place)");

        // 3. Position de stockage du record dans la page
        int recordOffset = nbSlotsPerDataPage         // bytemap en début de page
                         + slotIdx * getFixedRecordSizeBytes();

        // 4. Ecrit le record dans le buffer à la bonne position
        ByteBuffer buff = ByteBuffer.wrap(dataBuffer);
        writeRecordToBuffer(record, buff, recordOffset);

        // 5. Marque le slot comme utilisé dans la bytemap
        dataBuffer[slotIdx] = 1;

        // 6. Libère et marque la page comme dirty
        bufferManager.FreePage(pageId, true);

        // 7. Retourne le RecordId correspondant à l'insertion
        return new RecordId(pageId, slotIdx);

    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

public List<Record> getRecordsInDataPage(PageId pageId) {
    List<Record> records = new ArrayList<>();
    try {
        // 1. Accéder à la page via BufferManager
        byte[] dataBuffer = bufferManager.GetPage(pageId);

        // 2. Pour chaque slot de la bytemap, si occupé alors extraire le record correspondant
        int recordSize = getFixedRecordSizeBytes();
        for (int i = 0; i < nbSlotsPerDataPage; i++) {
            if (dataBuffer[i] == 1) {
                int recordOffset = nbSlotsPerDataPage + i * recordSize;
                ByteBuffer buff = ByteBuffer.wrap(dataBuffer);

                // Crée un nouvel objet Record à remplir
                // À chaque record trouve dans la page :
                Record rec = new Record();
                readFromBuffer(rec, buff, recordOffset); // Ajoute toutes les colonnes
                records.add(rec);

            }
        }
        // 3. Libérer la page après lecture
        bufferManager.FreePage(pageId, false);

    } catch (Exception e) {
        e.printStackTrace();
    }
    return records;
}

public List<PageId> getDataPages() {
    List<PageId> pages = new ArrayList<>();
    try {
        // Accède à la Header Page via BufferManager
        byte[] headerBuffer = bufferManager.GetPage(headerPageId);

        // Récupère la tête de la liste des pages "pleines"
        int fullListFileIdx = ByteBuffer.wrap(headerBuffer, 0, 4).getInt();
        int fullListPageIdx = ByteBuffer.wrap(headerBuffer, 4, 4).getInt();

        // Récupère la tête de la liste des pages "ayant de la place"
        int freeListFileIdx = ByteBuffer.wrap(headerBuffer, 8, 4).getInt();
        int freeListPageIdx = ByteBuffer.wrap(headerBuffer, 12, 4).getInt();

        // --------- Liste chaînée des pages "pleines" ----------
        while (!(fullListFileIdx == -1 && fullListPageIdx == -1)) {
            PageId pid = new PageId(fullListFileIdx, fullListPageIdx);
            pages.add(pid);

            byte[] dataBuffer = bufferManager.GetPage(pid);
            int base = nbSlotsPerDataPage; // Offset du chaînage
            int nextFileIdx = ByteBuffer.wrap(dataBuffer, base, 4).getInt();
            int nextPageIdx = ByteBuffer.wrap(dataBuffer, base + 4, 4).getInt();

            bufferManager.FreePage(pid, false);
            fullListFileIdx = nextFileIdx;
            fullListPageIdx = nextPageIdx;
        }

        // --------- Liste chaînée des pages "ayant de la place" ----------
        while (!(freeListFileIdx == -1 && freeListPageIdx == -1)) {
            PageId pid = new PageId(freeListFileIdx, freeListPageIdx);
            pages.add(pid);

            byte[] dataBuffer = bufferManager.GetPage(pid);
            int base = nbSlotsPerDataPage; // Offset du chaînage
            int nextFileIdx = ByteBuffer.wrap(dataBuffer, base, 4).getInt();
            int nextPageIdx = ByteBuffer.wrap(dataBuffer, base + 4, 4).getInt();

            bufferManager.FreePage(pid, false);
            freeListFileIdx = nextFileIdx;
            freeListPageIdx = nextPageIdx;
        }

        bufferManager.FreePage(headerPageId, false);

    } catch (Exception e) {
        e.printStackTrace();
    }
    return pages;
}



/////////////////////////////////////////
public RecordId InsertRecord(Record record) {
    // 1. Cherche une page avec de la place
    PageId pageId = getFreeDataPageId(getFixedRecordSizeBytes());
    if (pageId == null) {
        // Si aucune page disponible, on en crée une nouvelle
        addDataPage();
        pageId = getFreeDataPageId(getFixedRecordSizeBytes());
        if (pageId == null)
            throw new RuntimeException("Impossible d'insérer : aucune page disponible même après ajout");
    }
    // 2. Insère le record sur cette page
    return writeRecordToDataPage(record, pageId);
}

public List<Record> GetAllRecords() {
    List<Record> allRecords = new ArrayList<>();
    List<PageId> pageIds = getDataPages();
    for (PageId pid : pageIds) {
        allRecords.addAll(getRecordsInDataPage(pid));
    }
    return allRecords;
}

public void DeleteRecord(RecordId rid) {
    try {
        PageId pageId = rid.getPageId();
        int slotIdx = rid.getSlotIdx();

        // 1. Charge la page et libère le slot
        byte[] dataBuffer = bufferManager.GetPage(pageId);
        dataBuffer[slotIdx] = 0;

        // 2. Vérifie si la page est vide (tous slots à 0)
        boolean isEmpty = true;
        for (int i = 0; i < nbSlotsPerDataPage; i++) {
            if (dataBuffer[i] == 1) {
                isEmpty = false;
                break;
            }
        }
        bufferManager.FreePage(pageId, true);

        if (isEmpty) {
            // 3. Désallouer la page et MAJ les listes doublement chaînées
            // 3a. Lis les pointeurs next et prev dans la page à supprimer
            byte[] delBuf = bufferManager.GetPage(pageId);
            int offset = nbSlotsPerDataPage;
            int nextFileIdx = ByteBuffer.wrap(delBuf, offset, 4).getInt();
            int nextPageIdx = ByteBuffer.wrap(delBuf, offset + 4, 4).getInt();
            int prevFileIdx = ByteBuffer.wrap(delBuf, offset + 8, 4).getInt();
            int prevPageIdx = ByteBuffer.wrap(delBuf, offset + 12, 4).getInt();
            bufferManager.FreePage(pageId, false);

            PageId next = (nextFileIdx != -1 && nextPageIdx != -1) ? new PageId(nextFileIdx, nextPageIdx) : null;
            PageId prev = (prevFileIdx != -1 && prevPageIdx != -1) ? new PageId(prevFileIdx, prevPageIdx) : null;

            // 3b. MAJ du next.prev et du prev.next
            if (next != null) {
                byte[] nextBuf = bufferManager.GetPage(next);
                ByteBuffer.wrap(nextBuf, offset + 8, 4).putInt(prevFileIdx);     // next.prev.fileIdx = prevFileIdx
                ByteBuffer.wrap(nextBuf, offset + 12, 4).putInt(prevPageIdx);    // next.prev.pageIdx = prevPageIdx
                bufferManager.FreePage(next, true);
            }
            if (prev != null) {
                byte[] prevBuf = bufferManager.GetPage(prev);
                ByteBuffer.wrap(prevBuf, offset, 4).putInt(nextFileIdx);    // prev.next.fileIdx = nextFileIdx
                ByteBuffer.wrap(prevBuf, offset + 4, 4).putInt(nextPageIdx);// prev.next.pageIdx = nextPageIdx
                bufferManager.FreePage(prev, true);
            }

            // 3c. Si c'était la tête de liste ("head"), MAJ la HeaderPage
            // Ici on suppose que la liste "pages ayant de la place" démarre à l'offset 8 dans la HeaderPage
            byte[] headerBuffer = bufferManager.GetPage(headerPageId);
            // Regarde si le head de la liste pointe sur pageId
            int headFileIdx = ByteBuffer.wrap(headerBuffer, 8, 4).getInt();
            int headPageIdx = ByteBuffer.wrap(headerBuffer, 12, 4).getInt();
            if (headFileIdx == pageId.getFileIdx() && headPageIdx == pageId.getPageIdx()) {
                // MAJ le head vers "next"
                int newHeadFileIdx = (next != null) ? next.getFileIdx() : -1;
                int newHeadPageIdx = (next != null) ? next.getPageIdx() : -1;
                ByteBuffer.wrap(headerBuffer, 8, 4).putInt(newHeadFileIdx);
                ByteBuffer.wrap(headerBuffer, 12, 4).putInt(newHeadPageIdx);
                bufferManager.FreePage(headerPageId, true);
            } else {
                bufferManager.FreePage(headerPageId, false);
            }

            // 4. Désalloue la page sur disque via le DiskManager
            diskManager.DeallocPage(pageId);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}





   
}

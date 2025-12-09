import java.util.ArrayList;
import java.util.List;

public class DBManagerTest {

    public static void main(String[] args) {
        // 1) Préparer la config et le DBManager
        DBConfig cfg = new DBConfig(".DBTEST", 4096, 4, 2, "LRU");
        DBManager manager = new DBManager(cfg);

        // 2) Créer une relation simple
        List<ColumnInfo> cols = new ArrayList<>();
        cols.add(new ColumnInfo("X", ColumnType.INT));
        cols.add(new ColumnInfo("C", ColumnType.CHAR, 10));
        PageId header = new PageId(0, 1);
        int nbSlots = 5;
        Relation r = new Relation("R", cols, header, nbSlots, null, null);

        // fais moi autre exemple de table
        List<ColumnInfo> cols2 = new ArrayList<>();
        cols2.add(new ColumnInfo("A", ColumnType.FLOAT));
        Relation r2 = new Relation("R2", cols2, new PageId(1, 2), 3, null, null);

        // ---- Test addTable / getTable ----
        manager.addTable(r);
        manager.addTable(r2);
        if (manager.getTable("R") == null) {
            throw new AssertionError("getTable(R) should not be null");
        }

        // ---- Test describeTable / describeTables (juste pour voir que ça ne plante pas) ----
        manager.describeTable("R");
        manager.describeTables();

        // ---- Test removeTable / removeAllTables ----
        manager.removeTable("R");
        if (manager.getTable("R") != null) {
            throw new AssertionError("R should have been removed");
        }
        manager.addTable(r);
        manager.removeAllTables();
        if (manager.getTable("R") != null || manager.getTable("R2") != null) {
            throw new AssertionError("All tables should have been removed");
        }

        manager.describeTables(); // normalement rien d'affiché

        // ---- Test saveState / loadState ----
        // Re-ajouter une table, sauvegarder, recharger dans un nouveau DBManager
        manager.addTable(r);
        manager.addTable(r2);
        manager.saveState();

        DBManager manager2 = new DBManager(cfg);
        manager2.loadState();
        Relation loaded = manager2.getTable("R");
        if (loaded == null) {
            throw new AssertionError("Loaded table R should exist");
        }
        if (loaded.getColumns().size() != 2) {
            throw new AssertionError("R should have 2 columns");
        }
        if (!loaded.getColumns().get(0).name.equals("X")) {
            throw new AssertionError("First column should be X");
        }
        if (loaded.getHeaderPageId() == null
                || loaded.getHeaderPageId().getFileIdx() != 0
                || loaded.getHeaderPageId().getPageIdx() != 1) {
            throw new AssertionError("HeaderPageId should be (0,1)");
        }
        if (loaded.getNbSlotsPerDataPage() != 5) {
            throw new AssertionError("nbSlotsPerDataPage should be 5");
        }

        System.out.println("DBManagerTest: all inline tests passed.");
    }
}

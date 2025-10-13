import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DiskManagerTests {
    private static DBConfig cfg = new DBConfig(".", 16, 4);

    public static void main(String[] args) throws Exception {
        // If called with 'verbose' or 'demo', run the merged unit demo and exit
        if (args != null && args.length > 0 && ("verbose".equalsIgnoreCase(args[0]) || "demo".equalsIgnoreCase(args[0]))) {
            boolean ok = VerboseDemo();
            System.exit(ok ? 0 : 5);
            return;
        }

        System.out.println("=== DiskManager Tests (refactor) ===\n");

        boolean ok1 = TestAllocWriteRead();
        System.out.println();
        boolean ok2 = TestPersistence();
        System.out.println();
        boolean ok3 = TestGrowth();

        System.out.println("\n=== Summary ===");
        System.out.println("Alloc/Write/Read test : " + (ok1?"PASSED":"FAILED"));
        System.out.println("Persistence test     : " + (ok2?"PASSED":"FAILED"));
        System.out.println("Growth test          : " + (ok3?"PASSED":"FAILED"));

        if (ok1 && ok2 && ok3) System.exit(0); else System.exit(5);
    }

    // Helper: cleanup BinData
    public static void cleanupBinData(DBConfig c) {
        Path bd = Paths.get(c.getDbpath()).resolve("BinData");
        try { if (Files.exists(bd)) Files.walk(bd).sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p);} catch (IOException e){} }); } catch (Exception ignored) {}
    }

    // Test 1: allocation, write, read, dealloc, reallocate
    public static boolean TestAllocWriteRead() {
        System.out.println("--- TestAllocWriteRead ---");
        cleanupBinData(cfg);
        DiskManager dm = new DiskManager(cfg);
        try {
            dm.Init();
            PageId p1 = dm.AllocPage();
            System.out.println("Alloc p1=" + p1);
            PageId p2 = dm.AllocPage();
            System.out.println("Alloc p2=" + p2);

            byte[] buf = new byte[cfg.getPagesize()];
            for (int i = 0; i < buf.length; i++) buf[i] = (byte) (i+1);
            dm.WritePage(p1, buf);

            byte[] read = new byte[cfg.getPagesize()];
            dm.ReadPage(p1, read);
            boolean eq = Arrays.equals(buf, read);
            System.out.println("Read equals written: " + eq);
            if (!eq) return false;

            dm.DeallocPage(p1);
            PageId p3 = dm.AllocPage();
            System.out.println("Realloc p3=" + p3);
            if (!p3.equals(p1)) { System.out.println("Realloc mismatch"); return false; }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try { dm.Finish(); } catch (Exception ignored) {}
        }
    }

    // Test 2: persistence
    public static boolean TestPersistence() {
        System.out.println("--- TestPersistence ---");
        cleanupBinData(cfg);
        try {
            DiskManager dm = new DiskManager(cfg);
            dm.Init();
            PageId a1 = dm.AllocPage();
            System.out.println("Alloc a1=" + a1);
            dm.Finish();

            // Reopen and check
            DiskManager dm2 = new DiskManager(cfg);
            dm2.Init();
            int pages = dm2.getPageCount(a1.getFileIdx());
            System.out.println("Pages in file after restart: " + pages);
            PageId r = dm2.AllocPage();
            System.out.println("Alloc after restart r=" + r);
            boolean ok = !r.equals(a1);
            dm2.Finish();
            return ok;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Test 3: growth up to dm_maxfilecount and verify exception
    public static boolean TestGrowth() {
        System.out.println("--- TestGrowth ---");
        cleanupBinData(cfg);
        // force per-file small capacity to spread pages across files
        System.setProperty("dm.maxpagesperfile", "1");
        DBConfig cfg2 = new DBConfig(".", 16, 2);
        try {
            DiskManager dm = new DiskManager(cfg2);
            dm.Init();
            PageId g1 = dm.AllocPage(); System.out.println("g1=" + g1);
            PageId g2 = dm.AllocPage(); System.out.println("g2=" + g2);
            try {
                dm.AllocPage();
                System.out.println("Expected exception but allocation succeeded");
                dm.Finish();
                return false;
            } catch (IOException ex) {
                System.out.println("Expected exception: " + ex.getMessage());
                dm.Finish();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            System.clearProperty("dm.maxpagesperfile");
        }
    }

    // Merged from DiskManagerUnitTests: a very verbose, step-by-step demo.
    public static boolean VerboseDemo() throws Exception {
        System.out.println("=== DiskManager Unit Tests (commented, merged) ===\n");

        // Configuration de test : pagesize=16 pour visibilité
        System.out.println("// Ligne: DBConfig cfg = new DBConfig(\".\", 16, 4);  --> créer un objet DBConfig avec dbpath='.', pagesize=16, dm_maxfilecount=4");
        DBConfig lcfg = new DBConfig(".", 16, 4);
        System.out.println("Configuration de test = " + lcfg);

        // Nettoyage du dossier BinData pour partir d'une base propre
        System.out.println("// Ligne: Path bd = Paths.get(cfg.getDbpath()).resolve(\"BinData\");  --> calcule le chemin ./BinData");
        Path bd = Paths.get(lcfg.getDbpath()).resolve("BinData");
        System.out.println("Computed BinData path = " + bd.toAbsolutePath());
        System.out.println("// Vérifier si ce chemin existe (Files.exists)");
        if (Files.exists(bd)) {
            System.out.println("BinData existe -> suppression récursive des fichiers et dossiers qu'il contient");
            try {
                System.out.println("// Ligne: Files.walk(bd).sorted(...).forEach(Files.deleteIfExists)");
                Files.walk(bd).sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p);} catch (Exception e){} });
            } catch (Exception ignored) {}
            System.out.println("Suppression BinData terminée");
        } else {
            System.out.println("BinData n'existe pas, rien à supprimer");
        }

        System.out.println("// Ligne: DiskManager dm = new DiskManager(cfg);  --> créer un gestionnaire disque en mémoire lié à la config");
        DiskManager dm = new DiskManager(lcfg);

        // 1) Init()
        System.out.println("\n--- Etape 1 : Init() ---");
        System.out.println("// Appel: dm.Init() -> crée le dossier BinData (si absent), ouvre les DataX.bin existants et charge les .meta");
        System.out.println("Avant Init: Files.exists(BinData) = " + Files.exists(bd));
        dm.Init();
        System.out.println("Après Init: Files.exists(BinData) = " + Files.exists(bd));
        System.out.println("Après Init: getPageCount(0) = " + dm.getPageCount(0) + "  (taille du fichier Data0.bin / pagesize)");

        // 2) AllocPage()
        System.out.println("\n--- Etape 2 : AllocPage() x2 ---");
        System.out.println("// Appel: PageId p1 = dm.AllocPage();  -> cherche une page libre dans les bitmaps, sinon étend un fichier");
        PageId p1 = dm.AllocPage();
        System.out.println("Alloc p1 = " + p1 + "  (fileIdx=" + p1.getFileIdx() + ", pageIdx=" + p1.getPageIdx() + ")");
        System.out.println("// Appel: PageId p2 = dm.AllocPage();  -> allocation suivante") ;
        PageId p2 = dm.AllocPage();
        System.out.println("Alloc p2 = " + p2 + "  (fileIdx=" + p2.getFileIdx() + ", pageIdx=" + p2.getPageIdx() + ")");
        System.out.println("// getPageCount(0) retourne le nombre de pages actuellement présentes physiquement");
        System.out.println("getPageCount(0) après allocations physiques = " + dm.getPageCount(0));

        // 3) WritePage + ReadPage
        System.out.println("\n--- Etape 3 : WritePage/ReadPage ---");
        System.out.println("// Préparer un buffer de test puis écrire dans la page p1");
        byte[] buf = new byte[lcfg.getPagesize()];
        for (int i = 0; i < buf.length; i++) {
            System.out.println("// buf[" + i + "] = (byte)" + (i+1));
            buf[i] = (byte) (i + 1);
        }
        System.out.println("// Appel: dm.WritePage(p1, buf) -> écrit pagesize octets à l'offset pageIdx*pagesize");
        dm.WritePage(p1, buf);
        System.out.println("// Préparer buffer de lecture et appeler dm.ReadPage(p1, read)");
        byte[] read = new byte[lcfg.getPagesize()];
        dm.ReadPage(p1, read);
        System.out.println("// Contenu lu (en décimaux) : ");
        for (int i = 0; i < read.length; i++) System.out.print((read[i] & 0xFF) + (i+1<read.length?",":"\n"));
        boolean eq = Arrays.equals(buf, read);
        System.out.println("Comparaison == " + eq);
        if (!eq) { dm.Finish(); return false; }

        // 4) DeallocPage()
        System.out.println("\n--- Etape 4 : DeallocPage() puis réallocation ---");
        System.out.println("// Appel: dm.DeallocPage(p1) -> marque la page comme libre dans la bitmap en mémoire");
        dm.DeallocPage(p1);
        System.out.println("// Ensuite Appel: PageId p3 = dm.AllocPage() -> doit réutiliser la première page libre");
        PageId p3 = dm.AllocPage();
        System.out.println("Réallocation p3 = " + p3 + " (doit égaler p1)");
        if (!p3.equals(p1)) { System.out.println("Realloc mismatch"); dm.Finish(); return false; }

        // 5) Finish() et persistance
        System.out.println("\n--- Etape 5 : Finish() ---");
        System.out.println("// Appel: dm.Finish() -> force flush sur canaux, écrit les bitmaps dans Data{i}.meta et ferme les fichiers");
        dm.Finish();
        System.out.println("Finish done. Vérifier physiquement que les fichiers Data*.meta existent dans BinData si besoin");

        // 6) Ré-init et vérification de persistance
        System.out.println("\n--- Etape 6 : Re-Init() et vérification de persistance ---");
        System.out.println("// Créer un nouveau DiskManager et appeler Init() pour vérifier que les .meta sont lus et restaurent la bitmap");
        DiskManager dm2 = new DiskManager(lcfg);
        dm2.Init();
        System.out.println("// Appel: dm2.getPageCount(0) -> nombre de pages physiques détectées");
        System.out.println("getPageCount(0) après re-init = " + dm2.getPageCount(0));
        System.out.println("// Allouer une nouvelle page pour vérifier si l'ancienne était considérée comme utilisée");
        PageId after = dm2.AllocPage();
        System.out.println("Alloc après restart = " + after + "  (si égal à previously allocated => persistance NOK)");
        boolean notEqualOld = !after.equals(p1);
        dm2.Finish();

        System.out.println("\n=== DiskManager Unit Tests finished ===");
        return notEqualOld;
    }
}

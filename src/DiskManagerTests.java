import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class DiskManagerTests {
    public static void main(String[] args) throws Exception {
        System.out.println("=== DiskManager Tests ===");

        // Configuration de test : pagesize petit pour vérification
        DBConfig cfg = new DBConfig(".", 16, 4);
        DiskManager dm = new DiskManager(cfg);

        // Cleanup test BinData
        Path bd = Paths.get(cfg.getDbpath()).resolve("BinData");
        if (Files.exists(bd)) {
            // supprimer les fichiers data* pour test propre
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(bd)) {
                for (Path p : ds) Files.deleteIfExists(p);
            } catch (Exception ignored) {}
        }

        dm.Init();

        // Test allocation
        PageId p1 = dm.AllocPage();
        System.out.println("Alloc p1=" + p1);
        PageId p2 = dm.AllocPage();
        System.out.println("Alloc p2=" + p2);

        // Test write
        byte[] buf = new byte[cfg.getPagesize()];
        for (int i = 0; i < buf.length; i++) buf[i] = (byte) (i + 1);
        dm.WritePage(p1, buf);

        // Test read
        byte[] read = new byte[cfg.getPagesize()];
        dm.ReadPage(p1, read);
        System.out.println("Read eq : " + Arrays.equals(buf, read));

        // Test dealloc
        dm.DeallocPage(p1);
        // Réallouer - devrait reuse first free page
        PageId p3 = dm.AllocPage();
        System.out.println("Alloc p3=" + p3);

        dm.Finish();
        System.out.println("=== DiskManager Tests Finished ===");
    }
}

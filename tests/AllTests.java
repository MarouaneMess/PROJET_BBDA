public class AllTests {
    public static void main(String[] args) {
        System.out.println("=== Running All Tests ===\n");
        int rc = 0;
        try {
            // DBConfig tests
            System.out.println("[1/3] DBConfigTest...");
            DBConfigTest.main(new String[0]);
        } catch (Throwable t) { t.printStackTrace(); rc = 1; }
        try {
            // DiskManager tests
            System.out.println("\n[2/3] DiskManagerTests...");
            DiskManagerTests.main(new String[0]);
        } catch (Throwable t) { t.printStackTrace(); rc = 1; }
        try {
            // BufferManager tests
            System.out.println("\n[3/3] BufferManagerTests...");
            BufferManagerTests.main(new String[0]);
        } catch (Throwable t) { t.printStackTrace(); rc = 1; }
        System.exit(rc);
    }
}

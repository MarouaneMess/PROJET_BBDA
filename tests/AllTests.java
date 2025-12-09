public class AllTests {
    public static void main(String[] args) {
        System.out.println("=== Running All Tests ===\n");
        int rc = 0;
        String[] names = new String[] {
            "DBConfigTest",
            "DiskManagerTests",
            "BufferManagerTests",
            "RelationRecordTests",
            "DBManagerTest",
        };

        

        int total = names.length;

        // Determine the classpath used by the parent JVM so child JVMs can use the same one.
        String parentClassPath = System.getProperty("java.class.path");

        for (int i = 0; i < total; i++) {
            System.out.println(String.format("\n[%d/%d] %s...", i+1, total, names[i]));

            // Launch each test in a separate JVM to avoid System.exit in a test killing the aggregator.
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("java", "-cp", parentClassPath, names[i]);
            pb.redirectErrorStream(true);

            try {
                Process p = pb.start();

                // Stream child output to parent stdout.
                java.io.InputStream is = p.getInputStream();
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf)) != -1) {
                    System.out.write(buf, 0, read);
                }

                int exit = p.waitFor();
                if (exit != 0) {
                    System.out.println(names[i] + " returned exit code " + exit);
                    rc = 1;
                }
            } catch (Exception e) {
                System.out.println("Failed to run " + names[i] + ":");
                e.printStackTrace(System.out);
                rc = 1;
            }
        }

        System.exit(rc);
    }
}

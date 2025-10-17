import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class RelationRecordTests {
    // Helper assertion method to compare expected and actual values 
    private static void assertEq(String msg, Object exp, Object got) {
        if ((exp == null && got != null) || (exp != null && !exp.equals(got))) {
            throw new AssertionError(msg + " expected=" + exp + " got=" + got);
        }
    }

    private static void testFixedSizes() {
        ColumnInfo c1 = new ColumnInfo("id", ColumnType.INT);
        ColumnInfo c2 = new ColumnInfo("price", ColumnType.FLOAT);
        ColumnInfo c3 = new ColumnInfo("code", ColumnType.CHAR, 5);
        ColumnInfo c4 = new ColumnInfo("name", ColumnType.VARCHAR, 10);
        Relation r = new Relation("P", Arrays.asList(c1, c2, c3, c4));
        int expected = 4 + 4 + (2 * 5) + (4 + 2 * 10);
        assertEq("fixed size bytes", expected, r.getFixedRecordSizeBytes());
        System.out.println("testFixedSizes: PASSED");
    }

    private static void testWriteReadSimple() {
        List<ColumnInfo> cols = Arrays.asList(
                new ColumnInfo("id", ColumnType.INT),
                new ColumnInfo("val", ColumnType.FLOAT),
                new ColumnInfo("c", ColumnType.CHAR, 3),
                new ColumnInfo("v", ColumnType.VARCHAR, 6)
        );
        Relation r = new Relation("T", cols);
        int sz = r.getFixedRecordSizeBytes();
        ByteBuffer buf = ByteBuffer.allocate(sz + 16);

        Record rec = new Record(Arrays.asList("123", "3.14", "AB", "Hello"));
        r.writeRecordToBuffer(rec, buf, 8);

        Record out = new Record();
        r.readFromBuffer(out, buf, 8);

        assertEq("id", "123", out.get(0));
        assertEq("val", Float.toString(Float.parseFloat("3.14")), out.get(1));
        assertEq("c", "AB", out.get(2)); // padded during write, trimmed on read
        assertEq("v", "Hello", out.get(3));
        System.out.println("testWriteReadSimple: PASSED");
    }

    private static void testCharTruncateAndPad() {
        Relation r = new Relation("C", Arrays.asList(new ColumnInfo("c", ColumnType.CHAR, 4)));
        int sz = r.getFixedRecordSizeBytes();
        ByteBuffer buf = ByteBuffer.allocate(sz);

        // longer than T -> truncate to 4
        Record rec1 = new Record(Arrays.asList("ABCDEFG"));
        r.writeRecordToBuffer(rec1, buf, 0);
        Record out1 = new Record();
        r.readFromBuffer(out1, buf, 0);
        assertEq("char truncate", "ABCD", out1.get(0));

        // shorter than T -> pad with NULs, read trims NULs
        Record rec2 = new Record(Arrays.asList("Z"));
        r.writeRecordToBuffer(rec2, buf, 0);
        Record out2 = new Record();
        r.readFromBuffer(out2, buf, 0);
        assertEq("char pad", "Z", out2.get(0));
        System.out.println("testCharTruncateAndPad: PASSED");
    }

    private static void testVarcharLengthPrefix() {
        Relation r = new Relation("V", Arrays.asList(new ColumnInfo("v", ColumnType.VARCHAR, 5)));
        int sz = r.getFixedRecordSizeBytes();
        ByteBuffer buf = ByteBuffer.allocate(sz);

        Record rec1 = new Record(Arrays.asList("HELLO"));
        r.writeRecordToBuffer(rec1, buf, 0);
        Record out1 = new Record();
        r.readFromBuffer(out1, buf, 0);
        assertEq("varchar exact", "HELLO", out1.get(0));

        Record rec2 = new Record(Arrays.asList("WORLD!!")); // longer than T=5
        r.writeRecordToBuffer(rec2, buf, 0);
        Record out2 = new Record();
        r.readFromBuffer(out2, buf, 0);
        assertEq("varchar truncate", "WORLD", out2.get(0));
        System.out.println("testVarcharLengthPrefix: PASSED");
    }

    private static void testIntegrationExample() {
        List<ColumnInfo> cols = new ArrayList<>();
        cols.add(new ColumnInfo("id", ColumnType.INT));
        cols.add(new ColumnInfo("val", ColumnType.FLOAT));
        cols.add(new ColumnInfo("name", ColumnType.CHAR, 5));
        cols.add(new ColumnInfo("descr", ColumnType.VARCHAR, 10));

        Relation rel = new Relation("people", cols);

        Record r = new Record();
        r.add("123");            // INT
        r.add("3.14");           // FLOAT
        r.add("hi");             // CHAR(5)
        r.add("variable-length"); // VARCHAR(10) -> "variable-l"

        int recSize = rel.getFixedRecordSizeBytes();
        ByteBuffer buff = ByteBuffer.allocate(recSize);

        rel.writeRecordToBuffer(r, buff, 0);

        Record r2 = new Record();
        rel.readFromBuffer(r2, buff, 0);

        assertEq("INT", "123", r2.get(0));
        float expectedF = 3.14f;
        float readF = Float.parseFloat(r2.get(1));
        if (Math.abs(expectedF - readF) > 1e-5f) throw new AssertionError("FLOAT mismatch: " + r2.get(1));
        assertEq("CHAR", "hi", r2.get(2));
        assertEq("VARCHAR", "variable-l", r2.get(3));
        // je veux print le resultat ici . 
        System.out.println("INT: " + r2.get(0));
        System.out.println("FLOAT: " + r2.get(1));
        System.out.println("CHAR: " + r2.get(2));
        System.out.println("VARCHAR: " + r2.get(3));

        System.out.println("Integration example test passed");
    }

    public static void main(String[] args) {
        System.out.println("RelationRecordTests: start");
        try {
            runAllTests();
            System.out.println("RelationRecordTests: PASSED");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void runAllTests() throws Exception {
        testFixedSizes();
        testWriteReadSimple();
        testCharTruncateAndPad();
        testVarcharLengthPrefix();
        testIntegrationExample();
    }
}

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class RelationRecordTests {
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
        // Allow float string equivalence by Double.parseFloat comparison
        assertEq("val", Float.toString(Float.parseFloat("3.14")), out.get(1));
        assertEq("c", "AB", out.get(2)); // padded during write, trimmed on read
        assertEq("v", "Hello", out.get(3));
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
    }

    public static void main(String[] args) {
        System.out.println("RelationRecordTests: start");
        testFixedSizes();
        testWriteReadSimple();
        testCharTruncateAndPad();
        testVarcharLengthPrefix();
        System.out.println("RelationRecordTests: PASSED");
    }
}

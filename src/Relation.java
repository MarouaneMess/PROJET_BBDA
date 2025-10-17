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

    public Relation(String name, List<ColumnInfo> columns) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("relation name required");
        if (columns == null || columns.isEmpty()) throw new IllegalArgumentException("at least one column required");
        this.name = name;
        this.columns = new ArrayList<>(columns);
    }

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
}

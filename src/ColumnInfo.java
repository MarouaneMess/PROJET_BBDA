/**
 * ColumnInfo holds the schema info for a single column: name, type, and optional length T
 * (for CHAR(T) and VARCHAR(T)). For INT/FLOAT, lengthT is ignored (set to 0).
 */
public class ColumnInfo {
    public final String name;
    public final ColumnType type;
    public final int lengthT; // only for CHAR/VARCHAR, otherwise 0

    public ColumnInfo(String name, ColumnType type) {
        this(name, type, 0);
    }

    public ColumnInfo(String name, ColumnType type, int lengthT) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("column name required");
        if (type == null) throw new IllegalArgumentException("column type required");
        if ((type == ColumnType.CHAR || type == ColumnType.VARCHAR) && lengthT <= 0) {
            throw new IllegalArgumentException("length T must be > 0 for CHAR/VARCHAR");
        }
        this.name = name;
        this.type = type;
        this.lengthT = (type == ColumnType.CHAR || type == ColumnType.VARCHAR) ? lengthT : 0;
    }

    public int fixedSizeBytes() {
        return type.fixedSizeBytes(lengthT);
    }
}

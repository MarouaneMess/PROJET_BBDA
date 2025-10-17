/**
 * ColumnType represents the supported column kinds for TP4.
 * For CHAR and VARCHAR, a length parameter (T) is required and stored on ColumnInfo.
 */
public enum ColumnType {
    INT,
    FLOAT,
    CHAR,
    VARCHAR;

    /**
     * Returns the fixed number of bytes needed to store a value of this type.
     * For parameterized types (CHAR/VARCHAR), the size depends on lengthT.
     * Convention used:
     * - INT: 4 bytes (ByteBuffer.putInt)
     * - FLOAT: 4 bytes (ByteBuffer.putFloat)
     * - CHAR(T): T Java chars, each 2 bytes (ByteBuffer.putChar) => 2*T bytes
     * - VARCHAR(T): 4 bytes length prefix + T Java chars (2 bytes each) => 4 + 2*T bytes
     */
    public int fixedSizeBytes(int lengthT) {
        switch (this) {
            case INT:
                return 4;
            case FLOAT:
                return 4;
            case CHAR:
                if (lengthT < 0) throw new IllegalArgumentException("CHAR requires non-negative length");
                return 2 * lengthT;
            case VARCHAR:
                if (lengthT < 0) throw new IllegalArgumentException("VARCHAR requires non-negative max length");
                return 4 + 2 * lengthT; // 4 bytes length prefix + payload space
            default:
                throw new IllegalStateException("Unexpected type: " + this);
        }
    }
}

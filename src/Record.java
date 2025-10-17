import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Record represents a tuple (row). Values are stored in a list in column order.
 * Values are kept as Strings for simplicity; Relation is responsible for converting
 * to/from binary representation using the schema types.
 */
public class Record {
    private final List<String> values;

    public Record() {
        this.values = new ArrayList<>();
    }

    public Record(List<String> values) {
        if (values == null) throw new IllegalArgumentException("values cannot be null");
        this.values = new ArrayList<>(values);
    }

    public void add(String value) {
        values.add(value);
    }

    public int size() {
        return values.size();
    }

    public String get(int idx) {
        return values.get(idx);
    }

    public void set(int idx, String value) {
        values.set(idx, value);
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void clear() {
        values.clear();
    }
}

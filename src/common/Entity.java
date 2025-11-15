package common;

/**
 * Base type for CSV-backed entities providing convenience methods for array-driven storage.
 */
public abstract class Entity {


    protected String[] values;

    /**
     * Returns the value stored at the requested index or {@code null} when out of bounds.
     */
    public String getArrayValueByIndex(int i){
        if (i < values.length) return values[i];
        return null;
    }

    /**
     * Updates the value stored at the requested index when present.
     *
     * @return the value that was written, or {@code null} when the index is invalid
     */
    public String setArrayValueByIndex(int i, String value){
        if (i < values.length) {
            values[i] = value;
            return values[i];
        }
        return null;
    }

    /**
     * Serializes the backing array into comma-separated form.
     *
     * @return CSV representation of the entity
     */
    public String toCSVFormat() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i] != null ? values[i] : "");
            if (i < values.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}

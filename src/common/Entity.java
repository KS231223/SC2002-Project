package common;

public abstract class Entity {


    protected String[] values;
    public String getArrayValueByIndex(int i){
        if (i < values.length) return values[i];
        return null;
    }
    public String setArrayValueByIndex(int i, String value){
        if (i < values.length) {
            values[i] = value;
            return values[i];
        }
        return null;
    }
    public String toCSVFormat() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i] != null ? values[i] : "");
            if (i < values.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}

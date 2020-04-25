package data;

public class DataType {
    private String name, id;
    private boolean isPrimitive;

    public DataType(String name, String id) {
        this.name = name;
        this.id = id;
        this.isPrimitive = false;
    }

    public DataType(String name, boolean isPrimitive) {
        this.name = name;
        this.id = "";
        this.isPrimitive = isPrimitive;
    }

    @Override
    public String toString() {
        return id.length() > 0 ? id : name;
    }
}

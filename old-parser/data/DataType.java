package data;

import java.util.ArrayList;
import java.util.List;

public class DataType {
    private String name, id;
    private boolean isPrimitive;
    private boolean isArray;
    private boolean isVoid = false;
    private boolean isGenericType = false;
    private List<DataType> typeArgs = new ArrayList<>();
    private ParseFrom parseFrom;

    public ParseFrom getParseFrom() {
        return parseFrom;
    }

    public void setParseFrom(ParseFrom parseFrom) {
        this.parseFrom = parseFrom;
    }

    public DataType(String id, String name, boolean isArray) {
        this.name = name;
        this.id = id;
        this.isPrimitive = false;
        this.isArray = isArray;
    }

    public DataType(String name, boolean isPrimitive, boolean isArray) {
        this.name = name;
        this.id = "";
        this.isPrimitive = isPrimitive;
        this.isArray = isArray;
    }

    public boolean isGenericType() {
        return isGenericType;
    }

    public void setGenericType(boolean genericType) {
        isGenericType = genericType;
    }

    public void addTypeArg(DataType typeArg) {
        this.typeArgs.add(typeArg);
    }

    public List<DataType> getTypeArgs() {
        return typeArgs;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public void setVoid(boolean aVoid) {
        isVoid = aVoid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        String name = id.length() > 0 ? id : this.name;
        if (isArray) name = name + "[]";
        return name;
    }
}

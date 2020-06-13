package data;

import config.StringConstant;

public class FieldMember extends Member {
    protected DataType type;
    protected boolean isStatic = false;

    public FieldMember(String name, DataType type, StringConstant accessLevel) {
        super(name, accessLevel);
        this.type = type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return accessModifier + " " + type + " " + name;
    }
}

package data;

import config.StringConstant;

public class FieldMember extends Member {
    protected DataType type;

    public FieldMember(String name, DataType type, StringConstant accessLevel) {
        super(name, accessLevel);
        this.type = type;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }
}

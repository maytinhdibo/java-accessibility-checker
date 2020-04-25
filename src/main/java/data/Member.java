package data;

import config.StringConstant;

class Member{
    private String name;
    private DataType type;
    private StringConstant accessLevel;

    public Member(String name, DataType type, StringConstant accessLevel) {
        this.name = name;
        this.type = type;
        this.accessLevel = accessLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public StringConstant getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(StringConstant accessLevel) {
        this.accessLevel = accessLevel;
    }
}

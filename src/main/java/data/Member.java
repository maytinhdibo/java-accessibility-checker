package data;

import config.StringConstant;

class Member{
    private String name;
    private DataType type;
    private StringConstant accessModifier;

    public Member(String name, DataType type, StringConstant accessModifier) {
        this.name = name;
        this.type = type;
        this.accessModifier = accessModifier;
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

    public StringConstant getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(StringConstant accessModifier) {
        this.accessModifier = accessModifier;
    }
}

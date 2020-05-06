package data;

import config.StringConstant;

class Member{
    protected String name;
    protected StringConstant accessModifier;

    public Member(String name, StringConstant accessModifier) {
        this.name = name;
        this.accessModifier = accessModifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StringConstant getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(StringConstant accessModifier) {
        this.accessModifier = accessModifier;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

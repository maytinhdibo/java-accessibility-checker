package data;

import config.StringConstant;

class Member{
    protected String name;
    protected StringConstant accessModifier;
    protected ClassModel parentClass;
    private String originClass;


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

    public ClassModel getParentClass() {
        return parentClass;
    }

    public void setParentClass(ClassModel parentClass) {
        this.parentClass = parentClass;
    }

    public String getOriginClass() {
        return originClass;
    }

    public void setOriginClass(String originClass) {
        this.originClass = originClass;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

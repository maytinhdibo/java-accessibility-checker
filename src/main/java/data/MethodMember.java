package data;

import config.StringConstant;

public class MethodMember extends MemberWithParams {
    protected DataType type;


    public MethodMember(String name, DataType type, StringConstant accessLevel) {
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

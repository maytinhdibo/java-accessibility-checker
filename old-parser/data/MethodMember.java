package data;

import config.StringConstant;

import java.util.ArrayList;
import java.util.List;

public class MethodMember extends MemberWithParams {
    protected DataType type;
    protected boolean isStatic = false;
    private List<String> genericTypes = new ArrayList<>();

    public MethodMember(String name, DataType type, StringConstant accessLevel) {
        super(name, accessLevel);
        this.type = type;
    }

    public MethodMember(MemberWithParams memberWithParams){
        super(memberWithParams);
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public void setGenericTypes(List<String> genericTypes) {
        this.genericTypes = genericTypes;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public List<String> getGenericTypes() {
        return genericTypes;
    }

    public void addGenericType(String genericType) {
        this.genericTypes.add(genericType);
    }

    @Override
    public String toString() {
        List<String> paramsAsString = new ArrayList<>();
        params.forEach(param -> {
            paramsAsString.add(param.toString());
        });
        return accessModifier.toString() + " " + type.toString() + " " + super.name + "(" + String.join(", ", paramsAsString) + ")";
    }
}

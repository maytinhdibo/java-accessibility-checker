package data;

import config.StringConstant;

import java.util.ArrayList;
import java.util.List;

public class MemberWithParams extends Member {
    protected List<DataType> params = new ArrayList<>();

    public MemberWithParams(String name, StringConstant accessModifier) {
        super(name, accessModifier);
    }

    public MemberWithParams(MemberWithParams memberWithParams) {
        super(memberWithParams.getName(), memberWithParams.getAccessModifier());
        this.setParams(memberWithParams.getParams());
        this.setParentClass(memberWithParams.getParentClass());
        this.setOriginClass(memberWithParams.getOriginClass());
    }

    public List<DataType> getParams() {
        return params;
    }

    public void addParam(DataType param) {
        this.params.add(param);
    }

    public void setParams(List<DataType> params) {
        this.params = params;
    }


    @Override
    public String toString() {
        List<String> paramsAsString = new ArrayList<>();
        params.forEach(param -> {
            paramsAsString.add(param.toString());
        });
        return accessModifier.toString() + " " + super.name + "(" + String.join(", ", paramsAsString) + ")";
    }
}

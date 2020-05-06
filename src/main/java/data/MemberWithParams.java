package data;

import config.StringConstant;

import java.util.ArrayList;
import java.util.List;

public class MemberWithParams extends Member {
    protected List<DataType> params = new ArrayList<>();
    public MemberWithParams(String name, StringConstant accessModifier) {
        super(name, accessModifier);
    }
    public List<DataType> getParams() {
        return params;
    }

    public void addParam(DataType param) {
        this.params.add(param);
    }

    @Override
    public String toString() {
        List<String> paramsAsString = new ArrayList<>();
        params.forEach(param -> {
            paramsAsString.add(param.toString());
        });
        return super.name + "(" + String.join(", ", paramsAsString) + ")";
    }
}

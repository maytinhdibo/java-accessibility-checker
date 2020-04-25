package data;

import config.StringConstant;

import java.util.ArrayList;
import java.util.List;

public class MethodMember extends Member{
    private List<String> params = new ArrayList<>();
    public MethodMember(String name, DataType type, StringConstant accessLevel) {
        super(name, type, accessLevel);
    }

    public List<String> getParams() {
        return params;
    }

    public void addParam(String param) {
        this.params.add(param);
    }
}

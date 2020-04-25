package data;

import java.util.ArrayList;
import java.util.List;

class MethodMember extends Member{
    private List<String> params = new ArrayList<>();
    public MethodMember(String name, String type, String accessLevel) {
        super(name, type, accessLevel);
    }

    public List<String> getParams() {
        return params;
    }

    public void addParam(String param) {
        this.params.add(param);
    }
}

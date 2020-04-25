package data;

import java.util.ArrayList;
import java.util.List;

public class ModelClass {
    private String packageName;
    private String classId;
    private List<MethodMember> members = new ArrayList<>();

    public ModelClass(String packageName, String classId) {
        this.packageName = packageName;
        this.classId = classId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public List<MethodMember> getMembers() {
        return members;
    }

    public void addMember(MethodMember method){
        this.members.add(method);
    }
}

class FeildMember extends Member{
    public FeildMember(String name, String type, String accessLevel) {
        super(name, type, accessLevel);
    }
}

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

class Member{
    private String name;
    private String type;
    private String accessLevel;

    public Member(String name, String type, String accessLevel) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}
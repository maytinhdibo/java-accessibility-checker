package data;

import config.StringConstant;

import java.util.ArrayList;
import java.util.List;

public class ClassModel {
    private String packageName;
    private String classId;
    private boolean isInterface;
    private StringConstant modifier;
    private List<MethodMember> members = new ArrayList<>();

    public ClassModel(String packageName, String classId, boolean isInterface, StringConstant modifier) {
        this.packageName = packageName;
        this.classId = classId;
        this.isInterface = isInterface;
        this.modifier = modifier;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public StringConstant getModifier() {
        return modifier;
    }

    public void setModifier(StringConstant modifier) {
        this.modifier = modifier;
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



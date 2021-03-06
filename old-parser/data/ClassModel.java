package data;

import config.StringConstant;

import java.util.ArrayList;
import java.util.List;

public class ClassModel implements Cloneable {
    private String packageName;
    private String classId;
    private boolean isInterface;
    private StringConstant accessModifier;
    private List<Member> members = new ArrayList<>();
    private List<String> genericTypes = new ArrayList<>();
    private ClassModel classExtended;
    private List<String> interfaces = new ArrayList<>();

    public ClassModel(String packageName, String classId, boolean isInterface, StringConstant accessModifier) {
        this.packageName = packageName;
        this.classId = classId;
        this.isInterface = isInterface;
        this.accessModifier = accessModifier;
    }

    public ClassModel getClassExtended() {
        return classExtended;
    }

    public void setClassExtended(ClassModel classExtended) {
        this.classExtended = classExtended;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public StringConstant getAccessModifier() {
        return accessModifier;
    }

    public void setAccessModifier(StringConstant accessModifier) {
        this.accessModifier = accessModifier;
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

    public List<Member> getMembers() {
        return members;
    }

    public List<String> getGenericTypes() {
        return genericTypes;
    }

    public void addGenericType(String genericType) {
        this.genericTypes.add(genericType);
    }

    public void addMember(Member method) {
        this.members.add(method);
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void addInterface(String interfaceId) {
        interfaces.add(interfaceId);
    }

    @Override
    public String toString() {
        return classId;
    }

    public ClassModel clone() {
        ClassModel classCloned = new ClassModel(packageName, classId, isInterface, accessModifier);
        members.forEach(member -> {
            classCloned.addMember(member);
        });
        genericTypes.forEach(genericType -> {
            classCloned.addGenericType(genericType);
        });
        return classCloned;
    }
}



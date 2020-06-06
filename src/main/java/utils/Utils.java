package utils;

import config.StringConstant;

import java.util.Arrays;

public class Utils {
    public static void main(String arg[]) {
    }

    public static String normalizeId(String id) {
        return id.replaceAll("\\<.*\\>(\\(.*\\))?(\\{.*\\})?", "");
    }

    public static int getValueAccessModifier(StringConstant accessModifier) {
        switch (accessModifier) {
            case PRIVATE:
                return 0;
            case PROTECTED:
                return 2;
            case PUBLIC:
                return 3;
            default:
                return 1;
        }
    }

    public static String getPackageName(String classId) {
        String[] classIdArr = classId.split("\\.");
        if (classIdArr.length == 0) return classId;
        classIdArr = Arrays.copyOf(classIdArr, classIdArr.length - 1);
        String classPackageName = String.join(".", classIdArr);
        return classPackageName;
    }

    public static boolean checkVisibleMember(StringConstant accessModifier, String classId, String memberClassId, boolean isExtended) {
        String classPackageName = null;
        String memberPackageName = null;

        classPackageName = getPackageName(classId);
        memberPackageName = getPackageName(memberClassId);

        if (memberClassId.equals(classId)) {
            //class declare
            return true;
        }
        if (classPackageName.equals(memberPackageName)) {
            //same package
            if (classPackageName != memberPackageName) {
                if (accessModifier == StringConstant.PRIVATE) return false;
            }
        } else if (isExtended) {
            //super class
            if (memberPackageName != classPackageName) {
                if (getValueAccessModifier(accessModifier) < 2) return false;
            }
        } else {
            if (memberPackageName != classPackageName) {
                if (accessModifier != StringConstant.PUBLIC) return false;
            }
        }
        return true;
    }
}

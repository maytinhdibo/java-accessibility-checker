package parser;

import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import config.StringConstant;
import data.ClassModel;
import data.DataType;
import data.MethodMember;

import java.util.Arrays;

public class ClassParser {
    protected ClassModel classModel = null;
    protected MethodMember curMethod = null;
    protected String packageName;
    protected ProjectParser projectParser;

    public boolean resolveGenericType(String name) {
        if (classModel.getGenericTypes().contains(name)) return true;
        if (curMethod != null && curMethod.getGenericTypes().contains(name)) return true;
        return false;
    }

    //parse generic type
    protected void parseGenericType() {
    }

    public DataType parseType(Type type) {
        return parseType(type, false);
    }

    public DataType parseType(Type type, boolean isArray) {
        if (type instanceof ArrayType) return parseType(((ArrayType) type).getComponentType(), true);

        if (type.isPrimitiveType()) {
            return new DataType(type.toString(), true, isArray);
        } else if (type instanceof VoidType) {
            DataType voidType = new DataType(StringConstant.VOID.toString(), true, false);
            voidType.setVoid(true);
            return voidType;
        } else {
            String typeId = "";
            String typeName = type.toString();

            boolean isGenericType = false;
            try {
                typeId = type.resolve().asReferenceType().getTypeDeclaration().getId();
            } catch (UnsupportedOperationException err) {
                if (resolveGenericType(typeName)) isGenericType = true;
                else throw err;
            }
            DataType result = new DataType(typeId, typeName, isArray);
            if (isGenericType) result.setGenericType(true);
            if (type instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
                result.setName(classOrInterfaceType.getNameAsString());
                if (classOrInterfaceType.getTypeArguments().isPresent()) {
                    classOrInterfaceType.getTypeArguments().get().forEach(typeArg -> {
                        if (typeArg instanceof Type) result.addTypeArg(parseType(typeArg, false));
                    });
                }
            }
            return result;
        }
    }

    public static StringConstant getAccessModifier(String word) {
        switch (word.toLowerCase()) {
            case "public":
                return StringConstant.PUBLIC;
            case "private":
                return StringConstant.PRIVATE;
            case "protected":
                return StringConstant.PROTECTED;
            default:
                return StringConstant.DEFAULT;
        }
    }

    public static String getPackageName(String classId) {
        String[] classIdArr = classId.split("\\.");
        if (classIdArr.length == 0) return classId;
        classIdArr = Arrays.copyOf(classIdArr, classIdArr.length - 1);
        String classPackageName = String.join(".", classIdArr);
        return classPackageName;
    }

    public static boolean checkVisibleMember(StringConstant accessModifier, String classId, String memberClassId, String extendedId) {
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
        } else if (extendedId.equals(memberClassId)) {
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
}

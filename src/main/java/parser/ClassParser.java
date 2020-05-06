package parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import config.StringConstant;
import data.*;
import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static parser.Parser.classListModel;

public class ClassParser {
    private List<ClassModel> listClassModel = new ArrayList<>();
    private Object b;

    public static void parseMember(ClassModel classModel, ClassOrInterfaceDeclaration klass) {
        klass.getMembers().forEach(member -> {
            if (member instanceof FieldDeclaration) {
                parseField(classModel, (FieldDeclaration) member);
            } else if (member instanceof MethodDeclaration) {
                parseMethod(classModel, (MethodDeclaration) member);
            } else if (member instanceof ConstructorDeclaration) {
                parseConstructor(classModel, (ConstructorDeclaration) member);
            } else {
                Log.error(member.toString() + ": Member of class is not support");
            }
        });
    }

    public static void parseField(ClassModel classModel, FieldDeclaration field) {
        StringConstant accessModifier = parseAccessModifier(field.getModifiers());
        field.getVariables().forEach(variable -> {
            String name = variable.getName().toString();
            Type type = variable.getType();
            DataType dataType = parseType(type);
            FieldMember fieldMember = new FieldMember(name, dataType, accessModifier);
            classModel.addMember(fieldMember);
        });
    }


    public static void parseConstructor(ClassModel classModel, ConstructorDeclaration constructor) {
        StringConstant accessModifier = parseAccessModifier(constructor.getModifiers());
        String name = constructor.getNameAsString();
        ConstructorMember constructorMember = new ConstructorMember(name, accessModifier);
        constructor.getParameters().forEach(parameter -> {
            DataType paramType = parseType(parameter.getType());
            constructorMember.addParam(paramType);
        });
        classModel.addMember(constructorMember);
    }

    public static void parseMethod(ClassModel classModel, MethodDeclaration method) {
        StringConstant accessModifier = parseAccessModifier(method.getModifiers());
        Type type = method.getType();
        String name = method.getName().toString();
        DataType dataType = parseType(type, false);
        MethodMember methodMember = new MethodMember(name, dataType, accessModifier);
        method.getParameters().forEach(parameter -> {
            DataType paramType = parseType(parameter.getType());
            methodMember.addParam(paramType);
        });
        classModel.addMember(methodMember);
    }

    public static DataType parseType(Type type) {
        return parseType(type, false);
    }

    public static DataType parseType(Type type, boolean isArray) {
        if (type instanceof ArrayType) return parseType(((ArrayType) type).getComponentType(), true);

        if (type.isPrimitiveType()) {
            return new DataType(type.toString(), true, isArray);
        } else if (type instanceof VoidType) {
            DataType voidType = new DataType(StringConstant.VOID.toString(), true, false);
            voidType.setVoid(true);
            return voidType;
        } else {
            String typeId = type.resolve().asReferenceType().getTypeDeclaration().getId();
            String typeName = type.toString();
            DataType result = new DataType(typeId, typeName, isArray);
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

    public static StringConstant parseAccessModifier(NodeList modifiers) {
        if (modifiers.size() != 0) {
            for (Object item : modifiers) {
                Modifier modifier = (Modifier) item;
                switch (modifier.getKeyword().name().toLowerCase()) {
                    case "public":
                        return StringConstant.PUBLIC;
                    case "private":
                        return StringConstant.PRIVATE;
                    case "protected":
                        return StringConstant.PROTECTED;
                    default:
                        break;
                }
            }
        }
        return StringConstant.DEFAULT;
    }

    public void parse(CompilationUnit cuFile) {
        String packageName = cuFile.getPackageDeclaration().get().getName().toString();
        Log.write("Package: " + packageName);
        cuFile.findAll(ClassOrInterfaceDeclaration.class).forEach(klass -> {
            String classId = packageName.length() > 0 ? String.join(".", Arrays.asList(packageName, klass.getNameAsString())) : klass.getNameAsString();
            Boolean isInterface = klass.isInterface();
            StringConstant accessModifier = parseAccessModifier(klass.getModifiers());
            ClassModel classModel = new ClassModel(packageName, classId, isInterface, accessModifier);
            System.out.println(classModel.getClassId());
            System.out.println(classModel.getClassId());
            classListModel.put(classId, classModel);
            parseMember(classModel, klass);
        });
    }

}

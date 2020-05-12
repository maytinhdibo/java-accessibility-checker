package parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedVoidType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import config.StringConstant;
import data.ClassModel;
import data.DataType;
import data.MethodMember;

import java.util.*;

public class ReflectionClassParser extends ClassParser {
    protected void parseGenericType(List<ResolvedTypeParameterDeclaration> genericTypes) {
        genericTypes.forEach(type -> {
            classModel.addGenericType(type.getName());
        });
    }


    private void parseMethods(Set<MethodUsage> methods) {
        Object[] methodArr = methods.toArray();
        for (Object methodUsage : methodArr) {
            if (methodUsage instanceof MethodUsage) {
                Object method = ((MethodUsage) methodUsage).getDeclaration();
                if (method instanceof ReflectionMethodDeclaration) {
                    parseMethod((ReflectionMethodDeclaration) method);
                }else if (method instanceof JavaParserMethodDeclaration){
                    parseMethod((JavaParserMethodDeclaration) method);
                }
            }
        }

    }

    private void parseMethod(JavaParserMethodDeclaration method) {
        String name = method.getName();
        StringConstant accessModifier = getAccessModifier(method.accessSpecifier().asString());
        MethodMember methodMember = new MethodMember(name, null, accessModifier);

        method.getTypeParameters().forEach(genericType -> {
            methodMember.addGenericType(genericType.getName());
        });
        curMethod = methodMember;

        DataType dataType = parseType(method.getReturnType());
        methodMember.setType(dataType);


        for (int i = 0; i < method.getNumberOfParams(); i++) {
            ResolvedType type = method.getParam(i).getType();
            parseType(type);
        }

        curMethod = null;
        classModel.addMember(methodMember);
        methodMember.setParentClass(classModel);
    }

    private void parseMethod(ReflectionMethodDeclaration method) {
        String name = method.getName();
        StringConstant accessModifier = getAccessModifier(method.accessSpecifier().asString());
        MethodMember methodMember = new MethodMember(name, null, accessModifier);

        method.getTypeParameters().forEach(genericType -> {
            methodMember.addGenericType(genericType.getName());
        });
        curMethod = methodMember;

        DataType dataType = parseType(method.getReturnType());
        methodMember.setType(dataType);

        for (int i = 0; i < method.getNumberOfParams(); i++) {
            ResolvedType type = method.getParam(i).getType();
            parseType(type);
        }

        curMethod = null;
        classModel.addMember(methodMember);
        methodMember.setParentClass(classModel);
    }

    public DataType parseType(ResolvedType type) {
        return parseType(type, false);
    }

    public DataType parseType(ResolvedType type, boolean isArray) {
        if (type instanceof ResolvedArrayType) return parseType(((ResolvedArrayType) type).getComponentType(), true);

        if (type.isPrimitive()) {
            return new DataType(type.toString(), true, isArray);
        } else if (type instanceof ResolvedVoidType) {
            DataType voidType = new DataType(StringConstant.VOID.toString(), true, false);
            voidType.setVoid(true);
            return voidType;
        } else {
            String typeId = "";
            String typeName =type.describe();

            boolean isGenericType = false;
            try {
                typeId = type.asReferenceType().getTypeDeclaration().getId();
                typeName = typeId;
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


    public ClassModel parse(JavaParserClassDeclaration resolveClass, ProjectParser projectParser) {
        String packageName = resolveClass.getPackageName();
        String classId = resolveClass.getId();
        Boolean isInterface = resolveClass.isInterface();

        StringConstant accessModifier = getAccessModifier(resolveClass.accessSpecifier().asString());
        classModel = new ClassModel(packageName, classId, isInterface, accessModifier);

        //parse generic type
        parseGenericType(resolveClass.getTypeParameters());


//        Object b = resolveClass.getAllFields();
//        Object a = resolveClass.getDeclaredMethods();
//        Object c = resolveClass.getDeclaredFields();
        Object d = resolveClass.getAllMethods();

        parseMethods(resolveClass.getAllMethods());


        projectParser.addClass(classModel);
        return classModel;
    }

}

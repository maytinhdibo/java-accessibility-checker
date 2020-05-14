
package parser;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedVoidType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import com.github.javaparser.utils.Pair;
import config.StringConstant;
import data.*;

import java.util.*;

public class ReflectionClassParser extends ClassParser {
    protected void parseGenericType(List<ResolvedTypeParameterDeclaration> genericTypes) {
        genericTypes.forEach(type -> {
            classModel.addGenericType(type.getName());
        });
    }


    protected JavaParserClassDeclaration resolveClass;

    private void parseFields(List<ResolvedFieldDeclaration> fields) {
        fields.forEach(field -> parseField(field));
    }

    public void parseField(ResolvedFieldDeclaration field) {
        StringConstant accessModifier = getAccessModifier(field.accessSpecifier().asString());

        String memberClassId = field.declaringType().getPackageName() + "." + field.declaringType().getClassName();
        boolean visible = checkVisibleMember(accessModifier, resolveClass.getId(), memberClassId, resolveClass.getSuperClass().getId());

        if (!visible) return;

        String name = field.getName();
        ResolvedType type = field.getType();
        DataType dataType = parseType(type);
        FieldMember fieldMember = new FieldMember(name, dataType, accessModifier);
        fieldMember.setParentClass(classModel);
        classModel.addMember(fieldMember);
    }

    private void parseMethods(Set<MethodUsage> methods) {
        Object[] methodArr = methods.toArray();
        for (Object methodUsage : methodArr) {
            if (methodUsage instanceof MethodUsage) {
                Object method = ((MethodUsage) methodUsage).getDeclaration();
                if (method instanceof ReflectionMethodDeclaration) {
                    parseMethod((ReflectionMethodDeclaration) method);
                } else if (method instanceof JavaParserMethodDeclaration) {
                    parseMethod((JavaParserMethodDeclaration) method);
                }
            }
        }
    }

    private void parseMethod(JavaParserMethodDeclaration method) {
        String name = method.getName();
        StringConstant accessModifier = getAccessModifier(method.accessSpecifier().asString());

        String memberClassId = method.getPackageName() + "." + method.getClassName();
        boolean visible = checkVisibleMember(accessModifier, resolveClass.getId(), memberClassId, resolveClass.getSuperClass().getId());

        if (!visible) return;

        MethodMember methodMember = new MethodMember(name, null, accessModifier);

        method.getTypeParameters().forEach(genericType -> {
            methodMember.addGenericType(genericType.getName());
        });
        curMethod = methodMember;

        try {
            DataType dataType = parseType(method.getReturnType());
            methodMember.setType(dataType);
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (int i = 0; i < method.getNumberOfParams(); i++) {
            ResolvedType type = method.getParam(i).getType();
            try {
                methodMember.addParam(parseType(type));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        curMethod = null;
        classModel.addMember(methodMember);
        methodMember.setParentClass(classModel);
    }

    private void parseMethod(ReflectionMethodDeclaration method) {
        String name = method.getName();
        StringConstant accessModifier = getAccessModifier(method.accessSpecifier().asString());

        if (getValueAccessModifier(accessModifier) < 3) {
            return;
        }

        MethodMember methodMember = new MethodMember(name, null, accessModifier);

        method.getTypeParameters().forEach(genericType -> {
            methodMember.addGenericType(genericType.getName());
        });
        curMethod = methodMember;

        DataType dataType = parseType(method.getReturnType());
        methodMember.setType(dataType);

        for (int i = 0; i < method.getNumberOfParams(); i++) {
            ResolvedType type = method.getParam(i).getType();
            methodMember.addParam(parseType(type));
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

        String typeName = type.describe();

        if (type.isPrimitive()) {
            return new DataType(typeName, true, isArray);
        } else if (type instanceof ResolvedVoidType) {
            DataType voidType = new DataType(StringConstant.VOID.toString(), true, false);
            voidType.setVoid(true);
            return voidType;
        } else {
            String typeId = "";

            boolean isGenericType = false;
            String parseFrom = null;
            try {
                typeId = type.asReferenceType().getTypeDeclaration().getId();
//                typeName = typeId;
            } catch (UnsupportedOperationException err) {
                if (resolveGenericType(typeName) && type.asTypeParameter().getId().equals(classModel.getClassId() + "." + typeName)) {
                    isGenericType = true;
                } else {
//                    if (resolveClass.getClassName().equals("C")) {}
                    ResolvedExtendedGenericType resolveId = resolveExtendGenericType(resolveClass, type);
                    if (resolveId != null) {
                        if (resolveId.type == StringConstant.RESOLVED) typeId = resolveId.result;
                        if (resolveId.type == StringConstant.GENERIC) {
                            typeName = resolveId.result;
                            isGenericType = true;
                            parseFrom = resolveId.parseFrom;
                        }
                    } else throw err;
                }
            }
            DataType result = new DataType(typeId, typeName, isArray);
            if (parseFrom != null) result.setParseFrom(parseFrom);
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

    private Optional<ResolvedType> finded = null;
    private ResolvedClassDeclaration findedClass;

    public ResolvedExtendedGenericType resolveExtendGenericType(ResolvedClassDeclaration resolvedClassDeclaration, ResolvedType type) {
        if (resolvedClassDeclaration == null) return null;
        if (resolvedClassDeclaration.getSuperClass() != null) {
            Optional<ResolvedType> result = resolvedClassDeclaration.getSuperClass().getGenericParameterByName(type.describe());

            if (resolveClass.getClassName().equals("B") && type.describe().equals("K")) {
                if (result.isPresent()) {
                    Object b = result.get().describe();
                    System.out.println("b");
                }
                System.out.println("a");
            }

            if (result.isPresent() && (result.get().isReference() || result.get().asTypeParameter().getId().equals(type.asTypeParameter().getId()))) {
                if (result.get().isReferenceType()) {
//                    if (resolveClass.getClassName().equals("C") && type.describe().equals("V")) {
//                        System.out.println("a");
//                    }
                    Object re = result.get().describe();
                    String resolve = result.get().asReferenceType().describe();
                    return new ResolvedExtendedGenericType(StringConstant.RESOLVED, resolve, "");
                } else {
                    finded = result;
                    findedClass = resolvedClassDeclaration;
                    return resolveExtendGenericType(resolvedClassDeclaration.getSuperClass().getTypeDeclaration().asClass(), type);
                }
            } else {
                return resolveExtendGenericType(resolvedClassDeclaration.getSuperClass().getTypeDeclaration().asClass(), type);
            }
        }
        if (finded != null) {
            Optional<ResolvedType> result = finded;
            //khác class trả về type gốc
//            Object c = result.get().is;
            finded = null;
            if (!findedClass.getId().equals(resolveClass.getId())) {
                return new ResolvedExtendedGenericType(StringConstant.GENERIC, type.describe(), findedClass.getSuperClass().getId());
            } else {
                return new ResolvedExtendedGenericType(StringConstant.GENERIC, result.get().describe(), findedClass.getId());
            }
        }
        return null;
    }

    public ReflectionClassParser(JavaParserClassDeclaration resolveClass, ProjectParser projectParser) {
        this.resolveClass = resolveClass;
        this.projectParser = projectParser;
    }

    public ClassModel parse() {
        packageName = resolveClass.getPackageName();
        String classId = resolveClass.getId();
        Boolean isInterface = resolveClass.isInterface();

        StringConstant accessModifier = getAccessModifier(resolveClass.accessSpecifier().asString());
        classModel = new ClassModel(packageName, classId, isInterface, accessModifier);

        //parse generic type
        parseGenericType(resolveClass.getTypeParameters());

        parseMethods(resolveClass.getAllMethods());
        parseFields(resolveClass.getAllFields());

        projectParser.addClass(classModel);
        return classModel;
    }

}

package parser;

import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.*;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import com.github.javaparser.utils.Pair;
import config.StringConstant;
import data.*;

import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
                    parseFrom = classModel.getClassId();
                } else {
                    if (resolveClass.getClassName().equals("C") && type.describe().equals("K")) {
                        System.out.println("a");
                    }
                    ResolvedExtendedGenericType resolveId = resolveExtendGenericType(resolveClass, type);
                    if (resolveId != null) {
                        if (resolveId.type == StringConstant.RESOLVED) {
                            return parseType(resolveId.resolvedResult);
                        }
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

//            if (result.getName().equals("java.util.List<java.lang.String>")) {
//                List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> c = type.asReferenceType().getTypeParametersMap();
//                System.out.println("one");
//            }

            if (type.isReferenceType()) {
                try {
                    type.asReferenceType().getTypeParametersMap().forEach(param -> {
                        if (!(param.b instanceof ResolvedWildcard)) {
                            System.out.println("none");
                            DataType paramType = parseType(param.b);
                            result.addTypeArg(paramType);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }

    private Optional<ResolvedType> finded = null;
    private ResolvedReferenceType findedClass;

    public ResolvedExtendedGenericType resolveExtendGenericType(ResolvedClassDeclaration resolvedClassDeclaration, ResolvedType type) {
        if (resolvedClassDeclaration == null) return null;

        List<ResolvedReferenceType> listExtended = resolvedClassDeclaration.getAllSuperClasses();
        for (ResolvedReferenceType klass : listExtended) {
            Optional<ResolvedType> result = klass.getGenericParameterByName(type.describe());
            if (result.isPresent() && (result.get().isReference() || result.get().asTypeParameter().getId().equals(type.asTypeParameter().getId()))) {
                if (result.get().isReferenceType()) {
                    return new ResolvedExtendedGenericType(StringConstant.RESOLVED, result.get(), "");
                } else {
                    finded = result;
                    findedClass = klass;
                }
            }
        }

        if (finded != null) {
            Optional<ResolvedType> result = finded;
            //findedClass tìm thấy ở đâu, ví dụ: xét G thì K được tìm thấy tại B{<M>}
            //result generic type tìm thấy

            //Trong trường hợp tìm thấy generic type map với một class extend nhưng không phải là một referenceType
            //Nếu GenericType tìm thấy nằm trong class -> Trả về kết quả resolve và class
            //Nếu GenericType tìm thấy nằm ở class khác -> giữ nguyên type và trả về class gốc chứa nó.

            finded = null;

            if (getPackageName(result.get().asTypeVariable().qualifiedName()).equals(classModel.getClassId())) {
                //Resolve and case
                return new ResolvedExtendedGenericType(StringConstant.GENERIC, result.get().describe(), classModel.getClassId());
            } else {
                //K and stock
                return new ResolvedExtendedGenericType(StringConstant.GENERIC, type.describe(), findedClass.getId());
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
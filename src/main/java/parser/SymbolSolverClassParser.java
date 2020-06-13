package parser;

import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.types.*;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.logic.AbstractClassDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionMethodDeclaration;
import config.StringConstant;
import data.*;
import utils.Utils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SymbolSolverClassParser extends ClassParser {

    private void parseConstructors(List<ResolvedConstructorDeclaration> constructors) {
        constructors.forEach(constructor -> parseConstructor(constructor));
    }

    public void parseConstructor(ResolvedConstructorDeclaration constructor) {
        StringConstant accessModifier = getAccessModifier(constructor.accessSpecifier().asString());

        String name = constructor.getName();

        ConstructorMember constructorMember = new ConstructorMember(name, accessModifier);

        for (int i = 0; i < constructor.getNumberOfParams(); i++) {
            ResolvedType type = constructor.getParam(i).getType();
            try {
                constructorMember.addParam(parseType(type));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        constructorMember.setParentClass(classModel);
        constructorMember.setOriginClass(constructor.declaringType().getId());

        classModel.addMember(constructorMember);
    }

    private void parseFields(List<ResolvedFieldDeclaration> fields) {
        fields.forEach(field -> parseField(field));
    }

    public void parseField(ResolvedFieldDeclaration field) {
        StringConstant accessModifier = getAccessModifier(field.accessSpecifier().asString());

        String memberClassId = field.declaringType().getPackageName() + "." + field.declaringType().getClassName();

        boolean visible = Utils.checkVisibleMember(accessModifier, reflectionClass.getId(), memberClassId, true);

        if (!visible) return;

        String name = field.getName();

        ResolvedType type = field.getType();

        DataType dataType = parseType(type);
        FieldMember fieldMember = new FieldMember(name, dataType, accessModifier);
        fieldMember.setStatic(field.isStatic());

        fieldMember.setParentClass(classModel);
        fieldMember.setOriginClass(field.declaringType().getId());

        classModel.addMember(fieldMember);
    }

    private void parseMethods(Set<MethodUsage> methods) {
        Object[] methodArr = methods.toArray();
        for (Object methodUsage : methodArr) {
            if (methodUsage instanceof MethodUsage) {
                Object method = ((MethodUsage) methodUsage).getDeclaration();
                if (method instanceof ReflectionMethodDeclaration) {
                    ReflectionMethodDeclaration reflectionMethodDeclaration = (ReflectionMethodDeclaration) method;
                    parseMethod((ReflectionMethodDeclaration) method);
                } else if (method instanceof JavaParserMethodDeclaration) {
                    JavaParserMethodDeclaration javaParserMethodDeclaration = (JavaParserMethodDeclaration) method;
                    parseMethod(javaParserMethodDeclaration);
                }
            }
        }
    }

    private void parseMethod(JavaParserMethodDeclaration method) {
        String name = method.getName();
        StringConstant accessModifier = getAccessModifier(method.accessSpecifier().asString());

        String memberClassId = method.getPackageName() + "." + method.getClassName();
        boolean visible = Utils.checkVisibleMember(accessModifier, reflectionClass.getId(), memberClassId, true);

        if (!visible) return;

        MethodMember methodMember = new MethodMember(name, null, accessModifier);

        methodMember.setStatic(method.isStatic());

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
        methodMember.setOriginClass(method.declaringType().getId());
        methodMember.setParentClass(classModel);
    }

    private void parseMethod(ReflectionMethodDeclaration method) {
        String name = method.getName();
        StringConstant accessModifier = getAccessModifier(method.accessSpecifier().asString());

        if (Utils.getValueAccessModifier(accessModifier) < 3) {
            return;
        }

        MethodMember methodMember = new MethodMember(name, null, accessModifier);

        method.getTypeParameters().forEach(genericType -> {
            methodMember.addGenericType(genericType.getName());
        });
        curMethod = methodMember;

        for (int i = 0; i < method.getNumberOfParams(); i++) {
            ResolvedType type = method.getParam(i).getType();
            methodMember.addParam(parseType(type));
        }

        DataType dataType = parseType(method.getReturnType());
        methodMember.setType(dataType);

        curMethod = null;
        classModel.addMember(methodMember);
        methodMember.setOriginClass(method.declaringType().getId());
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
            ParseFrom parseFrom = null;
            try {
                typeId = type.asReferenceType().getTypeDeclaration().getId();
//                typeName = typeId;
            } catch (UnsupportedOperationException err) {
//                err.printStackTrace();
                if (type.asTypeParameter().declaredOnMethod() || type.asTypeParameter().declaredOnConstructor()) {
                    typeId = type.describe();
                    parseFrom = new ParseFrom(true);
                } else if (resolveGenericType(typeName) && type.asTypeParameter().getId().equals(classModel.getClassId() + "." + typeName)) {
                    isGenericType = true;
                    parseFrom = new ParseFrom(classModel.getClassId());
                } else {
                    ResolvedExtendedGenericType resolveId = resolveExtendGenericType(reflectionClass, type);
                    if (resolveId != null) {
                        if (resolveId.type == StringConstant.RESOLVED) {
                            return parseType(resolveId.resolvedResult);
                        }
                        if (resolveId.type == StringConstant.GENERIC) {
                            typeName = resolveId.result;
                            isGenericType = true;
                            parseFrom = new ParseFrom(resolveId.parseFrom);
                        }
                    } else {
                        typeName = "Object";
                        typeId = "java.lang.Object";
                        parseFrom = null;
                    }
                }
            }


            DataType result = new DataType(typeId, typeName, isArray);
            if (parseFrom != null) result.setParseFrom(parseFrom);
            if (isGenericType) result.setGenericType(true);

            if (type.isReferenceType()) {
                try {
                    type.asReferenceType().getTypeParametersMap().forEach(param -> {
                        if (!(param.b instanceof ResolvedWildcard)) {
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

    private ResolvedType finded = null;
    private ResolvedReferenceType findedClass;

    public ResolvedExtendedGenericType resolveExtendGenericType(AbstractClassDeclaration resolvedClassDeclaration, ResolvedType type) {
        if (resolvedClassDeclaration == null) return null;

        List<ResolvedReferenceType> listExtended = resolvedClassDeclaration.getAllSuperClasses();
        for (ResolvedReferenceType klass : listExtended) {
            Optional<ResolvedType> result = klass.getGenericParameterByName(type.describe());

            if (result.isPresent() && (result.get().isReference() || result.get().asTypeParameter().getId().equals(type.asTypeParameter().getId()))) {
                if (result.get().isReferenceType()) {
                    return new ResolvedExtendedGenericType(StringConstant.RESOLVED, result.get(), "");
                } else {
                    finded = result.get();
                    findedClass = klass;
                }
            }
        }

        if (finded != null) {
            ResolvedType result = finded;

            //findedClass tìm thấy ở đâu, ví dụ: xét G thì K được tìm thấy tại B{<M>}
            //result generic type tìm thấy

            //Trong trường hợp tìm thấy generic type map với một class extend nhưng không phải là một referenceType
            //Nếu GenericType tìm thấy nằm trong class -> Trả về kết quả resolve và class
            //Nếu GenericType tìm thấy nằm ở class khác -> giữ nguyên type và trả về class gốc chứa nó.

            finded = null;

            if (Utils.getPackageName(result.asTypeVariable().qualifiedName()).equals(classModel.getClassId())) {
                //Resolve and case
                return new ResolvedExtendedGenericType(StringConstant.GENERIC, result.describe(), classModel.getClassId());
            } else {
                //K and stock
                return new ResolvedExtendedGenericType(StringConstant.GENERIC, type.describe(), findedClass.getId());
            }

        }
        return null;
    }

    AbstractClassDeclaration reflectionClass;

    public SymbolSolverClassParser(AbstractClassDeclaration reflectionClass, ProjectParser projectParser) {
        this.reflectionClass = reflectionClass;
        this.projectParser = projectParser;
    }

    public ClassModel parse() {
        String classId = reflectionClass.getId();
        if (projectParser.classListModel.get(classId) != null) {
            return null;
        }
        packageName = reflectionClass.getPackageName();

        boolean isInterface = reflectionClass.isInterface();

        StringConstant accessModifier = getAccessModifier(reflectionClass.accessSpecifier().asString());
        classModel = new ClassModel(packageName, classId, isInterface, accessModifier);

        if (reflectionClass.getInterfaces().size() > 0) {
            reflectionClass.getInterfaces().forEach(type -> {
                classModel.addInterface(type.getId());
            });
        }

        ResolvedType superClass = reflectionClass.getSuperClass();

        if (superClass != null) {
            projectParser.parseClass(superClass.describe());
            classModel.setClassExtended(superClass.describe());
        }
        //parse generic type
        parseGenericType(reflectionClass.getTypeParameters());

        parseMethods(reflectionClass.getAllMethods());
        parseFields(reflectionClass.getVisibleFields());
        parseConstructors(reflectionClass.getConstructors());

        projectParser.addClass(classModel);

        return classModel;
    }

}
package parser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import config.StringConstant;
import data.*;
import utils.Log;

import java.util.Arrays;

public class ProjectClassParser extends ClassParser {


    protected ClassOrInterfaceDeclaration klass;

    public ProjectClassParser(ClassOrInterfaceDeclaration klass, String packageName, ProjectParser projectParser) {
        this.klass = klass;
        this.packageName = packageName;
        this.projectParser = projectParser;
    }

    public void parseMember(ClassOrInterfaceDeclaration klass) {
        klass.getMembers().forEach(member -> {
            if (member instanceof FieldDeclaration) {
                parseField((FieldDeclaration) member);
            } else if (member instanceof MethodDeclaration) {
                parseMethod((MethodDeclaration) member);
            } else if (member instanceof ConstructorDeclaration) {
                parseConstructor((ConstructorDeclaration) member);
            } else {
                Log.error(member.toString() + ": Member of class is not support");
                return;
            }
        });
    }

    public void parseField(FieldDeclaration field) {
        StringConstant accessModifier = parseAccessModifier(field.getModifiers());
        field.getVariables().forEach(variable -> {
            String name = variable.getName().toString();
            Type type = variable.getType();
            DataType dataType;
            try {
                dataType = parseType(type);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            FieldMember fieldMember = new FieldMember(name, dataType, accessModifier);
            fieldMember.setParentClass(classModel);
            classModel.addMember(fieldMember);
        });
    }


    public void parseConstructor(ConstructorDeclaration constructor) {
        StringConstant accessModifier = parseAccessModifier(constructor.getModifiers());
        String name = constructor.getNameAsString();
        ConstructorMember constructorMember = new ConstructorMember(name, accessModifier);
        constructor.getParameters().forEach(parameter -> {
            try{
                DataType paramType = parseType(parameter.getType());
                constructorMember.addParam(paramType);
            }catch (Exception e){
                return;
            }
        });
        constructorMember.setParentClass(classModel);
        classModel.addMember(constructorMember);
    }

    public void parseMethod(MethodDeclaration method) {
        StringConstant accessModifier = parseAccessModifier(method.getModifiers());
        Type type = method.getType();
        String name = method.getName().toString();
        MethodMember methodMember = new MethodMember(name, null, accessModifier);
        //parse generic type
        method.getTypeParameters().forEach(genericType -> {
            methodMember.addGenericType(genericType.asString());
        });

        curMethod = methodMember;
        DataType dataType;
        try {
            dataType = parseType(type);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        methodMember.setType(dataType);
        method.getParameters().forEach(parameter -> {
            try {
                DataType paramType = parseType(parameter.getType());

                methodMember.addParam(paramType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        curMethod = null;
        methodMember.setParentClass(classModel);

        classModel.addMember(methodMember);
    }

    public static StringConstant parseAccessModifier(NodeList modifiers) {
        if (modifiers.size() != 0) {
            for (Object item : modifiers) {
                Modifier modifier = (Modifier) item;
                StringConstant result = getAccessModifier(modifier.getKeyword().name());
                if (result != StringConstant.DEFAULT) {
                    return result;
                }
            }
        }
        return StringConstant.DEFAULT;
    }

    private void parseGenericType(NodeList<TypeParameter> typeParameters) {
        typeParameters.forEach(genericType -> {
            classModel.addGenericType(genericType.asString());
        });
    }

    public ClassModel parse() {
        //parse resolve
        try {
            Object resolveClass = klass.resolve();
            if (resolveClass instanceof JavaParserClassDeclaration)
                return new SymbolSolverClassParser((JavaParserClassDeclaration) resolveClass, projectParser).parse();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        String classId = packageName.length() > 0 ? String.join(".", Arrays.asList(packageName, klass.getNameAsString())) : klass.getNameAsString();
        boolean isInterface = klass.isInterface();

        StringConstant accessModifier = parseAccessModifier(klass.getModifiers());
        classModel = new ClassModel(packageName, classId, isInterface, accessModifier);

        if (klass.getImplementedTypes().size() > 0) {
            klass.getImplementedTypes().forEach(type -> {
                try {
                    classModel.addInterface(type.resolve().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        //parse generic type
        parseGenericType(klass.getTypeParameters());

        projectParser.addClass(classModel);

        parseMember(klass);
        return classModel;
    }

}

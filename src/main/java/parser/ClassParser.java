package parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import config.StringConstant;
import data.ClassModel;
import data.DataType;
import data.FieldMember;
import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static parser.Parser.classListModel;

public class ClassParser {
    private List<ClassModel> listClassModel = new ArrayList<>();

    public static void parseMember(ClassModel classModel, ClassOrInterfaceDeclaration klass) {
        klass.getMembers().forEach(member -> {
            if (member instanceof FieldDeclaration) {
                parseField(classModel, (FieldDeclaration) member);
            } else if (member instanceof MethodDeclaration) {
                parseMethod(classModel, (MethodDeclaration) member);
            } else if (member instanceof ConstructorDeclaration) {
                parseConstructor(classModel, (ConstructorDeclaration) member);
            }else {
                Log.error(member.toString() + ": Member of class is not support");
            }
        });
    }

    public static void parseConstructor(ClassModel classModel, ConstructorDeclaration constructor) {

    }


    public static void parseField(ClassModel classModel, FieldDeclaration field) {
        StringConstant accessModifier = parseAccessModifier(field.getModifiers());
        field.getVariables().forEach(variable -> {
            String name = variable.getName().toString();
            Type type = variable.getType();
            DataType dataType;
            if (type.isPrimitiveType()) {
                dataType = new DataType(type.toString(), true);
            } else {
                dataType = new DataType(type.toString(), "needfill");
            }
            FieldMember fieldMember = new FieldMember(name, dataType, accessModifier);
            classModel.addMember(fieldMember);
        });
    }

    public static void parseMethod(ClassModel classModel, MethodDeclaration method) {

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
            String classId = String.join(".", Arrays.asList(packageName, klass.getNameAsString()));
            Boolean isInterface = klass.isInterface();
            StringConstant accessModifier = parseAccessModifier(klass.getModifiers());
            ClassModel classModel = new ClassModel(packageName, classId, isInterface, accessModifier);
            System.out.println(classModel.getClassId());
            classListModel.put(classId, classModel);
            parseMember(classModel, klass);
        });
    }

}

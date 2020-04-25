package parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import config.StringConstant;
import data.ClassModel;
import data.DataType;
import data.FieldMember;
import data.FileData;
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
            } else {
                Log.error(member.toString() + ": Member of class is not support");
            }
        });
    }

    public static void parseField(ClassModel classModel, FieldDeclaration field) {
        StringConstant modifier = parseModifier(field.getModifiers());
        field.getVariables().forEach(variable->{
            String name = variable.getName().toString();
            Type type = variable.getType();
            DataType dataType;
            if(type.isPrimitiveType()){
                dataType = new DataType(type.toString(), true);
            }else{
                dataType = new DataType(type.toString(), "needfill");
            }
            FieldMember fieldMember = new FieldMember(name, dataType, modifier);
            classModel.addMember(fieldMember);
        });
    }

    public static void parseMethod(ClassModel classModel, MethodDeclaration method) {

    }

    public static StringConstant parseModifier(NodeList modifiers) {
        if (modifiers.size() != 0) {
            switch (modifiers.get(0).toString()) {
                case "public":
                    return StringConstant.PUBLIC;
                case "private":
                    return StringConstant.PRIVATE;
                case "protected":
                    return StringConstant.PROTECTED;
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
            StringConstant modifier = parseModifier(klass.getModifiers());
            ClassModel classModel = new ClassModel(packageName, classId, isInterface, modifier);
            System.out.println(classModel.getClassId());
            classListModel.put(classId, classModel);
            parseMember(classModel, klass);
        });
    }

}

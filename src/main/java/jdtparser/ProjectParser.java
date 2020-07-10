package jdtparser;

import config.Config;
import data.Member;
import data.ClassModel;
import data.Variable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import utils.DirProcess;
import utils.FileProcess;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class ProjectParser {
    public HashMap<String, ClassModel> classListModel = new HashMap<>();

    private String projectDir;
    private String[] sourcePaths;
    private String[] classPaths;

    public static void main(String[] args) {
        System.out.println("Starting parse...");
        ProjectParser projectParser = new ProjectParser(Config.PROJECT_DIR, Config.SOURCE_PATH, Config.CLASS_PATH);

        File curFile = new File("/Users/maytinhdibo/Project/bomberman/src/com/carlosflorencio/bomberman/TestExtended.java");

        FileParser fileParser = new FileParser(projectParser, curFile, 1013);

        fileParser.parse();

        System.out.println("Parse done!");
    }

    public HashMap<String, ClassModel> getListAccess(ITypeBinding clazz) {
        HashMap<String, ClassModel> listAccess = new HashMap<>();
        for (Map.Entry<String, ClassModel> entry : classListModel.entrySet()) {
            String key = entry.getKey();
            ClassModel classModel = entry.getValue();

            if (clazz == classModel.getOrgType()) {
                listAccess.put(clazz.getKey(), classModel.clone());
                continue;
            } else {
                boolean extended = clazz.isSubTypeCompatible(classModel.getOrgType());

                String fromPackage = clazz.getPackage().getName();
                String toPackage = "-1";
                if (classModel.getOrgType().getPackage() != null) {
                    toPackage = classModel.getOrgType().getPackage().getName();
                }

                if (Utils.checkVisibleMember(classModel.getOrgType().getModifiers(), fromPackage, toPackage, extended)) {
                    ClassModel cloneModel = classModel.clone();
                    listAccess.put(key, cloneModel);
                    List<Member> toRemoves = new ArrayList<>();
                    String finalToPackage = toPackage;
                    classModel.getMembers().forEach(member -> {
                        if (!Utils.checkVisibleMember(member.getMember().getModifiers(), fromPackage, finalToPackage, extended)) {
                            toRemoves.add(member);
                        }
                    });
                    cloneModel.getMembers().removeAll(toRemoves);
                }
            }

        }

        return listAccess;
    }

    public ProjectParser(String projectDir, String[] sourcePaths, String[] classPaths) {
        this.projectDir = projectDir;
        this.sourcePaths = sourcePaths;
        this.classPaths = classPaths;
        parse();
    }

    public void parseClass(ITypeBinding iTypeBinding) {
        if (iTypeBinding == null) return;
        if (iTypeBinding.isPrimitive()) return;
        ClassModel classModel = new ClassModel(iTypeBinding);

        classListModel.put(classModel.getOrgType().getKey(), classModel);
    }

    public CompilationUnit createCU(File file) {
        ASTParser parser = ASTParser.newParser(AST.JLS13);//choose source code analyzing strategy
        parser.setResolveBindings(true);// turn on binding strategy
        parser.setKind(ASTParser.K_COMPILATION_UNIT);// the source code is a file .java
        parser.setBindingsRecovery(true);
        Hashtable<String, String> options = JavaCore.getOptions();

        JavaCore.setComplianceOptions(JavaCore.VERSION_13, options);


        parser.setCompilerOptions(options);
        parser.setEnvironment(classPaths, sourcePaths, new String[]{"UTF-8"}, true);

        parser.setUnitName(file.getName());
        parser.setSource(FileProcess.read(file).toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());
        return cu;
    }

    public void parse() {
        List<File> javaFiles = DirProcess.walkJavaFile(projectDir);

        for (File file : javaFiles) {
            CompilationUnit cu = createCU(file);
            // Now binding is activated. Do something else
            cu.accept(new Visitor(this));
        }
    }


}
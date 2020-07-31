package jdtparser;

import config.Config;
import data.Member;
import data.ClassModel;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import utils.DirProcess;
import utils.FileProcess;
import utils.Timer;
import utils.Utils;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ProjectParser {
    public HashMap<String, ClassModel> classListModel = new HashMap<>();

    private String projectDir;
    private String[] sourcePaths;
    private String[] encodeSources;
    private String[] classPaths;

    public static void main(String[] args) {
        Timer timer = new Timer();


        System.out.println("Starting parse...");
        ProjectParser projectParser = new ProjectParser(Config.PROJECT_DIR, Config.SOURCE_PATH, Config.ENCODE_SOURCE, Config.CLASS_PATH);
//        projectParser.parse();

        System.out.print("Project parse time: ");
        System.out.printf("%.5fs\n", timer.getTimeCounter() / 1000.0);

        File curFile = new File(Config.TEST_FILE_PATH);

        FileParser fileParser = new FileParser(projectParser, curFile, Config.TEST_POSITION);
        try {
            fileParser.parse();
        } catch (Exception e) {
//            e.printStackTrace();
        }

        System.out.print("File parse: ");
        System.out.printf("%.5fs\n", timer.getTimeCounter() / 1000.0);

        List<IProblem> problems = fileParser.getErrors(0, 20000);
        System.out.println(problems.isEmpty());
        System.out.print("Type check: ");
        System.out.printf("%.5fs\n", timer.getTimeCounter() / 1000.0);

//        cassandra
        List<File> listFile = DirProcess.walkJavaFile(Config.PROJECT_DIR);
        AtomicInteger numError = new AtomicInteger();
        listFile.forEach(file -> {
//            System.out.println(file.getAbsolutePath());

            FileParser fileeParser = new FileParser(projectParser, file, Config.TEST_POSITION);

            try {
                fileeParser.parse();
            } catch (Exception e) {
//                e.printStackTrace();
            }


//            System.out.print("File parse: ");
//            System.out.printf("%.5fs\n", timer.getTimeCounter() / 1000.0);

            List<IProblem> problemss = fileeParser.getErrors(0, 20000);
//            System.out.println(problemss.isEmpty());

            if (!problemss.isEmpty()) {
                numError.getAndIncrement();
                problemss.forEach(problemNoti -> {
                    System.out.println(problemNoti);
                });
                System.out.println(file.getAbsolutePath());

            }

//            System.out.print("Type check: ");
//            System.out.printf("%.5fs\n", timer.getTimeCounter() / 1000.0);
        });

        System.out.println("Number problems: " + numError);

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

    public ProjectParser(String projectDir, String[] sourcePaths, String[] encodeSources, String[] classPaths) {
        this.projectDir = projectDir;
        this.sourcePaths = sourcePaths;
        this.classPaths = classPaths;
        this.encodeSources = encodeSources;
    }

    public void parseClass(ITypeBinding iTypeBinding) {
        if (iTypeBinding == null) return;
        if (iTypeBinding.isPrimitive()) return;
        ClassModel classModel = new ClassModel(iTypeBinding);

        classListModel.put(classModel.getOrgType().getKey(), classModel);
    }

    public CompilationUnit createCU(File file) {
        ASTParser parser = ASTParser.newParser(Config.JDT_LEVEL); //choose source code analyzing strategy
        parser.setResolveBindings(true); // turn on binding strategy
        parser.setKind(ASTParser.K_COMPILATION_UNIT);// the source code is a file .java
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        Hashtable<String, String> options = JavaCore.getOptions();

        JavaCore.setComplianceOptions(Config.JAVA_VERSION, options);

        parser.setCompilerOptions(options);
        parser.setEnvironment(classPaths, sourcePaths, encodeSources, true);

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
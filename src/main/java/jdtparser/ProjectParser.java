package jdtparser;

import config.Config;
import data.Member;
import data.ClassModel;
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

    public static void main(String[] args) throws IOException {
        System.out.println("Starting parse...");
        ProjectParser projectParser = new ProjectParser(Config.PROJECT_DIR, Config.SOURCE_PATH, Config.CLASS_PATH);

        try {
            ITypeBinding clazz = projectParser.getClassScope(1805, new File("/Users/maytinhdibo/Downloads/data/cassandra/src/java/org/apache/cassandra/hadoop/ConfigHelper.java"));
            HashMap<String, ClassModel> listAccess = projectParser.getListAccess(clazz);
        } catch (NullPointerException err) {
            err.printStackTrace();
        }

        System.out.println("Parse done!");
    }

    private HashMap<String, ClassModel> getListAccess(ITypeBinding clazz) {
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

    public CompilationUnit createCU(String[] sourcePaths, String[] classpath, File file) {
        ASTParser parser = ASTParser.newParser(AST.JLS13);//choose source code analyzing strategy
        parser.setResolveBindings(true);// turn on binding strategy
        parser.setKind(ASTParser.K_COMPILATION_UNIT);// the source code is a file .java
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(JavaCore.getOptions());
        parser.setEnvironment(classpath, sourcePaths, new String[]{"UTF-8"}, true);

        parser.setUnitName(file.getName());
        parser.setSource(FileProcess.read(file).toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(new NullProgressMonitor());
        return cu;

    }

    public void parse() {
        List<File> javaFiles = DirProcess.walkJavaFile(projectDir);

        for (File file : javaFiles) {
            CompilationUnit cu = createCU(sourcePaths, classPaths, file);

            // Now binding is activated. Do something else
            cu.accept(new Visitor(this));
        }
    }

    public ITypeBinding getClassScope(int position, File file) throws NullPointerException {
        CompilationUnit cu = createCU(sourcePaths, classPaths, file);
        final TypeDeclaration[] result = {null};
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                int cPosStart = node.getStartPosition();
                int cPosEnd = node.getStartPosition() + node.getLength();
                if (cPosStart <= position && cPosEnd >= position) {
                    result[0] = node;
                }
                return true;
            }
        });
        if (result[0] == null) throw new NullPointerException();
        return result[0].resolveBinding();
    }


}
package jdtparser;

import config.Config;
import data.Member;
import data.TypeModel;
import data.TypeModel;
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
    public HashMap<String, TypeModel> classListModel = new HashMap<>();

    private String projectDir;
    private String[] sourcePaths;
    private String[] classPaths;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting parse...");
        ProjectParser projectParser = new ProjectParser(Config.PROJECT_DIR, Config.SOURCE_PATH, Config.CLASS_PATH);
        ITypeBinding clazz = projectParser.getClassScope(1805, new File("file:///Users/maytinhdibo/Downloads/data/cassandra/src/java/org/apache/cassandra/hadoop/ConfigHelper.java"));
        HashMap<String, TypeModel> listAccess = projectParser.getListAccess(clazz);
        System.out.println("Parse done!");
    }

    private HashMap<String, TypeModel> getListAccess(ITypeBinding clazz) {
        HashMap<String, TypeModel> listAccess = new HashMap<>();
        for (Map.Entry<String, TypeModel> entry : classListModel.entrySet()) {
            String key = entry.getKey();
            TypeModel typeModel = entry.getValue();

            if (clazz == typeModel.getOrgType()) {
                listAccess.put(clazz.getKey(), typeModel.clone());
                continue;
            } else {
                boolean extended = clazz.isSubTypeCompatible(typeModel.getOrgType());

                String fromPackage = clazz.getPackage().getName();
                String toPackage = "-1";
                if (typeModel.getOrgType().getPackage() != null) {
                    toPackage = typeModel.getOrgType().getPackage().getName();
                }

                if (Utils.checkVisibleMember(typeModel.getOrgType().getModifiers(), fromPackage, toPackage, extended)) {
                    TypeModel cloneModel = typeModel.clone();
                    listAccess.put(key, cloneModel);
                    List<Member> toRemoves = new ArrayList<>();
                    String finalToPackage = toPackage;
                    typeModel.getMembers().forEach(member -> {
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
        TypeModel typeModel = new TypeModel(iTypeBinding);

        classListModel.put(typeModel.getOrgType().getKey(), typeModel);
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

    public ITypeBinding getClassScope(int position, File file) {
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
        return result[0].resolveBinding();
    }


}


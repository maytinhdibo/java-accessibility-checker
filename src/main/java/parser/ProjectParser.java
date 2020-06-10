package parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javassistmodel.JavassistClassDeclaration;
import com.github.javaparser.symbolsolver.logic.AbstractClassDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import config.Config;
import data.ClassModel;
import data.FileData;
import data.Member;
import utils.DirProcess;
import utils.FileProcess;
import utils.Log;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProjectParser {
    public HashMap<String, ClassModel> classListModel = new HashMap<>();
    private CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
    private String projectDir;

    public ProjectParser() {
        combinedTypeSolver = new CombinedTypeSolver();
    }

    public static void main(String[] args) {
        ProjectParser projectParser = new ProjectParser();
        projectParser.addSource(Config.SOURCE_PATH);
        projectParser.setProjectPath(Config.PROJECT_DIR);
        try {
            projectParser.addJarFile("/Users/maytinhdibo/Downloads/org.json-chargebee-1.0.jar");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Starting parser...");
        projectParser.parse();
        System.out.println("Parse done!");
    }

    public void setProjectPath(String projectPath) {
        projectDir = projectPath;
    }

    public void addSource(String sourcePath) {
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourcePath)));
    }

    public void addJarFile(String jarPath) throws IOException {
        combinedTypeSolver.add(new JarTypeSolver(new File(jarPath)));
    }

    public void addClass(ClassModel classModel) {
        classListModel.put(classModel.getClassId(), classModel);
    }

    public void parseClass(String classId) {
//        Object d = combinedTypeSolver.solveType(classId);
        classId = Utils.normalizeId(classId);
        parseClass((AbstractClassDeclaration) combinedTypeSolver.solveType(classId));
    }

    public void parseClass(AbstractClassDeclaration declaration) {
        SymbolSolverClassParser classParser = new SymbolSolverClassParser(declaration, this);
        classParser.parse();
    }

    public void parse() {
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        JarTypeSolver jarTypeSolver = null;
        try {
            jarTypeSolver = new JarTypeSolver(new File("/Users/maytinhdibo/Downloads/org.json-chargebee-1.0.jar"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        combinedTypeSolver.add(jarTypeSolver);

        List<File> javaFiles = DirProcess.walkJavaFile(projectDir);
        List<FileData> dataFiles = new ArrayList<FileData>();


        for (File file : javaFiles) {
            String data = FileProcess.read(file);
            Log.write("File: " + file.getAbsolutePath());
            dataFiles.add(new FileData(file.getPath(), data));
            CompilationUnit compilationUnit = StaticJavaParser.parse(data);
//            SymbolSolver.resolvedType(compilationUnit);

            String packageName = compilationUnit.getPackageDeclaration().get().getName().toString();
            compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(klass -> {
                ProjectClassParser classParser = new ProjectClassParser(klass, packageName, this);
                classParser.parse();
            });

            compilationUnit.findAll(Expression.class).forEach(be -> {
                try {
                    if (be == null) return;
                    if (!(be instanceof MarkerAnnotationExpr)) {
                        ResolvedType resolvedType = be.calculateResolvedType();
                        if (resolvedType.isReferenceType()) {
                            ResolvedReferenceTypeDeclaration declaration = resolvedType.asReferenceType().getTypeDeclaration();
                            if (declaration instanceof ReflectionClassDeclaration || declaration instanceof JavassistClassDeclaration) {
                                parseClass((AbstractClassDeclaration) declaration);
                            }
                        }
                    }
                } catch (UnsupportedOperationException e) {
                    Log.warning("NOT SUPPORT BINDING: " + be.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public boolean isExtended(String classId, String parentId) {
        if (parentId.equals("java.lang.Object")) return true;
        if (classId.equals("java.lang.Object")) return false;

        if (classListModel.get(classId) == null) return false;

        String extendedClass = Utils.normalizeId(classListModel.get(classId).getClassExtended());

        if (extendedClass.equals(parentId)) {
            return true;
        } else {
            return isExtended(extendedClass, parentId);
        }
    }

    public HashMap<String, ClassModel> canAccess(String fromClass) {
        HashMap<String, ClassModel> canAccessList = new HashMap<>();

        for (Map.Entry<String, ClassModel> entry : classListModel.entrySet()) {
            String key = entry.getKey();
            ClassModel klass = entry.getValue();
            ClassModel klazz = klass.clone();
            if (klass.getClassId().equals(fromClass)) {
                canAccessList.put(key, klazz);
                continue;
            }
            boolean extended = isExtended(fromClass, klass.getClassId());
            if (Utils.checkVisibleMember(klazz.getAccessModifier(), fromClass, klazz.getClassId(), extended)) {
                canAccessList.put(key, klazz);
                List<Member> toRemoves = new ArrayList<>();
                klazz.getMembers().forEach(member -> {
                    if (!Utils.checkVisibleMember(member.getAccessModifier(), fromClass, klazz.getClassId(), extended)) {
                        toRemoves.add(member);
                    }
                });
                klazz.getMembers().removeAll(toRemoves);
            }
        }
        return canAccessList;
    }
}

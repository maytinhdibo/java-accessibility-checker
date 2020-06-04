package parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.reflectionmodel.ReflectionClassDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import config.Config;
import data.ClassModel;
import data.FileData;
import utils.DirProcess;
import utils.FileProcess;
import utils.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
        projectParser.parse();
    }

    public void setProjectPath(String projectPath) {
        projectDir = projectPath;
    }

    public void addClass(ClassModel classModel) {
        classListModel.put(classModel.getClassId(), classModel);
    }

    public void addSource(String sourcePath) {
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(sourcePath)));
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


        Object d = jarTypeSolver.solveType("org.json.CDL").asClass().getAllMethods();


        for (File file : javaFiles) {
            String data = FileProcess.read(file);
            Log.write("File: " + file.getAbsolutePath());
            dataFiles.add(new FileData(file.getPath(), data));
            CompilationUnit compilationUnit = StaticJavaParser.parse(data);
            SymbolSolver.resolvedType(compilationUnit);

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
                            ResolvedReferenceTypeDeclaration id = resolvedType.asReferenceType().getTypeDeclaration();
                            if (id instanceof ReflectionClassDeclaration) {
                                ReflectionClassParser classParser = new ReflectionClassParser((ReflectionClassDeclaration)id, this);
                                classParser.parse();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.warning("NOT SUPPORT BINDING: " + be.toString());
                }
            });

        }
    }

}

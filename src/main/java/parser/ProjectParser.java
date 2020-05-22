package parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import config.Config;
import data.ClassModel;
import data.FileData;
import utils.DirProcess;
import utils.FileProcess;
import utils.Log;

import java.io.File;
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

        List<File> javaFiles = DirProcess.walkJavaFile(projectDir);
        List<FileData> dataFiles = new ArrayList<FileData>();

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
        }
    }

}

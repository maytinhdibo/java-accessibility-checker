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

    public static void main(String[] args) {
        ProjectParser projectParser = new ProjectParser();
        projectParser.parse();
    }

    public void addClass(ClassModel classModel) {
        classListModel.put(classModel.getClassId(), classModel);
    }

    public void parse() {
        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        //Add src path
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(Config.SOURCE_PATH)));
        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);


        List<File> javaFiles = DirProcess.walkJavaFile(Config.PROJECT_DIR);
        List<FileData> dataFiles = new ArrayList<FileData>();

        for (File file : javaFiles) {
            String data = FileProcess.read(file);
            Log.write("File: " + file.getAbsolutePath());
            dataFiles.add(new FileData(file.getPath(), data));
            CompilationUnit compilationUnit = StaticJavaParser.parse(data);
            SymbolSolver.resolvedType(compilationUnit);

            String packageName = compilationUnit.getPackageDeclaration().get().getName().toString();
            compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(klass -> {
                ProjectClassParser classParser = new ProjectClassParser();
                classParser.parse(klass, packageName, this);
            });
        }
    }

}

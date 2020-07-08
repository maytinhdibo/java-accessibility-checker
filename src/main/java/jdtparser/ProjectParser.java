package jdtparser;

import com.google.errorprone.annotations.Var;
import config.Config;
import data.Member;
import data.ClassModel;
import data.Variable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import utils.DirProcess;
import utils.FileProcess;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


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

        ASTNode scope = projectParser.getScope(985, new File("/Users/maytinhdibo/Project/bomberman/src/com/carlosflorencio/bomberman/TestExtended.java"));

        Object m = null;
        if (scope != null) {
            m = getVariableScope(scope);
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
                if (isNode(position, node)) {
                    result[0] = node;
                }
                return true;
            }
        });
        if (result[0] == null) throw new NullPointerException();
        return result[0].resolveBinding();
    }

    public ASTNode getScope(int position, File file) {
        CompilationUnit cu = createCU(sourcePaths, classPaths, file);
        final ASTNode[] astNode = {null};

        cu.accept(new ASTVisitor() {
            public void preVisit(ASTNode node) {
                if (isNode(position, node)) {
                    astNode[0] = node;
                }
            }
        });
        return astNode[0];
    }

    public static List<Variable> getVariableScope(ASTNode astNode) {
        List<Variable> listVariable = new ArrayList<>();
        getVariableScope(astNode, listVariable);
        return listVariable;
    }

    public static void getVariableScope(ASTNode astNode, List<Variable> variableList) {
        if (astNode == null) return;
        Block block = null;
        if (astNode instanceof Block) {
            block = (Block) astNode;
        } else if (astNode instanceof MethodDeclaration) {
            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
            block = methodDeclaration.getBody();

            List params = methodDeclaration.parameters();
            params.forEach(param -> {
                if (param instanceof SingleVariableDeclaration) {
                    SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) param;
                    IVariableBinding variableBinding = singleVariableDeclaration.resolveBinding();
                    ITypeBinding typeBinding = variableBinding.getType();
                    String varName = singleVariableDeclaration.getName().toString();

                    if (!checkVariableInList(varName, variableList))
                        variableList.add(new Variable(typeBinding, varName));
                }
            });
        } else if (astNode instanceof TypeDeclaration) {
            ITypeBinding classBinding = ((TypeDeclaration) astNode).resolveBinding();
            IVariableBinding[] variableBindings = classBinding.getDeclaredFields();
            for (IVariableBinding variableBinding : variableBindings) {
                ITypeBinding typeBinding = variableBinding.getType();
                String varName = variableBinding.getName();

                if (!checkVariableInList(varName, variableList))
                    variableList.add(new Variable(typeBinding, varName));
            }
        }

        if (block != null) {
            List listStatement = block.statements();
            listStatement.forEach(stmt -> {
                if (stmt instanceof VariableDeclarationStatement) {
                    VariableDeclarationStatement declareStmt = (VariableDeclarationStatement) stmt;
                    declareStmt.fragments().forEach(fragment -> {
                        if (fragment instanceof VariableDeclarationFragment) {
                            IVariableBinding variableBinding = ((VariableDeclarationFragment) fragment).resolveBinding();
                            String varName = ((VariableDeclarationFragment) fragment).getName().toString();
                            if (!checkVariableInList(varName, variableList))
                                variableList.add(new Variable(variableBinding.getType(), varName));
                        }
                    });
                }
            });
        }

        getVariableScope(getParentBlock(astNode), variableList);

    }

    public static boolean checkVariableInList(String varName, List<Variable> variableList) {
        for (Variable variableTmp : variableList) {
            if (varName.equals(variableTmp.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNode(int pos, ASTNode astNode) {
        int cPosStart = astNode.getStartPosition();
        int cPosEnd = astNode.getStartPosition() + astNode.getLength();
        if (cPosStart <= pos && cPosEnd >= pos) {
            return true;
        }
        return false;
    }

    public static ASTNode getParentBlock(ASTNode astNode) {
        if (astNode == null) return null;
        ASTNode parentNode = astNode.getParent();
        if (parentNode instanceof Block) {
//            if (parentNode.getParent() instanceof MethodDeclaration) return parentNode.getParent();
            return (Block) parentNode; //block object
        } else if (parentNode instanceof MethodDeclaration) {
            return (MethodDeclaration) parentNode;
        } else if (parentNode instanceof TypeDeclaration) {
            return parentNode;
        } else return getParentBlock(parentNode);

    }


}
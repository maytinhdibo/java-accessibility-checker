package jdtparser;

import data.ClassModel;
import data.Variable;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileParser {
    private ProjectParser projectParser;
    private File curFile;
    private int curPosition;

    private CompilationUnit cu;

    public List<Variable> visibleVariable;
    public HashMap<String, ClassModel> visibleClass;

    public FileParser(ProjectParser projectParser, File curFile, int curPosition) {
        this.projectParser = projectParser;
        this.curFile = curFile;
        this.curPosition = curPosition;
        cu = projectParser.createCU(curFile);
    }

    public void parse() {
        try {
            ITypeBinding clazz = getClassScope(curPosition);
            visibleClass = projectParser.getListAccess(clazz);
        } catch (NullPointerException err) {
            visibleClass.clear();
            err.printStackTrace();
        }

        ASTNode scope = getScope(curPosition);

        if (scope != null) {
            visibleVariable = getVariableScope(scope);
        } else {
            visibleVariable.clear();
        }
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
//            block = methodDeclaration.getBody();

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
        } else if (astNode instanceof LambdaExpression) {
            LambdaExpression lambdaExpression = (LambdaExpression) astNode;
            List params = lambdaExpression.parameters();
            params.forEach(param -> {
                if (param instanceof VariableDeclarationFragment) {
                    VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) param;
                    IVariableBinding variableBinding = variableDeclarationFragment.resolveBinding();

                    ITypeBinding typeBinding = variableBinding.getType();
                    String varName = variableBinding.getName();

                    if (!checkVariableInList(varName, variableList))
                        variableList.add(new Variable(typeBinding, varName));
                }
            });
        } else if (astNode instanceof ForStatement) {
            ForStatement forStatement = (ForStatement) astNode;
            List inits = forStatement.initializers();
            inits.forEach(init -> {
                if (init instanceof VariableDeclarationExpression) {
                    VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) init;
                    List variableDeclarations = variableDeclarationExpression.fragments();
                    variableDeclarations.forEach(variableDeclarationItem -> {
                        if (variableDeclarationItem instanceof VariableDeclarationFragment) {
                            IVariableBinding variableBinding = ((VariableDeclarationFragment) variableDeclarationItem).resolveBinding();

                            ITypeBinding typeBinding = variableBinding.getType();
                            String varName = variableBinding.getName();

                            if (!checkVariableInList(varName, variableList))
                                variableList.add(new Variable(typeBinding, varName));
                        }
                    });
                }
            });
        } else if (astNode instanceof EnhancedForStatement) {
            EnhancedForStatement enhancedForStatement = (EnhancedForStatement) astNode;
            SingleVariableDeclaration singleVariableDeclaration = enhancedForStatement.getParameter();

            IVariableBinding variableBinding = singleVariableDeclaration.resolveBinding();
            ITypeBinding typeBinding = variableBinding.getType();
            String varName = singleVariableDeclaration.getName().toString();

            if (!checkVariableInList(varName, variableList))
                variableList.add(new Variable(typeBinding, varName));
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

    private void addVariableToList(int startPosition, IVariableBinding variableBinding, List<Variable> variableList) {
        ITypeBinding typeBinding = variableBinding.getType();
        String varName = variableBinding.getName();

        if (!checkVariableInList(varName, variableList))
            variableList.add(new Variable(typeBinding, varName));
    }

    public static boolean checkVariableInList(String varName, List<Variable> variableList) {
        for (Variable variableTmp : variableList) {
            if (varName.equals(variableTmp.getName())) {
                return true;
            }
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
            return parentNode;
        } else if (parentNode instanceof TypeDeclaration) {
            return parentNode;
        } else if (parentNode instanceof LambdaExpression) {
            return parentNode;
        } else if (parentNode instanceof ForStatement || parentNode instanceof EnhancedForStatement) {
            return parentNode;
        } else return getParentBlock(parentNode);
    }

    public ITypeBinding getClassScope(int position) throws NullPointerException {
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

    public ASTNode getScope(int position) {
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


    public static boolean isNode(int pos, ASTNode astNode) {
        int cPosStart = astNode.getStartPosition();
        int cPosEnd = astNode.getStartPosition() + astNode.getLength();
        if (cPosStart <= pos && cPosEnd >= pos) {
            return true;
        }
        return false;
    }

}

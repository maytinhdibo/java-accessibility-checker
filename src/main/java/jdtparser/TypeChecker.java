package jdtparser;

import data.Variable;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class TypeChecker {
    private CompilationUnit cu;
    private int startPos;
    private int stopPos;

    public TypeChecker(CompilationUnit cu, int startPos, int stopPos) {
        this.cu = cu;
        this.startPos = startPos;
        this.stopPos = stopPos;
    }

    public boolean check() {
        TypeCheckerVisitor visitor = new TypeCheckerVisitor(startPos, stopPos);
        cu.accept(visitor);
        return true;
    }

    public static int checkStatement(ASTNode astNode) {
        // 0    ASTNode can not be checked
        // 1    true
        // 2    cast
        // -1   false
        if (astNode instanceof VariableDeclarationStatement) {
            VariableDeclarationStatement variableDeclarationStmt = (VariableDeclarationStatement) astNode;
            List fragments = variableDeclarationStmt.fragments();
            fragments.forEach(fragment -> {
                if (fragment instanceof VariableDeclarationFragment) {
                    VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;
                    ((VariableDeclarationFragment) fragment).resolveBinding();
                    Expression expression = variableDeclarationFragment.getInitializer();
                    System.out.println("a");
                }
            });
        } else if (astNode instanceof VariableDeclarationFragment) {
            return 1;
        } else if (astNode instanceof Assignment) {
            return 1;
        } else if (astNode instanceof PrefixExpression) {
            return 1;
        } else if (astNode instanceof PostfixExpression) {
            return 1;
        } else if (astNode instanceof InfixExpression) {
            return 1;
        } else if (astNode instanceof MethodInvocation) {
            return 1;
        } else if (astNode instanceof ConstructorInvocation) {

        }
        return 0;
    }
}

class TypeCheckerVisitor extends ASTVisitor {
    public int status = 9;
    private int startPos, stopPos;

    public TypeCheckerVisitor(int startPos, int stopPos) {
        this.startPos = startPos;
        this.stopPos = stopPos;
    }

    @Override
    public boolean preVisit2(ASTNode astNode) {
        int start = astNode.getStartPosition();
        int end = start + astNode.getLength();
        if ((start < startPos && end < startPos) || start > stopPos) return false;
        return true;
    }

    //Assignment, VariableDeclarationFragment, PrefixExpression, PostfixExpression, InfixExpression, MethodInvocation, ConstructorInvocation
    @Override
    public boolean visit(VariableDeclarationFragment variableDeclarationFragment) {
        Expression init = variableDeclarationFragment.getInitializer();
        if (init == null) return false;

        IVariableBinding variableBinding = variableDeclarationFragment.resolveBinding();

        ITypeBinding leftType = variableBinding.getType();
        ITypeBinding rightType = init.resolveTypeBinding();

        addValue(compareType(leftType, rightType));
        return true;
    }

    @Override
    public boolean visit(PrefixExpression prefixExpression) {
        ITypeBinding type = prefixExpression.resolveTypeBinding();
        if (type == null) {
            addValue(0);
            return false;
        }
        if (type.isPrimitive() && !type.getName().equals("boolean")) {
            addValue(1);
            return true;
        }
        addValue(-1);
        return true;
    }

    @Override
    public boolean visit(PostfixExpression postfixExpression) {
        ITypeBinding type = postfixExpression.resolveTypeBinding();
        if (type == null) {
            addValue(0);
            return false;
        }
        if (type.isPrimitive() && !type.getName().equals("boolean")) {
            addValue(1);
            return true;
        }
        addValue(-1);
        return true;
    }

    @Override
    public boolean visit(Assignment assignment) {
        ITypeBinding leftType = assignment.getLeftHandSide().resolveTypeBinding();
        ITypeBinding rightType = assignment.getRightHandSide().resolveTypeBinding();

        addValue(compareType(leftType, rightType));
        return true;
    }

    @Override
    public boolean visit(ReturnStatement returnStatement) {
        ITypeBinding leftType = getMethodType(returnStatement);
        ITypeBinding rightType = returnStatement.getExpression().resolveTypeBinding();

        addValue(compareType(leftType, rightType));
        return true;
    }

    public ITypeBinding getMethodType(ASTNode astNode) {
        if (astNode == null) return null;
        if (astNode instanceof MethodDeclaration) {
            return ((MethodDeclaration) astNode).getReturnType2().resolveBinding();
        }
        return getMethodType(astNode.getParent());
    }

    public int compareType(ITypeBinding leftType, ITypeBinding rightType) {
        if (leftType == null || rightType == null) return 0;
        if (rightType.isAssignmentCompatible(leftType)) return 1;
        if (rightType.isCastCompatible(leftType)) {
            return 2;
        }

        if (leftType.isPrimitive() && rightType.isPrimitive()
                && !leftType.getName().equals("boolean") && !rightType.getName().equals("boolean")) {
            return 2;
        }

        //Consider Integer, Float, Double,...

        return -1;
    }

    public void addValue(int value) {
        if (value < status) status = value;
    }
}

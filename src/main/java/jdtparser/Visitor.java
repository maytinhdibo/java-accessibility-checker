package jdtparser;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;

public class Visitor extends ASTVisitor {
    ProjectParser projectParser;

    public Visitor(ProjectParser projectParser) {
        this.projectParser = projectParser;
    }

    //    public boolean visit(MethodInvocation methodInvocation) {
//        String code = methodInvocation.toString();
//        ITypeBinding declaringClass = methodInvocation.resolveTypeBinding();
//        projectParser.parseClass(declaringClass);
//        return true;
//    }
//
//
//    public boolean visit(VariableDeclarationStatement variableDeclarationStatement) {
//        ITypeBinding typeBinding = variableDeclarationStatement.getType().resolveBinding();
//        projectParser.parseClass(typeBinding);
//        return true;
//    }
//
//    public boolean visit(TypeDeclaration typeDeclaration) {
//        ITypeBinding typeBinding = typeDeclaration.resolveBinding();
//        projectParser.parseClass(typeBinding);
//        return true;
//    }
    public boolean visit(SimpleType simpleType) {
        ITypeBinding typeBinding = simpleType.resolveBinding();
        projectParser.parseClass(typeBinding);
        return true;
    }
}


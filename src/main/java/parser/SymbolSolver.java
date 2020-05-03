package parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import utils.Log;

public class SymbolSolver {
    public static void resolvedType(CompilationUnit cu) {
        cu.findAll(MethodDeclaration.class).forEach(be -> {
            try {
                Object a = be.getType().resolve().asReferenceType();
                System.out.println(a.toString());
                System.out.println("abc");
            }catch (Exception e){
                System.out.println(e);
            }
                }
        );

            // Find all the calculations with two sides:
        cu.findAll(Expression.class).forEach(be -> {
            try {
                if (!(be instanceof MarkerAnnotationExpr)) {
                    ResolvedType resolvedType = be.calculateResolvedType();
                    Log.write("\t" + be.toString() + " is a: " + resolvedType);
                }
            } catch (Exception e) {
                Log.warning("NOT SUPPORT BINDING: " + be.toString());
//                e.printStackTrace();
            }
        });
    }
}

package parser;

import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import config.StringConstant;
import data.ClassModel;
import data.DataType;
import data.MethodMember;

import java.util.List;

public class ClassParser {
    protected ClassModel classModel = null;
    protected MethodMember curMethod = null;
    protected String packageName;
    protected ProjectParser projectParser;

    public boolean resolveGenericType(String name) {
        if (classModel.getGenericTypes().contains(name)) return true;
        if (curMethod != null && curMethod.getGenericTypes().contains(name)) return true;
        return false;
    }

    protected void parseGenericType(List<ResolvedTypeParameterDeclaration> genericTypes) {
        genericTypes.forEach(type -> {
            classModel.addGenericType(type.getName());
        });
    }

    //parse generic type
    protected void parseGenericType() {
    }

    public DataType parseType(Type type) throws UnsupportedOperationException {
        return parseType(type, false);
    }

    public DataType parseType(Type type, boolean isArray) throws UnsupportedOperationException{
        if (type instanceof ArrayType) return parseType(((ArrayType) type).getComponentType(), true);

        if (type.isPrimitiveType()) {
            return new DataType(type.toString(), true, isArray);
        } else if (type instanceof VoidType) {
            DataType voidType = new DataType(StringConstant.VOID.toString(), true, false);
            voidType.setVoid(true);
            return voidType;
        } else {
            String typeId = "";
            String typeName = type.toString();

            boolean isGenericType = false;
            try {
                typeId = type.resolve().asReferenceType().getTypeDeclaration().get().getId();
            } catch (UnsupportedOperationException err) {
                if (resolveGenericType(typeName)) isGenericType = true;
                else throw err;
            }catch (UnsolvedSymbolException err){
                err.printStackTrace();
            }
            DataType result = new DataType(typeId, typeName, isArray);
            if (isGenericType) result.setGenericType(true);
            if (type instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) type;
                result.setName(classOrInterfaceType.getNameAsString());
                if (classOrInterfaceType.getTypeArguments().isPresent()) {
                    classOrInterfaceType.getTypeArguments().get().forEach(typeArg -> {
                        if (typeArg instanceof Type) result.addTypeArg(parseType(typeArg, false));
                    });
                }
            }
            return result;
        }
    }

    public static StringConstant getAccessModifier(String word) {
        switch (word.toLowerCase()) {
            case "public":
                return StringConstant.PUBLIC;
            case "private":
                return StringConstant.PRIVATE;
            case "protected":
                return StringConstant.PROTECTED;
            default:
                return StringConstant.DEFAULT;
        }
    }


}

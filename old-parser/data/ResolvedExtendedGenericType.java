package data;

import com.github.javaparser.resolution.types.ResolvedType;
import config.StringConstant;

public class ResolvedExtendedGenericType {
    public StringConstant type;
    public String result;
    public ResolvedType resolvedResult;
    public String parseFrom;

    public ResolvedExtendedGenericType(StringConstant type, String result, String parseFrom) {
        this.type = type;
        this.result = result;
        this.parseFrom = parseFrom;
    }

    public ResolvedExtendedGenericType(StringConstant type, ResolvedType resolvedResult, String parseFrom) {
        this.type = type;
        this.resolvedResult = resolvedResult;
        this.parseFrom = parseFrom;
    }
}

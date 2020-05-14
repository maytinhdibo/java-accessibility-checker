package data;

import config.StringConstant;

public class ResolvedExtendedGenericType {
    public StringConstant type;
    public String result;
    public String parseFrom;

    public ResolvedExtendedGenericType(StringConstant type, String result, String parseFrom) {
        this.type = type;
        this.result = result;
        this.parseFrom = parseFrom;
    }
}

package config;

public enum StringConstant {
    DEFAULT("default"),
    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),
    VOID("void"),
    RESOLVED("resolved"),
    GENERIC("generic");

    private String value;

    StringConstant(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

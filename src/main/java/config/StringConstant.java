package config;

public enum StringConstant {
    DEFAULT("default"),
    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected");

    private String value;

    StringConstant(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
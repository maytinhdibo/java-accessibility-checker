package config;

public enum StringConstant {
    START("START"),
    END("END"),
    CLASS("CLASS"),
    METHOD("METHOD"),
    TYPE("TYPE"),
    VAR("VAR");

    private String value;

    StringConstant(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

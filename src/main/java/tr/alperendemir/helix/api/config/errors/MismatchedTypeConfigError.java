package tr.alperendemir.helix.api.config.errors;

public record MismatchedTypeConfigError(Object expected, Object received) implements ConfigError {

    @Override
    public ConfigErrorType getErrorType() {
        return ConfigErrorType.RECOVERABLE;
    }

    @Override
    public String getMessage() {
        return "<light:white>Expected to get <light:green>%s <light:white>but received <light:red>%s <light:gray>%s %s".formatted(
                getName(this.expected()),
                getName(this.received()),
                this.expected == null ? null : this.expected.getClass().getName(),
                this.received == null ? null : this.received.getClass().getName()
        );
    }

    private String getName(Object obj) {
        if (obj == null) {
            return "nothing";
        }

        if (!(obj instanceof Class<?> clazz)) {
            return getName(obj.getClass());
        }

        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }

        if (clazz == Boolean.class || clazz == boolean.class) {
            return "a true/false value";
        }

        if (checkSuperClass(clazz, CharSequence.class)) {
            return "some text";
        }

        if (checkSuperClass(clazz, Number.class)) {
            return "a number";
        }

        return clazz.getSimpleName();
    }

    private boolean checkSuperClass(Class<?> clazz, Class<?> expectedSuperClass) {
        if (clazz == expectedSuperClass) {
            return true;
        }

        if (expectedSuperClass.isAssignableFrom(clazz)) {
            return true;
        }

        var superClass = clazz.getSuperclass();
        if (superClass == null) {
            return false;
        }

        return checkSuperClass(superClass, expectedSuperClass);
    }

    @Override
    public String toString() {
        return "MismatchedTypeConfigError[" + this.getMessage() + "]";
    }
}

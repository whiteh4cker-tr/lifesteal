package tr.alperendemir.helix.api.config.parsing.auto;

import tr.alperendemir.helix.api.config.Commentable;
import tr.alperendemir.helix.api.config.Configuration;
import tr.alperendemir.helix.api.config.builder.ConfigurationBuilder;
import tr.alperendemir.helix.api.config.parsing.CompoundTypeParser;
import tr.alperendemir.helix.api.logging.HelixLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

/**
 * An automatic parser to parse types, do note that it may not always be the right choice for your types.
 * This parser does not have the required flexibility to handle varying parsers, for that you should still make your own.
 * */
public class AutoParser<T> implements CompoundTypeParser<T> {

    /**
     * We need this unsafe instance to set final fields
     * */
    private static final Unsafe UNSAFE;

    static {
        try {
            var unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);

            UNSAFE = (Unsafe) unsafe.get(null);
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Class<T> type;
    private final Constructor<T> constructor;

    /**
     * Constructs an auto parser, there should be at least one constructor with no parameters.
     * If your class requires method calls <em>after</em> the fields have been initialized you can implement {@link AutoParserCallbacks}
     *
     * @param type The type that the auto-parser should parse
     * @see AutoParserCallbacks
     * */
    public AutoParser(Class<T> type) {
        this.type = type;
        this.constructor = this.getConstructor(this.type);
    }

    @Override
    public @NotNull Class<T> complexType() {
        return this.type;
    }

    @Override
    public @NotNull T deserialize(@NotNull Configuration value) {
        if (this.type.isRecord()) {
            var objects = new Object[this.constructor.getParameterCount()];

            var components = this.type.getRecordComponents();

            for (int i = 0; i < components.length; i++) {
                var component = components[i];
                var type = component.getType();
                var name = component.getName();

                objects[i] = this.getValue(value, type, name);
            }

            try {
                return this.constructor.newInstance(objects);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        T instance;
        try {
            instance = this.constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        AutoParserCallbacks callbacks = null;

        if (AutoParserCallbacks.class.isAssignableFrom(this.type)) {
            callbacks = (AutoParserCallbacks) instance;
        }

        for (var field : this.type.getDeclaredFields()) {
            var modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            var parserTarget = field.getAnnotation(ParserTarget.class);
            var targetName = parserTarget == null ? field.getName() : parserTarget.value();

            var type = field.getType();

            var configValue = this.getValue(value, type, targetName);

            if (callbacks != null) {
                configValue = callbacks.fieldLoaded(type, targetName, configValue);
            }

            if (!type.isInstance(configValue)) {
                throw new IllegalStateException("Unexpected config value: '" + configValue + "' for field: " + targetName);
            }

            UNSAFE.putObject(instance, UNSAFE.objectFieldOffset(field), configValue);
        }

        if (callbacks != null) {
            callbacks.autoParserComplete();
        }

        return instance;
    }

    @Override
    public void serialize(@NotNull Configuration section, @NotNull T value) {
        for (var field : this.type.getDeclaredFields()) {
            var modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            var parserTarget = field.getAnnotation(ParserTarget.class);
            var targetName = parserTarget == null ? field.getName() : parserTarget.value();

            var type = field.getType();

            var defaultValue = this.getField(value, field);

            this.setValue(section, type, defaultValue, targetName);
        }
    }

    @Override
    public void setup(@NotNull ConfigurationBuilder template, @NotNull T value) {
        for (var field : this.type.getDeclaredFields()) {
            var modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            var parserTarget = field.getAnnotation(ParserTarget.class);
            var targetName = parserTarget == null ? field.getName() : parserTarget.value();

            var type = field.getType();

            var defaultValue = this.getField(value, field);

            var commentable = this.setupValue(template, type, defaultValue, targetName);

            var parserComments = field.getAnnotation(ParserComments.class);
            if (parserComments != null) {
                for (var comment : parserComments.value()) {
                    commentable.comment(comment);
                }
            }

            var parserCommentEnum = field.getAnnotation(ParserCommentEnum.class);
            if (parserCommentEnum != null) {
                var enumType = this.getType(type, parserCommentEnum.maxValue());
                this.commentEnum(commentable, type, enumType);
            }
        }
    }

    private void setValue(Configuration config, Class<?> type, Object value, String name) {
        if (this.isConfigPrimitive(type)) {
            config.value(name).value(value);
            return;
        }

        if (type.isArray()) {
            var comp = type.getComponentType();

            if (this.isConfigPrimitive(comp)) {
                config.valueArray(name).values((Object[]) value);
                return;
            }

            config.compoundArray(name).values((Object[]) value);
            return;
        }

        config.compound(name).value(value);
    }

    @SuppressWarnings("unchecked")
    private <E> Commentable<?> setupValue(ConfigurationBuilder template, Class<?> type, Object value, String name) {
        if (this.isConfigPrimitive(type)) {
            var val = template.value(name, value);
            val.next();

            return val;
        }

        if (type.isArray()) {
            var comp = type.getComponentType();

            if (this.isConfigPrimitive(comp)) {
                var val = template.valueArray(name, (Object[]) value);
                val.next();

                return val;
            }

            var compound = template.compoundArray(name, (E[]) value, new AutoParser<>((Class<E>) type)); // TODO replace with section default parsers
            compound.next();

            return compound;
        }

        var compound = template.compound(name, (E) value, new AutoParser<>((Class<E>) type)); // TODO replace with section default parsers
        compound.next();

        return compound;
    }

    private Object getValue(Configuration configuration, Class<?> type, String name) {
        if (this.isConfigPrimitive(type)) {
            return configuration.getValue(name);
        }

        if (type.isArray()) {
            var comp = type.getComponentType();

            if (this.isConfigPrimitive(comp)) {
                return configuration.getValueArray(name);
            }

            return configuration.getCompoundArray(name);
        }

        return configuration.getCompound(name);
    }

    private Object getField(Object obj, Field field) {
        if (obj.getClass().isRecord()) {
            // Find the same method

            return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                try {
                    var method = obj.getClass().getDeclaredMethod(field.getName());
                    method.setAccessible(true);

                    return method.invoke(obj);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    HelixLogger.reportError(e);
                    return null;
                }
            });
        }

        return AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                field.setAccessible(true);
                return field.get(obj);
            } catch (IllegalAccessException e) {
                HelixLogger.reportError(e);
                return null;
            }
        });
    }

    private boolean isConfigPrimitive(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || CharSequence.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void commentEnum(Commentable<?> commentable, Class<?> enumType, @Nullable Object value) {
        if (value == null) {
            commentable.commentEnum((Class<E>) enumType);
            return;
        }

        commentable.commentEnum((Class<E>) enumType, (E) value);
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> Object getType(Class<?> type, String value) {
        if (value.isBlank()) return null;

        try {
            return Enum.valueOf((Class<E>) type, value);
        } catch (IllegalArgumentException ex) {
            HelixLogger.reportError(ex);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Constructor<T> getConstructor(Class<T> type) {
        if (type.isRecord()) {
            var components = type.getRecordComponents();

            var componentTypes = Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new);

            try {
                return type.getConstructor(componentTypes);
            } catch (NoSuchMethodException e) {
                HelixLogger.reportError("[AutoParser] Unable to find record constructor for type %s", type);
                throw new RuntimeException(e);
            }
        }

        for (var constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return (Constructor<T>) constructor;
            }
        }

        HelixLogger.reportError("[AutoParser] Unable to find constructor for type %s", type);
        throw new IllegalStateException();
    }
}

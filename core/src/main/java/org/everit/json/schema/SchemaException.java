package org.everit.json.schema;

import org.json.JSONPointer;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Thrown by {@link org.everit.json.schema.loader.SchemaLoader#load()} when it encounters
 * un-parseable schema JSON definition.
 *
 * @author erosb
 */
public class SchemaException extends RuntimeException {

    private static final long serialVersionUID = 5987489689035036987L;

    private static Object typeOfValue(final Object actualValue) {
        return actualValue == null ? "null" : actualValue.getClass().getSimpleName();
    }

    static String buildMessage(JSONPointer pointer, Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        requireNonNull(pointer, "pointer cannot be null");
        String actualTypeDescr = actualTypeDescr(actualType);
        String formattedPointer = formatPointer(pointer);
        if (furtherExpectedTypes != null && furtherExpectedTypes.length > 0) {
            Class<?>[] allExpecteds = new Class<?>[furtherExpectedTypes.length + 1];
            allExpecteds[0] = expectedType;
            System.arraycopy(furtherExpectedTypes, 0, allExpecteds, 1, furtherExpectedTypes.length);
            return buildMessage(formattedPointer, actualTypeDescr, asList(allExpecteds));
        }
        return format("%s: expected type: %s, found: %s", formattedPointer,
                expectedType.getSimpleName(),
                actualTypeDescr);
    }

    private static String formatPointer(JSONPointer pointer) {
        return pointer.toURIFragment().toString();
    }

    private static String actualTypeDescr(Class<?> actualType) {
        return actualType == null ? "null" : actualType.getSimpleName();
    }

    static String buildMessage(String formattedPointer, String actualTypeDescr, Collection<Class<?>> expectedTypes) {
        String fmtExpectedTypes = expectedTypes.stream()
                .map(Class::getSimpleName)
                .collect(joining(" or "));
        return format("%s: expected type is one of %s, found: %s", formattedPointer,
                fmtExpectedTypes,
                actualTypeDescr);
    }

    private static String buildMessage(JSONPointer pointer, Class<?> actualType, Collection<Class<?>> expectedTypes) {
        return buildMessage(formatPointer(pointer), actualTypeDescr(actualType), expectedTypes);
    }

    private static String joinClassNames(final List<Class<?>> expectedTypes) {
        return expectedTypes.stream().map(Class::getSimpleName).collect(joining(", "));
    }

    private final JSONPointer pointerToViolation;

    public SchemaException(JSONPointer pointerToViolation, String message) {
        super(pointerToViolation == null ? "<unknown location>" : pointerToViolation.toURIFragment().toString() + ": " + message);
        this.pointerToViolation = pointerToViolation;
    }

    public SchemaException(JSONPointer pointerToViolation, Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        super(buildMessage(pointerToViolation, actualType, expectedType, furtherExpectedTypes));
        this.pointerToViolation = pointerToViolation;
    }

    public SchemaException(JSONPointer pointerToViolation, Class<?> actualType, Collection<Class<?>> expectedTypes) {
        super(buildMessage(pointerToViolation, actualType, expectedTypes));
        this.pointerToViolation = pointerToViolation;
    }

    @Deprecated
    public SchemaException(String message) {
        this((JSONPointer) null, message);
    }

    @Deprecated
    public SchemaException(String key, Class<?> expectedType, Object actualValue) {
        this(format("key %s : expected type: %s , found : %s", key, expectedType
                .getSimpleName(), typeOfValue(actualValue)));
    }

    @Deprecated
    public SchemaException(String key, List<Class<?>> expectedTypes,
            final Object actualValue) {
        this(format("key %s: expected type is one of %s, found: %s",
                key, joinClassNames(expectedTypes), typeOfValue(actualValue)));
    }

    @Deprecated
    public SchemaException(String message, Throwable cause) {
        super(message, cause);
        this.pointerToViolation = null;
    }

    public JSONPointer getPointerToViolation() {
        return pointerToViolation;
    }
}

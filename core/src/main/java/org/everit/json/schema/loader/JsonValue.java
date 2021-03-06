package org.everit.json.schema.loader;

import org.everit.json.schema.SchemaException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class JsonValue {

    class Multiplexer<R> {

        protected Map<Class<?>, Function<?, R>> actions = new HashMap<>();

        Multiplexer(Class<?> expectedType, Function<?, R> mapper) {
            actions.put(expectedType, mapper);
        }

        <T> Multiplexer<R> orMappedTo(Class<T> expectedType, Function<T, R> mapper) {
            actions.put(expectedType, mapper);
            return this;
        }

        R requireAny() {
            if (typeOfValue() == null) {
                throw multiplexFailure();
            }
            Function<Object, R> consumer = (Function<Object, R>) actions.keySet().stream()
                    .filter(clazz -> clazz.isAssignableFrom(typeOfValue()))
                    .findFirst()
                    .map(actions::get)
                    .orElseThrow(() -> multiplexFailure());
            return consumer.apply(value());
        }

        private SchemaException multiplexFailure() {
            return ls.createSchemaException(typeOfValue(), actions.keySet());
        }

    }

    class VoidMultiplexer extends Multiplexer<Void> {

        VoidMultiplexer(Class<?> expectedType, Consumer<?> consumer) {
            super(expectedType, obj -> {
                ((Consumer<Object>) consumer).accept(obj);
                return null;
            });
        }

        <T> VoidMultiplexer or(Class<T> expectedType, Consumer<T> consumer) {
            actions.put(expectedType,  obj -> {
                ((Consumer<Object>) consumer).accept(obj);
                return null;
            });
            return this;
        }

    }

    private static final Function<?, ?> IDENTITY = e -> e;

    static final <T, R> Function<T,  R> identity() {
        return (Function<T, R>) IDENTITY;
    }

    static JsonValue of(Object obj, LoadingState ls) {
        if (obj instanceof Map) {
            return new JsonObject((Map<String, Object>) obj, ls);
        } else if (obj instanceof List) {
            return new JsonArray((List<Object>) obj, ls);
        } else if (obj instanceof JSONObject) {
            JSONObject jo = (JSONObject) obj;
            return new JsonObject(jo.toMap(), ls);
        } else if (obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            return new JsonArray(arr.toList(), ls);
        }
        return new JsonValue(obj, ls);
    }

    protected Object value() {
        return obj;
    }

    protected Object unwrap() {
        return value();
    }

    private final Object obj;

    protected LoadingState ls;

    // only called from JsonObject
    protected JsonValue(Object obj) {
        this.obj = obj;
    }

    protected JsonValue(Object obj, LoadingState ls) {
        this.obj = obj;
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    public <T> VoidMultiplexer canBe(Class<T> expectedType, Consumer<T> consumer) {
        return new VoidMultiplexer(expectedType, consumer);
    }

    public <T, R> Multiplexer<R> canBeMappedTo(Class<T> expectedType, Function<T, R> mapper) {
        return new Multiplexer<R>(expectedType, mapper);
    }

    protected Class<?> typeOfValue() {
        return obj == null ? null : obj.getClass();
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JsonValue that = (JsonValue) o;

        return obj != null ? obj.equals(that.obj) : that.obj == null;

    }

    @Override public int hashCode() {
        return obj != null ? obj.hashCode() : 0;
    }

    @Override public String toString() {
        return "JsonValue{" +
                "obj=" + obj +
                '}';
    }

    public String requireString() {
        return requireString(identity());
    }

    public <R> R requireString(Function<String, R> mapper) {
        if (obj instanceof String) {
            return mapper.apply((String) obj);
        }
        throw ls.createSchemaException(typeOfValue(), String.class);
    }

    public Boolean requireBoolean() {
        return requireBoolean(identity());
    }

    public <R> R requireBoolean(Function<Boolean, R> mapper) {
        if (obj instanceof Boolean) {
            return mapper.apply((Boolean) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Boolean.class);
    }

    public JsonObject requireObject() {
        return requireObject(identity());
    }

    public <R> R requireObject(Function<JsonObject, R> mapper) {
        throw ls.createSchemaException(typeOfValue(), JsonObject.class);
    }

    public JsonArray requireArray() {
        return requireArray(identity());
    }

    public <R> R requireArray(Function<JsonArray, R> mapper) {
        throw ls.createSchemaException(typeOfValue(), JsonArray.class);
    }

    public Number requireNumber() {
        return requireNumber(identity());
    }

    public <R> R requireNumber(Function<Number, R> mapper) {
        if (obj instanceof Number) {
            return mapper.apply((Number) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Number.class);
    }

    public Integer requireInteger() {
        return requireInteger(identity());
    }

    public <R> R requireInteger(Function<Integer, R> mapper) {
        if (obj instanceof Integer) {
            return mapper.apply((Integer) obj);
        }
        throw ls.createSchemaException(typeOfValue(), Integer.class);
    }
}

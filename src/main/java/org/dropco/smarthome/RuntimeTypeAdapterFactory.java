//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.dropco.smarthome;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    private final Class<?> baseType;
    private final String typeFieldName;
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap();
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap();
    private final boolean maintainType;

    private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName, boolean maintainType) {
        if (typeFieldName != null && baseType != null) {
            this.baseType = baseType;
            this.typeFieldName = typeFieldName;
            this.maintainType = maintainType;
        } else {
            throw new NullPointerException();
        }
    }

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName, boolean maintainType) {
        return new RuntimeTypeAdapterFactory(baseType, typeFieldName, maintainType);
    }

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new RuntimeTypeAdapterFactory(baseType, typeFieldName, false);
    }

    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
        return new RuntimeTypeAdapterFactory(baseType, "type", false);
    }

    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type != null && label != null) {
            if (!this.subtypeToLabel.containsKey(type) && !this.labelToSubtype.containsKey(label)) {
                this.labelToSubtype.put(label, type);
                this.subtypeToLabel.put(type, label);
                return this;
            } else {
                throw new IllegalArgumentException("types and labels must be unique");
            }
        } else {
            throw new NullPointerException();
        }
    }

    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
        return this.registerSubtype(type, type.getSimpleName());
    }

    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != this.baseType) {
            return null;
        } else {
            final Map<String, TypeAdapter<?>> labelToDelegate = new LinkedHashMap();
            final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap();
            Iterator var5 = this.labelToSubtype.entrySet().iterator();

            while(var5.hasNext()) {
                Entry<String, Class<?>> entry = (Entry)var5.next();
                TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get((Class)entry.getValue()));
                labelToDelegate.put(entry.getKey(), delegate);
                subtypeToDelegate.put(entry.getValue(), delegate);
            }

            return (new TypeAdapter<R>() {
                public R read(JsonReader in) throws IOException {
                    JsonElement jsonElement = Streams.parse(in);
                    JsonElement labelJsonElement;
                    if (RuntimeTypeAdapterFactory.this.maintainType) {
                        labelJsonElement = jsonElement.getAsJsonObject().get(RuntimeTypeAdapterFactory.this.typeFieldName);
                    } else {
                        labelJsonElement = jsonElement.getAsJsonObject().remove(RuntimeTypeAdapterFactory.this.typeFieldName);
                    }

                    if (labelJsonElement == null) {
                        throw new JsonParseException("cannot deserialize " + RuntimeTypeAdapterFactory.this.baseType + " because it does not define a field named " + RuntimeTypeAdapterFactory.this.typeFieldName);
                    } else {
                        String label = labelJsonElement.getAsString();
                        TypeAdapter<R> delegate = (TypeAdapter)labelToDelegate.get(label);
                        if (delegate == null) {
                            throw new JsonParseException("cannot deserialize " + RuntimeTypeAdapterFactory.this.baseType + " subtype named " + label + "; did you forget to register a subtype?");
                        } else {
                            return delegate.fromJsonTree(jsonElement);
                        }
                    }
                }

                public void write(JsonWriter out, R value) throws IOException {
                    Class<?> srcType = value.getClass();
                    String label = (String)RuntimeTypeAdapterFactory.this.subtypeToLabel.get(srcType);
                    TypeAdapter<R> delegate = (TypeAdapter)subtypeToDelegate.get(srcType);
                    if (delegate == null) {
                        throw new JsonParseException("cannot serialize " + srcType.getName() + "; did you forget to register a subtype?");
                    } else {
                        JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                        if (RuntimeTypeAdapterFactory.this.maintainType) {
                            Streams.write(jsonObject, out);
                        } else {
                            JsonObject clone = new JsonObject();
                            if (jsonObject.has(RuntimeTypeAdapterFactory.this.typeFieldName)) {
                                throw new JsonParseException("cannot serialize " + srcType.getName() + " because it already defines a field named " + RuntimeTypeAdapterFactory.this.typeFieldName);
                            } else {
                                clone.add(RuntimeTypeAdapterFactory.this.typeFieldName, new JsonPrimitive(label));
                                Iterator var8 = jsonObject.entrySet().iterator();

                                while(var8.hasNext()) {
                                    Entry<String, JsonElement> e = (Entry)var8.next();
                                    clone.add((String)e.getKey(), (JsonElement)e.getValue());
                                }

                                Streams.write(clone, out);
                            }
                        }
                    }
                }
            }).nullSafe();
        }
    }
}

//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package com.intellectualsites.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility responsible for serializing and de-serializing HTTP entities
 */
public final class EntityMapper {

    private final Map<Class<?>, EntitySerializer<?>> serializers = new HashMap<>();
    private final Map<Class<?>, EntityDeserializer<?>> deserializers = new HashMap<>();

    @SuppressWarnings("ALL") private static <T> T castUnsafe(@NotNull final Object o) {
        return (T) o;
    }

    /**
     * Create a new entity mapper instance
     *
     * @return Created instance
     */
    @NotNull public static EntityMapper newInstance() {
        final EntityMapper mapper = new EntityMapper();
        mapper.registerDeserializer(String.class, new StringDeserializer());
        mapper.registerSerializer(String.class, new StringSerializer());
        return mapper;
    }

    /**
     * Register a serializer that maps a given type to an array of bytes
     *
     * @param clazz      Class of type to map
     * @param serializer Serializer that performs the mapping
     * @param <T>        Type to map
     * @return Mapper instance
     */
    @NotNull public <T> EntityMapper registerSerializer(@NotNull final Class<T> clazz,
        @NotNull final EntitySerializer<T> serializer) {
        Objects.requireNonNull(clazz, "Class may not be null");
        Objects.requireNonNull(serializer, "Serializer may not be null");
        this.serializers.put(clazz, serializer);
        return this;
    }

    /**
     * Register a deserializer that maps objects of a certain content type to Java objects
     *
     * @param clazz        Content type
     * @param deserializer Deserializer
     * @param <T>          Type of the objects produces by the deserializer
     * @return Mapper instance
     */
    @NotNull public <T> EntityMapper registerDeserializer(@NotNull final Class<T> clazz,
        @NotNull final EntityDeserializer<T> deserializer) {
        Objects.requireNonNull(clazz, "Type may not be null");
        Objects.requireNonNull(deserializer, "Deserializer may not be null");
        this.deserializers.put(clazz, deserializer);
        return this;
    }

    /**
     * Attempt to retrieve the serializer for a given type
     *
     * @param clazz Class
     * @param <T>   Type
     * @return Serializer
     */
    public <T> Optional<EntitySerializer<T>> getSerializer(@NotNull final Class<T> clazz) {
        final EntitySerializer<?> serializer = this.serializers.get(clazz);
        if (serializer == null) {
            return Optional.empty();
        }
        return Optional.of(castUnsafe(serializer));
    }

    /**
     * Attempt to retrieve the deserializer for a given content type
     *
     * @param type Content type
     * @return Deserializer
     */
    public <T> Optional<EntityDeserializer<T>> getDeserialiser(@NotNull final Class<T> type) {
        final EntityDeserializer<?> entityDeserializer = this.deserializers.get(type);
        if (entityDeserializer == null) {
            return Optional.empty();
        }
        return Optional.of((EntityDeserializer<T>) entityDeserializer);
    }


    /**
     * Serializer for HTTP request bodies
     *
     * @param <T> Object type
     */
    public interface EntitySerializer<T> {

        /**
         * Serialize the input into a byte array, which can then be
         * written to the HTTP request
         *
         * @param input Input that should be serialized
         * @return The serialized object
         */
        @NotNull byte[] serialize(@NotNull final T input);

        /**
         * Get the content type of the object
         *
         * @return Content Type
         */
        ContentType getContentType();

    }


    /**
     * Deserializer for HTTP response bodies
     *
     * @param <T> Object type
     */
    @FunctionalInterface
    public interface EntityDeserializer<T> {

        /**
         * Deserialize the input byte array into an object.
         *
         * @param contentType Optional content type, if supplied by the server
         * @param input Input that should be de-serialized
         * @return De-serialized input
         */
        @NotNull T deserialize(@Nullable final ContentType contentType, @NotNull final byte[] input);

    }


    private static final class StringDeserializer implements EntityDeserializer<String> {

        @NotNull @Override public String deserialize(@Nullable final ContentType contentType, @NotNull final byte[] input) {
            final Charset charset;
            if (contentType != null && contentType.toString().toLowerCase().contains("utf-8")) {
                charset = StandardCharsets.UTF_8;
            } else if (contentType != null && contentType.toString().toLowerCase().contains("utf-16")) {
                charset = StandardCharsets.UTF_16;
            } else {
                charset = StandardCharsets.US_ASCII;
            }
            return new String(input, charset);
        }

    }


    private static final class StringSerializer implements EntitySerializer<String> {

        @NotNull @Override public byte[] serialize(@NotNull final String string) {
            return string.getBytes(StandardCharsets.UTF_8);
        }

        @Override public ContentType getContentType() {
            return ContentType.STRING_UTF8;
        }

    }

}

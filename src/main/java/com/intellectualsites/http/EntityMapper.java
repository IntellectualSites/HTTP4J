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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility responsible for serializing and de-serializing HTTP entities
 */
public final class EntityMapper {

    private final Map<Class<?>, EntitySerializer<?>> serializers = new HashMap<>();
    private final Map<ContentType, EntityDeserializer<?>> deserializers = new HashMap<>();

    @SuppressWarnings("ALL") private static <T> T castUnsafe(@NotNull final Object o) {
        return (T) o;
    }

    /**
     * Register a serializer that maps a given type to an array of bytes
     *
     * @param clazz      Class of type to map
     * @param serializer Serializer that performs the mapping
     * @param <T>        Type to map
     */
    public <T> void registerSerializer(@NotNull final Class<T> clazz,
        @NotNull final EntitySerializer<T> serializer) {
        Objects.requireNonNull(clazz, "Class may not be null");
        Objects.requireNonNull(serializer, "Serializer may not be null");
        this.serializers.put(clazz, serializer);
    }

    /**
     * Register a deserializer that maps objects of a certain content type to Java objects
     *
     * @param type         Content type
     * @param deserializer Deserializer
     * @param <T>          Type of the objects produces by the deserializer
     */
    public <T> void registerDeserializer(@NotNull final ContentType type,
        @NotNull final EntityDeserializer<T> deserializer) {
        Objects.requireNonNull(type, "Type may not be null");
        Objects.requireNonNull(deserializer, "Deserializer may not be null");
        this.deserializers.put(type, deserializer);
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
    public Optional<EntityDeserializer<?>> getDeserialiser(@NotNull final ContentType type) {
        return Optional.ofNullable(this.deserializers.get(type));
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
         * @param input Input that should be de-serialized
         * @return De-serialized input
         */
        @NotNull T deserialize(@NotNull final byte[] input);

    }

}

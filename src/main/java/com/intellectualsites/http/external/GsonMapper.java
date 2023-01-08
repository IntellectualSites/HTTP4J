/*
 * MIT License
 *
 * Copyright (c) 2022 IntellectualSites
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.intellectualsites.http.external;

import com.google.gson.Gson;
import com.intellectualsites.http.ContentType;
import com.intellectualsites.http.EntityMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Class containing {@link EntityMapper mappers} for {@link Gson} objects
 */
public final class GsonMapper {

    /**
     * Create a new serializer
     *
     * @param clazz Input class
     * @param gson  Gson instance
     * @param <T>   Input type
     * @return Serializer for the input type
     */
    @NotNull public static <T> GsonSerializer<T> serializer(@NotNull final Class<T> clazz,
        @NotNull final Gson gson) {
        return new GsonSerializer<>(clazz, gson);
    }

    /**
     * Create a new deserializer
     *
     * @param clazz Output class
     * @param gson  Gson instance
     * @param <T>   Output type
     * @return Deserializer for the output type
     */
    @NotNull public static <T> GsonDeserializer<T> deserializer(@NotNull final Class<T> clazz,
        @NotNull final Gson gson) {
        return new GsonDeserializer<>(clazz, gson);
    }


    private static final class GsonSerializer<T> implements EntityMapper.EntitySerializer<T> {

        private final Class<T> clazz;
        private final Gson gson;

        private GsonSerializer(@NotNull final Class<T> clazz, @NotNull final Gson gson) {
            this.clazz = clazz;
            this.gson = gson;
        }

        @Override @NotNull public byte[] serialize(@NotNull final T input) {
            return this.gson.toJson(input).getBytes(StandardCharsets.UTF_8);
        }

        @Override public ContentType getContentType() {
            return ContentType.JSON;
        }

    }


    private static final class GsonDeserializer<T> implements EntityMapper.EntityDeserializer<T> {

        private final Class<T> clazz;
        private final Gson gson;

        private GsonDeserializer(@NotNull final Class<T> clazz, @NotNull final Gson gson) {
            this.clazz = clazz;
            this.gson = gson;
        }

        @NotNull @Override
        public T deserialize(@Nullable final ContentType contentType, @NotNull final byte[] input) {
            final Charset charset;
            if (contentType != null && contentType.toString().toLowerCase().contains("utf-8")) {
                charset = StandardCharsets.UTF_8;
            } else if (contentType != null && contentType.toString().toLowerCase()
                .contains("utf-16")) {
                charset = StandardCharsets.UTF_16;
            } else {
                charset = StandardCharsets.US_ASCII;
            }
            return gson.fromJson(new String(input, charset), this.clazz);
        }

    }

}

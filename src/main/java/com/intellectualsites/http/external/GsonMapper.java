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
            return this.gson.toJson(clazz).getBytes(StandardCharsets.UTF_8);
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

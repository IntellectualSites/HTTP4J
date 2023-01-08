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
package com.intellectualsites.http;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A HTTP response
 */
public final class HttpResponse {

    private final Headers headers;
    private final EntityMapper entityMapper;
    private final int code;
    private final String status;
    private final byte[] body;

    private HttpResponse(final int code,
                         @NotNull final String status,
                         @NotNull final Headers headers,
                         @NotNull final EntityMapper entityMapper,
                         @NotNull final byte[] body) {
        this.status = status;
        this.code = code;
        this.headers = headers;
        this.entityMapper = entityMapper;
        this.body = body;
    }

    /**
     * Create a new builder instance
     *
     * @return Builder instance
     */
    @NotNull static Builder builder() {
        return new Builder();
    }

    /**
     * Get the HTTP status message
     *
     * @return Status message
     */
    @NotNull public String getStatus() {
        return this.status;
    }

    /**
     * Get the HTTP status code
     *
     * @return Status code
     */
    public int getStatusCode() {
        return this.code;
    }

    /**
     * Get the raw response body
     *
     * @return Response body
     */
    @NotNull public byte[] getRawResponse() {
        return this.body;
    }

    /**
     * Get the response headers
     *
     * @return Response headers
     */
    @NotNull public Headers getHeaders() {
        return this.headers;
    }

    /**
     * Get the response entity and map it to a specific type
     *
     * @param returnType Return type class
     * @param <T> Return type
     * @return Response
     * @throws IllegalArgumentException If no mapper exists for the type
     */
    @NotNull public <T> T getResponseEntity(@NotNull final Class<T> returnType) {
        final String contentTypeString = this.headers.getOrDefault("content-type", null);
        final ContentType contentType;
        if (contentTypeString != null) {
            contentType = ContentType.of(contentTypeString);
        } else {
            contentType = null;
        }

        return this.entityMapper.getDeserializer(returnType).map(deserializer ->
            deserializer.deserialize(contentType, this.getRawResponse()))
            .orElseThrow(() -> new IllegalStateException(String.format("Could not deserialize response into type '%s'",
                returnType.getCanonicalName())));
    }


    static class Builder {

        private final Headers headers = Headers.newInstance();
        private int status;
        private String statusMessage;
        private EntityMapper entityMapper;
        private byte[] bytes = new byte[0];

        private Builder() {
        }

        @NotNull Builder withStatus(final int status) {
            this.status = status;
            return this;
        }

        @NotNull Builder withStatusMessage(@NotNull final String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        @NotNull Builder withHeader(@NotNull final String key, @NotNull final String value) {
            this.headers.addHeader(Objects.requireNonNull(key, "Key may not be null"),
                Objects.requireNonNull(value, "Value may not be null"));
            return this;
        }

        @NotNull Builder withEntityMapper(@NotNull final EntityMapper entityMapper) {
            this.entityMapper = Objects.requireNonNull(entityMapper, "Mapper may not be null");
            return this;
        }

        @NotNull Builder withBody(@NotNull final byte[] bytes) {
            this.bytes = Objects.requireNonNull(bytes, "Bytes may not be null");
            return this;
        }

        @NotNull HttpResponse build() {
            return new HttpResponse(this.status, this.statusMessage,
                this.headers, this.entityMapper, this.bytes);
        }

    }

}

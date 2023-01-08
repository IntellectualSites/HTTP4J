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
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * HTTP request class. This should not be interacted with directly,
 * rather {@link com.intellectualsites.http.HttpClient} should be used
 */
final class HttpRequest {

    private static final int READ_TIMEOUT = 3600000;

    @NotNull private final HttpMethod method;
    @NotNull private final URL url;
    @NotNull private final Headers headers;
    @NotNull private final EntityMapper mapper;
    @Nullable private final Supplier<Object> inputSupplier;
    @NotNull private final Consumer<Throwable> throwableConsumer;

    private HttpRequest(@NotNull final HttpMethod method, @NotNull final URL url, @NotNull final Headers headers,
        @Nullable Supplier<Object> inputSupplier, @NotNull final EntityMapper mapper,
        @NotNull final Consumer<Throwable> throwableConsumer) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.inputSupplier = inputSupplier;
        this.mapper = mapper;
        this.throwableConsumer = throwableConsumer;
    }

    /**
     * Create a new request {@link Builder builder}
     *
     * @return Builder instance
     */
    static Builder newBuilder() {
        return new Builder();
    }

    @Nullable HttpResponse executeRequest() throws IOException {
        final HttpURLConnection httpURLConnection = (HttpURLConnection) this.url.openConnection();
        try {
            httpURLConnection.setRequestMethod(this.method.name());
            httpURLConnection.setDoOutput(this.method.hasBody());
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setReadTimeout(READ_TIMEOUT);
            for (final String headerName : this.headers.getHeaders()) {
                final List<String> headers = this.headers.getHeaders(headerName);
                if (headers.size() == 1) {
                    httpURLConnection.addRequestProperty(headerName, headers.get(0));
                } else if (headers.size() > 1) {
                    final StringBuilder headerBuilder = new StringBuilder();
                    final Iterator<String> headerIterator = headers.iterator();
                    while (headerIterator.hasNext()) {
                        headerBuilder.append(headerIterator.next());
                        if (headerIterator.hasNext()) {
                            headerBuilder.append(',');
                        }
                    }
                    httpURLConnection.addRequestProperty(headerName, headerBuilder.toString());
                }
            }
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(this.inputSupplier != null);
            if (this.inputSupplier != null) {
                final Object object = this.inputSupplier.get();
                if (object != null) {
                    final EntityMapper.EntitySerializer serializer =
                        this.mapper.getSerializer(object.getClass()).orElseThrow(() -> new IllegalArgumentException(String
                            .format("There is no registered serializer for type '%s'",
                                object.getClass().getCanonicalName())));
                    if (this.headers.getHeader("Content-Type").isEmpty()) {
                        httpURLConnection.setRequestProperty("Content-Type", serializer.getContentType().toString());
                    }
                    final byte[] bytes = serializer.serialize(object);
                    httpURLConnection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                    try (final DataOutputStream dataOutputStream = new DataOutputStream(
                        httpURLConnection.getOutputStream())) {
                        dataOutputStream.write(bytes);
                        dataOutputStream.flush();
                    }
                }
            }
            httpURLConnection.connect();

            final InputStream stream;
            if (this.method.hasBody()) {
                if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    stream = httpURLConnection.getErrorStream();
                } else {
                    stream = httpURLConnection.getInputStream();
                }
            } else {
                stream = null;
            }

            final HttpResponse.Builder builder = HttpResponse.builder()
                .withStatus(httpURLConnection.getResponseCode())
                .withStatusMessage(httpURLConnection.getResponseMessage())
                .withEntityMapper(this.mapper);
            for (final Map.Entry<String, List<String>> entry : httpURLConnection.getHeaderFields().entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                for (final String header : entry.getValue()) {
                    builder.withHeader(entry.getKey(), header);
                }
            }

            if (stream != null) {
                try (final InputStream copy = stream; final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    int b;
                    while ((b = stream.read()) != -1) {
                        bos.write(b);
                    }
                    builder.withBody(bos.toByteArray());
                }
            }

            return builder.build();
        } catch (final Throwable throwable) {
            throwableConsumer.accept(throwable);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return null;
    }


    static final class Builder {

        private final Headers headers = Headers.newInstance();
        private EntityMapper mapper;
        private HttpMethod method;
        private URL url;
        private Supplier<Object> inputSupplier;
        private Consumer<Throwable> throwableConsumer = Throwable::printStackTrace;

        private Builder() {
        }

        /**
         * Specify the entity mapper used by the request
         *
         * @param mapper Entity mapper
         * @return Builder instance
         */
        @NotNull Builder withMapper(@NotNull final EntityMapper mapper) {
            this.mapper = Objects.requireNonNull(mapper, "Mapper may not be null");
            return this;
        }

        /**
         * Specify the HTTP method used in the request
         *
         * @param method HTTP method
         * @return Builder instance
         */
        @NotNull Builder withMethod(@NotNull final HttpMethod method) {
            this.method = Objects.requireNonNull(method, "Method may not be null");
            return this;
        }

        /**
         * Specify the URL used in the request
         *
         * @param url URL
         * @return Builder instance
         */
        @NotNull Builder withURL(@NotNull final URL url) {
            this.url = Objects.requireNonNull(url, "URL may not be null");
            return this;
        }

        /**
         * Add a header to the request
         *
         * @param key Header key
         * @param value Header value
         * @return Builder instance
         */
        @NotNull Builder withHeader(@NotNull final String key, @NotNull final String value) {
            this.headers.addHeader(Objects.requireNonNull(key, "Key may not be null"), Objects.requireNonNull(value, "Value may not be null"));
            return this;
        }

        /**
         * Add an input entity to the request
         *
         * @param inputSupplier Input supplier
         * @return Builder instance
         */
        @NotNull Builder withInput(@NotNull final Supplier<Object> inputSupplier) {
            this.inputSupplier = Objects.requireNonNull(inputSupplier, "Input supplier may not be null");
            return this;
        }

        /**
         * Add a throwable consumer
         *
         * @param consumer Throwable consumer
         * @return Builder instance
         */
        @NotNull Builder onException(@NotNull final Consumer<Throwable> consumer) {
            this.throwableConsumer = Objects.requireNonNull(consumer, "Consumer may not be null");
            return this;
        }

        @NotNull HttpRequest build() {
            Objects.requireNonNull(this.method, "No method was supplied");
            Objects.requireNonNull(this.url, "No URL was supplied");
            Objects.requireNonNull(this.mapper, "No mapper was supplied");
            Objects.requireNonNull(this.throwableConsumer, "No throwable consumer was supplied");
            return new HttpRequest(this.method, this.url, this.headers,
                this.inputSupplier, this.mapper, this.throwableConsumer);
        }

    }
    
}

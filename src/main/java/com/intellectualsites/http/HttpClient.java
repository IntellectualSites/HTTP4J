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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A simple Java HTTP client
 */
public final class HttpClient {

    private final EntityMapper mapper = new EntityMapper();
    private final ClientSettings settings;

    private HttpClient(@NotNull final ClientSettings settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    /**
     * Create a new {@link Builder}
     *
     * @return Builder instance
     */
    @NotNull public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Initialise a request builder for a GET request
     *
     * @param url URL
     * @return Created builder
     */
    @NotNull public WrappedRequestBuilder get(@NotNull final String url) {
        Objects.requireNonNull(url, "URL may not be null");
        return new WrappedRequestBuilder(HttpMethod.GET, url);
    }

    /**
     * Initialise a request builder for a POST request
     *
     * @param url URL
     * @return Created builder
     */
    @NotNull public WrappedRequestBuilder post(@NotNull final String url) {
        Objects.requireNonNull(url, "URL may not be null");
        return new WrappedRequestBuilder(HttpMethod.POST, url);
    }

    /**
     * Initialise a request builder for a PUT request
     *
     * @param url URL
     * @return Created builder
     */
    @NotNull public WrappedRequestBuilder put(@NotNull final String url) {
        Objects.requireNonNull(url, "URL may not be null");
        return new WrappedRequestBuilder(HttpMethod.PUT, url);
    }

    /**
     * Initialise a request builder for a HEAD request
     *
     * @param url URL
     * @return Created builder
     */
    @NotNull public WrappedRequestBuilder head(@NotNull final String url) {
        Objects.requireNonNull(url, "URL may not be null");
        return new WrappedRequestBuilder(HttpMethod.HEAD, url);
    }

    /**
     * Initialise a request builder for a DELETE request
     *
     * @param url URL
     * @return Created builder
     */
    @NotNull public WrappedRequestBuilder delete(@NotNull final String url) {
        Objects.requireNonNull(url, "URL may not be null");
        return new WrappedRequestBuilder(HttpMethod.DELETE, url);
    }

    /**
     * Initialise a request builder for a PATCH request
     *
     * @param url URL
     * @return Created builder
     */
    @NotNull public WrappedRequestBuilder patch(@NotNull final String url) {
        Objects.requireNonNull(url, "URL may not be null");
        return new WrappedRequestBuilder(HttpMethod.PATCH, url);
    }

    /**
     * Get the entity mapper used by the client
     *
     * @return Entity mapper
     */
    @NotNull public EntityMapper getMapper() {
        return this.mapper;
    }


    /**
     * Builder for {@link HttpClient}. Use {@link #newBuilder()} to create
     * an instance of the builder
     */
    public static final class Builder {

        private final ClientSettings settings;

        private Builder() {
            this.settings = new ClientSettings();
        }

        /**
         * Set the base URL. This will be prepended to each request made with the
         * client.
         *
         * @param baseURL Base URL. Cannot be null, but can be empty.
         * @return Builder instance
         */
        @NotNull public Builder withBaseURL(@NotNull final String baseURL) {
            Objects.requireNonNull(baseURL, "Base URL may not be null");
            if (baseURL.endsWith("/")) {
                this.settings.setBaseURL(baseURL.substring(0, baseURL.length() - 1));
            } else {
                this.settings.setBaseURL(baseURL);
            }
            return this;
        }

        /**
         * Set the default entity mapper that is used
         * by all requests, unless otherwise specified
         *
         * @param entityMapper Entity mapper
         * @return Builder instance
         */
        @NotNull public Builder withEntityMapper(@Nullable final EntityMapper entityMapper) {
            this.settings.setEntityMapper(entityMapper);
            return this;
        }

        /**
         * Add a new request decorator. This will have the opportunity
         * to decorate every request made by this client
         *
         * @param decorator Decorator
         * @return Builder instance
         */
        @NotNull public Builder withDecorator(@NotNull final Consumer<WrappedRequestBuilder> decorator) {
            this.settings.addDecorator(Objects.requireNonNull(decorator, "Decorator may not be null"));
            return this;
        }

        /**
         * Create a new {@link HttpClient} using the
         * settings specified in the builder
         *
         * @return Created client
         */
        public HttpClient build() {
            return new HttpClient(settings);
        }

    }


    /**
     * Wrapper used to interact with HTTP requests.
     *
     * @see #get(String) To create a new GET request
     * @see #post(String) To create a new POST request
     * @see #head(String) To create a new HEAD request
     * @see #put(String) To create a new PUT request
     * @see #patch(String) To create a new PATCH request
     * @see #delete(String) To create a new DELETE request
     */
    public final class WrappedRequestBuilder {

        private final HttpRequest.Builder builder = HttpRequest.newBuilder();
        private final Map<Integer, Consumer<HttpResponse>> consumers = new HashMap<>();
        private Consumer<HttpResponse> other = response -> {
        };
        private Consumer<Throwable> exceptionHandler = null;

        private WrappedRequestBuilder(@NotNull final HttpMethod method, @NotNull String url) {
            if (url.startsWith("/")) {
                if (url.length() == 1) {
                    url = "";
                } else {
                    url = url.substring(1);
                }
            }
            url = HttpClient.this.settings.getBaseURL() + '/' + url;
            try {
                final URL javaURL = new URL(url);
                builder.withURL(javaURL);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            builder.withMethod(method);
            if (settings.getEntityMapper() != null) {
                builder.withMapper(settings.getEntityMapper());
            }
        }

        /**
         * Specify an input supplier which will be used to write to the connection, if it
         * established correctly. This requires that there is a {@link com.intellectualsites.http.EntityMapper.EntitySerializer}
         * registered for the type of the object, in the {@link EntityMapper} used by the client
         *
         * @param input Input
         * @return Builder instance
         */
        @NotNull public WrappedRequestBuilder withInput(@NotNull final Supplier<Object> input) {
            builder.withInput(input);
            return this;
        }

        /**
         * Specify the entity mapper used by the request
         *
         * @param mapper Entity mapper
         * @return Builder instance
         */
        @NotNull public WrappedRequestBuilder withMapper(@NotNull final EntityMapper mapper) {
            builder.withMapper(mapper);
            return this;
        }

        /**
         * Add a header to the request
         *
         * @param key   Header key
         * @param value Header value
         * @return Builder instance
         */
        @NotNull public WrappedRequestBuilder withHeader(@NotNull final String key,
            @NotNull final String value) {
            builder.withHeader(key, value);
            return this;
        }

        /**
         * Add a consumer that acts on a specific status code
         *
         * @param code             Status code
         * @param responseConsumer Response consumer
         * @return Builder instance
         */
        @NotNull public WrappedRequestBuilder onStatus(final int code,
            @NotNull final Consumer<HttpResponse> responseConsumer) {
            consumers.put(code, responseConsumer);
            return this;
        }

        /**
         * Add a consumer that acts on all remaining status code
         *
         * @param responseConsumer Response consumer
         * @return Builder instance
         */
        @NotNull public WrappedRequestBuilder onRemaining(
            @NotNull final Consumer<HttpResponse> responseConsumer) {
            this.other = responseConsumer;
            return this;
        }

        /**
         * Add an exception consumer
         *
         * @param consumer Exception consumer
         * @return Builder instance
         */
        @NotNull public WrappedRequestBuilder onException(
            @NotNull final Consumer<Throwable> consumer) {
            this.exceptionHandler = consumer;
            builder.onException(consumer);
            return this;
        }

        /**
         * Perform the request
         *
         * @return The raw response, if no exception was thrown during the
         *         handling of the response. If any exception was handled,
         *         the method will return {@code null}
         */
        @Nullable public HttpResponse execute() {
            for (final Consumer<WrappedRequestBuilder> decorator : settings.getRequestDecorators()) {
                decorator.accept(this);
            }
            try {
                final Throwable[] throwables = new Throwable[1];
                if (this.exceptionHandler == null) {
                    builder.onException(e -> throwables[0] = e);
                }
                final HttpResponse response = this.builder.build().executeRequest();
                if (response != null) {
                    final Consumer<HttpResponse> responseConsumer = this.consumers.getOrDefault(response.getStatusCode(), this.other);
                    responseConsumer.accept(response);
                }
                if (throwables[0] != null) {
                    if (throwables[0] instanceof RuntimeException) {
                        throw (RuntimeException) throwables[0];
                    }
                    throw new RuntimeException(throwables[0]);
                } else {
                    return response;
                }
            } catch (final Exception e) {
                if (this.exceptionHandler == null) {
                    if (e instanceof RuntimeException) {
                        throw ((RuntimeException) e);
                    }
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

    }

}

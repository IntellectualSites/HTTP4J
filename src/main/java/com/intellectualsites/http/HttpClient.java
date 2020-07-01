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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A simple Java HTTP client
 */
public final class HttpClient {

    private static final Logger logger = Logger.getLogger(HttpClient.class.getCanonicalName());

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
            Objects.requireNonNull(baseURL);
            if (baseURL.endsWith("/")) {
                this.settings.setBaseURL(baseURL.substring(0, baseURL.length() - 1));
            } else {
                this.settings.setBaseURL(baseURL);
            }
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


    public final class WrappedRequestBuilder {

        private final HttpRequest.Builder builder = HttpRequest.newBuilder();

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
                final URL javaURL = new URL(URLEncoder.encode(url, "UTF-8"));
            } catch (MalformedURLException | UnsupportedEncodingException e) {
                logger.severe("Malformed URL: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

}

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
package com.intellectualsites.http.request;

import com.intellectualsites.http.Headers;
import com.intellectualsites.http.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * HTTP request class. This should not be interacted with directly,
 * rather {@link com.intellectualsites.http.HttpClient} should be used
 */
public final class HttpRequest {

    private final HttpMethod method;
    private final URL url;
    private final Headers headers;

    private HttpRequest(@NotNull final HttpMethod method, @NotNull final URL url, @NotNull final Headers headers) {
        this.method = method;
        this.url = url;
        this.headers = headers;
    }

    /**
     * Create a new request {@link Builder builder}
     *
     * @return Builder instance
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public void executeRequest() throws IOException {
        final HttpURLConnection httpURLConnection = (HttpURLConnection) this.url.openConnection();
        httpURLConnection.setRequestMethod(this.method.name());
        httpURLConnection.setDoOutput(this.method.hasBody());
        httpURLConnection.setUseCaches(false);
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

    }


    public static final class Builder {

        private final Headers headers = Headers.newInstance();
        private HttpMethod method;
        private URL url;

        private Builder() {
        }

        /**
         * Specify the HTTP method used in the request
         *
         * @param method HTTP method
         * @return Builder instance
         */
        @NotNull public Builder withMethod(@NotNull final HttpMethod method) {
            this.method = Objects.requireNonNull(method, "Method may not be null");
            return this;
        }

        /**
         * Specify the URL used in the request
         *
         * @param url URL
         * @return Builder instance
         */
        @NotNull public Builder withURL(@NotNull final URL url) {
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
        @NotNull public Builder withHeader(@NotNull final String key, @NotNull final String value) {
            this.headers.addHeader(Objects.requireNonNull(key, "Key may not be null"), Objects.requireNonNull(value, "Value may not be null"));
            return this;
        }

        @NotNull public HttpRequest build() {
            Objects.requireNonNull(this.method, "No method was supplied");
            Objects.requireNonNull(this.url, "No URL was supplied");
            return new HttpRequest(this.method, this.url, this.headers);
        }

    }
    
}

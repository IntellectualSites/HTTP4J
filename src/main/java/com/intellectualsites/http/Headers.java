/*
 * MIT License
 *
 * Copyright (c) 2021 IntellectualSites
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Container for HTTP headers
 */
final class Headers {

    private final Map<String, List<String>> headers = new HashMap<>();

    private Headers() {
    }

    /**
     * Return an empty {@link Headers} instance
     *
     * @return Headers instance
     */
    static Headers newInstance() {
        return new Headers();
    }

    /**
     * Add a header to the header collection
     *
     * @param key   Header name
     * @param value Header value
     */
    void addHeader(@NotNull String key, @NotNull final String value) {
        Objects.requireNonNull(key, "Key may not be null");
        Objects.requireNonNull(value, "Value may not be null");
        key = key.toLowerCase();
        if (this.headers.containsKey(key)) {
            this.headers.get(key).add(value);
        } else {
            final List<String> headers = new LinkedList<>();
            headers.add(value);
            this.headers.put(key, headers);
        }
    }

    /**
     * Get a list of all the headers with the specified name
     *
     * @param key Header key
     * @return Unmodifiable list
     */
    @NotNull List<String> getHeaders(@NotNull final String key) {
        Objects.requireNonNull(key, "Key may not be null");
        final List<String> headers = this.headers.get(key.toLowerCase());
        if (headers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(headers);
    }

    /**
     * Get the value of a specific header, or an empty string.
     * If multiple values are specified for the header key,
     * only the last value will be returned.
     *
     * @param key Header key
     * @return Header value, or {@code ""}
     */
    @NotNull String getHeader(@NotNull final String key) {
        return Objects.requireNonNull(getOrDefault(key, ""));
    }

    /**
     * Get the value of a specific header, or default string.
     * If multiple values are specified for the header key,
     * only the last value will be returned.
     *
     * @param key           Header key
     * @param defaultString Default value
     * @return Header value, or the default value
     */
    @Nullable String getOrDefault(@NotNull final String key, @Nullable final String defaultString) {
        final List<String> headers = this.getHeaders(key);
        if (headers.isEmpty()) {
            return defaultString;
        }
        return headers.get(headers.size() - 1);
    }

    /**
     * Get the name of all headers in the collection
     *
     * @return Unmodifiable collection
     */
    @NotNull Collection<String> getHeaders() {
        return Collections.unmodifiableSet(this.headers.keySet());
    }

}

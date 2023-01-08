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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Settings that change the behaviour of {@link HttpClient}
 */
final class ClientSettings {

    private final Collection<Consumer<HttpClient.WrappedRequestBuilder>> decorators = new LinkedList<>();
    private String baseURL;
    private EntityMapper entityMapper;

    ClientSettings() {
        this.baseURL = "";
    }

    /**
     * Base URL that is prepended to the URL
     * of each request
     *
     * @return Base URL
     */
    @NotNull String getBaseURL() {
        return this.baseURL;
    }

    /**
     * Get the entity mapper that should be used
     * in all request (by default)
     *
     * @return Entity mapper
     */
    @Nullable EntityMapper getEntityMapper() {
        return this.entityMapper;
    }

    /**
     * Get all registered request decorators
     *
     * @return Unmodifiable collection of decorators
     */
    @NotNull Collection<Consumer<HttpClient.WrappedRequestBuilder>> getRequestDecorators() {
        return Collections.unmodifiableCollection(this.decorators);
    }

    /**
     * Set the base URL, that is prepended to
     * the URL of each request
     *
     * @param baseURL base URL
     */
    void setBaseURL(@NotNull final String baseURL) {
        this.baseURL = Objects.requireNonNull(baseURL, "Base URL may not be null");
    }

    /**
     * Set the default entity mapper that is used
     * by all requests, unless otherwise specified
     *
     * @param entityMapper Entity mapper
     */
    void setEntityMapper(@Nullable final EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    /**
     * Add a new request decorator. This will have the opportunity
     * to decorate every request made by this client
     *
     * @param decorator Decorator
     */
    void addDecorator(@NotNull final Consumer<HttpClient.WrappedRequestBuilder> decorator) {
        this.decorators.add(Objects.requireNonNull(decorator, "Decorator may not be null"));
    }

}

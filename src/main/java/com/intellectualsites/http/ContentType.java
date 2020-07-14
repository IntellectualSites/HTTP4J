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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ContentType {

    private static final Map<String, ContentType> internalMap = new HashMap<>();

    public static final ContentType JSON = of("application/json");
    public static final ContentType XML = of("application/xml");
    public static final ContentType DUMMY = of("application/*");

    private final String type;

    private ContentType(@NotNull final String type) {
        this.type = type;
    }

    /**
     * Get the MIME type instance corresponding to the IANA template name
     *
     * @param type MIME type
     * @return MIME type instance
     */
    @NotNull public static ContentType of(@NotNull final String type) {
        return internalMap.computeIfAbsent(Objects.requireNonNull(type,
            "Type may not be null").toLowerCase(), ContentType::new);
    }

    @Override public String toString() {
        return this.type;
    }

    @Override public int hashCode() {
        return this.type.hashCode();
    }

    @Override public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ContentType mimeType = (ContentType) o;
        return Objects.equals(type, mimeType.type);
    }

}

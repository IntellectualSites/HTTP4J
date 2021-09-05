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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.intellectualsites.http.external.GsonMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HttpClientTest {

    private static final int PORT = 1080;
    private static final Gson GSON = new GsonBuilder().create();
    private static final String BASE_PATH = String.format("http://localhost:%d", PORT);
    private static final String BASE_BODY = "Unicorns are real!";
    private static final String BASE_HEADER_KEY = "X-Test-Header";
    private static final String BASE_HEADER_VALUE = "yay";
    private static final String ECHO_HEADER_KEY = "X-Test-Echo";
    private static final String ECHO_HEADER_VALUE = "Wooo!";
    private static final String ECHO_CONTENT = UUID.randomUUID().toString();
    private static MockServerClient mockServer;

    private HttpClient client;

    @BeforeAll static void setupServer() {
        final JsonObject object = new JsonObject();
        object.addProperty("hello", "world");
        mockServer = ClientAndServer.startClientAndServer(PORT);
        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/")).respond(
            org.mockserver.model.HttpResponse.response().withStatusCode(200)
                .withHeader(BASE_HEADER_KEY, BASE_HEADER_VALUE).withBody(BASE_BODY));
        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/echo"))
            .respond(new EchoCallBack());
        mockServer.when(HttpRequest.request().withPath("/invalid"))
            .respond(org.mockserver.model.HttpResponse.notFoundResponse());
        mockServer.when(HttpRequest.request().withPath("/gson"))
            .respond(org.mockserver.model.HttpResponse.response(GSON.toJson(object)));
        mockServer.when(HttpRequest.request().withPath("/testgson"))
            .respond(new GsonCallBack());
    }


    public static final class EchoCallBack implements ExpectationResponseCallback {

        @Override public org.mockserver.model.HttpResponse handle(HttpRequest httpRequest) {
            return org.mockserver.model.HttpResponse.response(httpRequest.getBodyAsString())
                .withHeader(ECHO_HEADER_KEY, httpRequest.getFirstHeader(ECHO_HEADER_KEY));
        }

    }


    public static final class GsonCallBack implements ExpectationResponseCallback {

        @Override public org.mockserver.model.HttpResponse handle(HttpRequest httpRequest) {
            final JsonObject object = GSON.fromJson(httpRequest.getBodyAsString(), JsonObject.class);
            return org.mockserver.model.HttpResponse.response(Boolean.toString(object.has("gson") &&
                object.get("gson").getAsString().equals("true") &&
                httpRequest.getFirstHeader("content-type").equalsIgnoreCase("application/json; charset=UTF-8")));
        }
    }


    @AfterAll static void stopServer() {
        mockServer.stop();
    }

    @BeforeEach void setupClient() {
        final EntityMapper mapper = EntityMapper.newInstance()
            .registerSerializer(JsonObject.class, GsonMapper.serializer(JsonObject.class, GSON))
            .registerDeserializer(JsonObject.class, GsonMapper.deserializer(JsonObject.class, GSON));
        this.client = HttpClient.newBuilder()
            .withBaseURL(BASE_PATH)
            .withEntityMapper(mapper)
            .withDecorator(request -> request.withHeader(ECHO_HEADER_KEY, ECHO_HEADER_VALUE))
            .build();
    }

    @Test void testSimpleGet() {
        final HttpResponse response = this.client.get("/").execute();
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
        assertEquals(200, response.getStatusCode());
        assertEquals(BASE_BODY, response.getResponseEntity(String.class));
        assertEquals(BASE_HEADER_VALUE, response.getHeaders().getHeader(BASE_HEADER_KEY));
    }

    @Test void testEcho() {
        final HttpResponse echoResponse = this.client.post("/echo").withInput(() -> ECHO_CONTENT)
            .onStatus(200, response -> System.out
                .printf("Got input: %s\n", response.getResponseEntity(String.class)))
            .onRemaining(response -> {
                throw new RuntimeException("No!");
            }).execute();
        assertNotNull(echoResponse);
        assertEquals(ECHO_CONTENT, echoResponse.getResponseEntity(String.class));
        assertEquals(ECHO_HEADER_VALUE, echoResponse.getHeaders().getHeader(ECHO_HEADER_KEY));
    }

    @Test void testThrow() {
        assertThrows(TestException.class, () -> this.client.get("/invalid").onStatus(404, response -> {
            throw new TestException();
        }).execute());
    }

    @Test void testNotThrow() {
        this.client.get("/invalid").onStatus(404, response -> {
            throw new TestException();
        }).onException(ignored -> {}).execute();
    }

    @Test void testGson() {
        final JsonObject object = Objects.requireNonNull(this.client.get("/gson").execute()).getResponseEntity(JsonObject.class);
        assertEquals("world", object.get("hello").getAsString());
    }

    @Test void testGsonWriting() {
        final JsonObject object = new JsonObject();
        object.addProperty("gson", true);
        assertEquals("true", Objects.requireNonNull(this.client.get("/testgson")
            .withInput(() -> object).execute()).getResponseEntity(String.class));
    }


    public static class TestException extends RuntimeException {
    }

}

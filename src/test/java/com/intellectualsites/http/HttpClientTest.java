package com.intellectualsites.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HttpClientTest {

    private static final int PORT = 1080;
    private static final String BASE_PATH = String.format("http://localhost:%d", PORT);
    private static final String BASE_BODY = "Unicorns are real!";
    private static final String BASE_HEADER_KEY = "X-Test-Header";
    private static final String BASE_HEADER_VALUE = "yay";
    private static final String ECHO_CONTENT = UUID.randomUUID().toString();
    private static MockServerClient mockServer;

    private HttpClient client;

    @BeforeAll static void setupServer() throws Throwable {
        mockServer = ClientAndServer.startClientAndServer(PORT);
        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/")).respond(
            org.mockserver.model.HttpResponse.response().withStatusCode(200)
                .withHeader(BASE_HEADER_KEY, BASE_HEADER_VALUE).withBody(BASE_BODY));
        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/echo"))
            .respond(new EchoCallBack());
        mockServer.when(HttpRequest.request().withPath("/invalid"))
            .respond(org.mockserver.model.HttpResponse.notFoundResponse());
    }

    public static final class EchoCallBack implements ExpectationResponseCallback {

        @Override public org.mockserver.model.HttpResponse handle(HttpRequest httpRequest) {
            return org.mockserver.model.HttpResponse.response(httpRequest.getBodyAsString());
        }

    }


    @AfterAll static void stopServer() {
        mockServer.stop();
    }

    @BeforeEach void setupClient() {
        final EntityMapper mapper = EntityMapper.newInstance();
        this.client =
            HttpClient.newBuilder().withBaseURL(BASE_PATH).withEntityMapper(mapper).build();
    }

    @Test void testSimpleGet() {
        final HttpResponse response = this.client.get("/").execute();
        assertNotNull(response);
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


    public static class TestException extends RuntimeException {
    }

}

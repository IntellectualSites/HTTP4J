# HTT4J

## Description

This is a simple, lightweight and tiny wrapper for Java's HttpURLConnection. It has no external
dependencies and is written for Java 8.

It comes with a entity mapping system (serialization and deserialization for request and response bodies)
with optional mappings for third party libraries (currently supporting: GSON).

### Rationale

Most HTTP client for Java are either built for Java 11+, or have a large amount of dependencies,
which means that in order to use them, one needs to built a fatjar that often end up being huge.
This aims to offer a nicer way to interact with the Java 8 HTTP client, without having to double the
size of the output artifacts.

## Usage

### Repository

HTT4J is available from [IntellectualSites](https://intellectualsites.com)' maven repository:

```xml
<repository>
    <id>intellectualsites-snapshots</id>
    <url>https://mvn.intellectualsites.com/content/repositories/snapshots</url>
</repository>
```

```xml
<dependency>
    <groupId>com.intellectualsites.http</groupId>
    <artifactId>HTTP4J</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Code

**JavaDocs:** [https://plotsquared.com/docs/http4j/](https://plotsquared.com/docs/http4j/)

All requests are done using an instance of `com.intellectualsites.http.HttpClient`:

```java
HttpClient client = HttpClient.newBuilder()
    .withBaseURL("https://your.api.com")
    .build();
```

The client also take in a `com.intellectualsites.http.EntityMapper` instance. This
is used to map request & response bodies to Java objects. By default, it includes a mapper
for Java strings.

```java
EntityMapper entityMapper = EntityMapper.newInstance()
    .registerDeserializer(JsonObject.class, GsonMapper.deserializer(JsonObject.class, GSON));
```

The above snippet would create an entity mapper that maps to and from Java strings, and
from HTTP response's to GSON json objects.

This can then be included in the HTTP client by using `<builder>.withEntityMapper(mapper)` to
be used in all requests, or added to individual requests.

HTTP4J also supports request decorators, that can be used to modify each request. These are
added by using:

```java
builder.withDecorator(request -> {
    request.doSomething();
});
```

The built client can then be used to make HTTP requests, like such:

```java
client.post("/some/api").withInput(() -> "Hello World")
    .onStatus(200, response -> {
        System.out.println("Everything is fine");
        System.out.println("Response: " + response.getResponseEntity(String.class));
    })
    .onStatus(404, response -> System.err.println("Could not find the resource =("))
    .onRemaining(response -> System.err.printf("Got status code: %d\n", response.getStatusCode()))
    .onException(Throwable::printStackTrace)
    .execute();
```

#### Exception Handling

HTTP4J will forward all RuntimeExceptions by default, and wrap all other exceptions (that do not
extend RuntimeException) in a RuntimeException.

By using `onException(exception -> {})` you are able to modify the behaviour.

#### Examples

More examples can be found in [HttpClientTest.java](https://github.com/Sauilitired/HTTP4J/blob/master/src/test/java/com/intellectualsites/http/HttpClientTest.java)

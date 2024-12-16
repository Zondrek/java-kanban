package util;

import java.net.URI;
import java.net.http.HttpRequest;

public class HttpUtil {

    public static HttpRequest post(URI uri, String body) {
        return HttpRequest.newBuilder().uri(uri).POST(HttpRequest.BodyPublishers.ofString(body)).build();
    }

    public static HttpRequest get(URI uri) {
        return HttpRequest.newBuilder().uri(uri).GET().build();
    }

    public static HttpRequest delete(URI uri) {
        return HttpRequest.newBuilder().uri(uri).DELETE().build();
    }
}

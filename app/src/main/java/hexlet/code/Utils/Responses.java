package hexlet.code.Utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public final class Responses {
    public static HttpResponse<String> responseToGet(String urlName) {
        return Unirest
                .get(urlName)
                .asString();
    }

    public static HttpResponse<String> responseToPost(String urlName, String fieldName, String fieldValue) {
        return Unirest
                .post(urlName)
                .field(fieldName, fieldValue)
                .asString();
    }

    public static HttpResponse<String> responseToPost(String urlName) {
        return Unirest
                .post(urlName)
                .asString();
    }
}

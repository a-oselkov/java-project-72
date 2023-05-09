package hexlet.code.Utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public final class Responses {
    public static HttpResponse<String> responseToGet(String urlName) {
        HttpResponse<String> response = Unirest
                .get(urlName)
                .asString();
        return response;
    }

    public static HttpResponse<String> responseToPost(String urlName, String fieldName, String fieldValue) {
        HttpResponse<String> response = Unirest
                .post(urlName)
                .field(fieldName, fieldValue)
                .asString();
        return response;
    }

    public static HttpResponse<String> responseToPost(String urlName) {
        HttpResponse<String> response = Unirest
                .post(urlName)
                .asString();
        return response;
    }
}

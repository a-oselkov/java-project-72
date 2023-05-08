package hexlet.code.Utils;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public final class Response {
    public static HttpResponse<String> getResponse(String urlName) {
        HttpResponse<String> response = Unirest
                .get(urlName)
                .asString();
        return response;
    }

    public static HttpResponse<String> postResponse(String urlName, String fieldName, String fieldValue) {
        HttpResponse<String> response = Unirest
                .post(urlName)
                .field(fieldName, fieldValue)
                .asString();
        return response;
    }

    public static HttpResponse<String> postResponse(String urlName) {
        HttpResponse<String> string = Unirest
                .post(urlName)
                .asString();
        return string;
    }
}

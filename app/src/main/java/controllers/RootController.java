package controllers;

import io.javalin.http.Handler;

public final class RootController {
    public static Handler welcome = ctx -> {
        ctx.result("HelloWorld");
    };
}

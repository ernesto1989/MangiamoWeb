package com.conciencia.vertx.verticles.web.auth;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.FormLoginHandler;

/**
 *
 * @author Ernesto Cantu
 */
public class FormLoginHndlrProv implements Handler<RoutingContext> {

    private AuthProv authProvider;
    private String usernameParam;
    private String passwordParam;

    public FormLoginHndlrProv(AuthProv authProvider, String usernameParam, String passwordParam,
            String returnURLParam, String directLoggedInOKURL) {
        this.authProvider = authProvider;
        this.usernameParam = usernameParam;
        this.passwordParam = passwordParam;
    }

    public FormLoginHndlrProv setUsernameParam(String usernameParam) {
        this.usernameParam = usernameParam;
        return this;
    }

    public FormLoginHndlrProv setPasswordParam(String passwordParam) {
        this.passwordParam = passwordParam;
        return this;
    }

    /**
     * Método que me ayuda a redirigir dentro de esta clase.
     * @param response Http Response
     * @param url url a la cual se redirige
     */
    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader(HttpHeaders.LOCATION, url).setStatusCode(302).end();
    } 
    
    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        if (req.method() != HttpMethod.POST) {
            context.fail(405); // Must be a POST
        } else {
            if (!req.isExpectMultipart()) {
                throw new IllegalStateException("HttpServerRequest should have setExpectMultipart set to true, but it is currently set to false.");
            }
            MultiMap params = req.formAttributes();
            String username = params.get(usernameParam);
            String password = params.get(passwordParam);
            if (username == null || password == null) {
                System.out.println("No username or password provided in form - did you forget to include a BodyHandler?");
                doRedirect(context.response(),"/login");
            } else {
                Session session = context.session();
                JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
                authProvider.authenticate(authInfo, res -> {
                    if (res.succeeded()) {
                        User user = res.result();
                        context.setUser(user);
                        if (session != null) {
                            session.regenerateId();
                        }
                        doRedirect(req.response(), "/cg");
                    } else {
                        doRedirect(context.response(),"/login");
                    }
                });
            }
        }
    }
}

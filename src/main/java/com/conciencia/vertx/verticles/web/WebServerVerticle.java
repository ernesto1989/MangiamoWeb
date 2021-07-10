package com.conciencia.vertx.verticles.web;

import com.conciencia.vertx.verticles.web.auth.AuthProv;
import com.conciencia.vertx.verticles.web.auth.FormLoginHndlrProv;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

/**
 * Verticle que crea un servidor web
 * 
 * http://vertx.io/docs/guide-for-java-devs/#_access_control_and_authentication
 * Autenticación
 * 
 * https://github.com/vert-x3/vertx-web/blob/master/vertx-web/src/main/java/io/vertx/ext/web/handler/impl/FormLoginHandlerImpl.java
 * form login handler
 *
 * @author Ernesto Cantu
 */
public class WebServerVerticle extends AbstractVerticle {

    private static final String WEB_APP_CONTEXT = "/Mangiamo";
    private static final String REST_API_CONTEXT = "/api/";
    private static final String STATIC_CONTEXT = "/static/*";
    private static final String STATIC_WEB_SOCKET_CONTEXT = "/websocket-connection/*";

    // <editor-fold defaultstate="collapsed" desc="MÉTODOS PARA TEMPLATES">
    private void defineTemplateRendering(Router router, FreeMarkerTemplateEngine templateEngine, AuthProv auth) {
        
//        router.get("/login").handler(context->{
//            templateEngine.render(context.data(), "templates/login.ftl", hndlr -> {
//                if (hndlr.succeeded()) {
//                    context.response().putHeader("Content-Type", "text/html");
//                    context.response().end(hndlr.result());
//                } else {
//                    context.fail(hndlr.cause());
//                }
//            });
//        });
        
//        router.post("/login-auth").handler(new FormLoginHndlrProv(auth,"username","password","return_url","/cg")); 
//        
//        router.get("/logout").handler(context -> {
//            context.clearUser(); 
//            context.response()
//              .setStatusCode(302)
//              .putHeader("Location", "/login")
//              .end();
//        });
        
        router.get("/").handler(context ->{
            context.response()
              .setStatusCode(302)
              .putHeader("Location", "/Mangiamo")
              .end();
        });

        //atiende index solamente
        router.get(WEB_APP_CONTEXT).handler(context -> {
            
            //context.put("user",context.user().principal().getString("username"));
            templateEngine.render(context.data(), "templates/index.ftl", hndlr -> {
                if (hndlr.succeeded()) {
                    context.response().putHeader("Content-Type", "text/html");
                    context.response().end(hndlr.result());
                } else {
                    context.fail(hndlr.cause());
                }
            });
        });

        router.get(WEB_APP_CONTEXT + "/:page").handler(context -> {
            String page = context.request().getParam("page");
            
            if(page.equals(""))
                page = "index";

            //context.put("user", "Ernesto Cantú");
            templateEngine.render(context.data(), "templates/" + page + ".ftl", hndlr -> {
                if (hndlr.succeeded()) {
                    context.response().putHeader("Content-Type", "text/html");
                    context.response().end(hndlr.result());
                } else {
                    context.fail(hndlr.cause());
                }
            });
        });
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="DEFINICION DE MÉTODOS HTTP REST GENERICOS">
    private void defineGetAll(Router router) {
        router.get(REST_API_CONTEXT + ":type").handler(routingContext -> {
            String type = routingContext.request().getParam("type");
            vertx.eventBus().request("get_" + type, null, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonArray results = (JsonArray) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(results.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }
    
    private void defineSearch(Router router) {
        router.post(REST_API_CONTEXT + "search").consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("search_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonArray results = (JsonArray) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(results.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }

    private void definePost(Router router) {
        router.post(REST_API_CONTEXT).consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("add_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonObject added = (JsonObject) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(added.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }

    private void definePut(Router router) {
        router.put(REST_API_CONTEXT).consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("edit_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonObject edited = (JsonObject) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(edited.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }

    private void defineDelete(Router router) {
        router.delete(REST_API_CONTEXT).consumes("application/json").produces("application/json").handler(routingContext -> {
            JsonObject object = routingContext.getBodyAsJson();
            String type = object.getString("type");
            vertx.eventBus().request("delete_" + type, object, hndlr -> {
                if (hndlr.succeeded()) {
                    JsonObject deleted = (JsonObject) hndlr.result().body();
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(200).end(deleted.encodePrettily());
                } else {
                    HttpServerResponse response = routingContext.response();
                    response.putHeader("content-type", "application/json; charset=utf-8");
                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
                }
            });
        });
    }

    /**
     * Método que permite a cualquier cliente suscribirse al bus de eventos por
     * medio de SockJS. Al recibirse una petición en esta dirección, se crea un
     * socket para comunicarse con el bus de eventos
     *
     * @param router
     */
    private void defineWebSocketSuscription(Router router) {
        BridgeOptions options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("out"))
                .addInboundPermitted(new PermittedOptions().setAddressRegex("in"));
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        router.route(STATIC_WEB_SOCKET_CONTEXT).handler(sockJSHandler.bridge(options));
    }

    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="DEFINICION DE MÉTODOS HTTP REST ESPECIFICOS">
//    private void defineOtherMethods(Router router) {
//
//        //método que permite crear una nueva quincena.
//        router.post(REST_API_CONTEXT + "nueva_quincena").produces("application/json").handler(routingContext -> {
//            JsonObject object = routingContext.getBodyAsJson();
//            vertx.eventBus().request("nueva_quincena", object, hndlr -> {
//                if (hndlr.succeeded()) {
//                    HttpServerResponse response = routingContext.response();
//                    response.putHeader("content-type", "application/json; charset=utf-8");
//                    response.setStatusCode(200).end(((JsonObject) hndlr.result().body()).encodePrettily());
//                } else {
//                    HttpServerResponse response = routingContext.response();
//                    response.putHeader("content-type", "application/json; charset=utf-8");
//                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
//                }
//            });
//        });
//
//        router.post(REST_API_CONTEXT + "elimina_cchica").produces("application/json").handler(routingContext -> {
//            vertx.eventBus().request("elimina_cchica", null, hndlr -> {
//                if (hndlr.succeeded()) {
//                    HttpServerResponse response = routingContext.response();
//                    response.putHeader("content-type", "application/json; charset=utf-8");
//                    response.setStatusCode(200).end(((JsonObject) hndlr.result().body()).encodePrettily());
//                } else {
//                    HttpServerResponse response = routingContext.response();
//                    response.putHeader("content-type", "application/json; charset=utf-8");
//                    response.setStatusCode(500).end(new JsonObject().put("error", hndlr.cause().toString()).encodePrettily());
//                }
//            });
//        });
//    }

    // </editor-fold>
    
    /**
     * Método ejecutado al arranque del verticle.
     *
     * @param promise
     * @param startFuture
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        HttpServer server = vertx.createHttpServer();
        //AuthProv auth = new AuthProv();
        FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create(vertx);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create()); // permite recibir json en el servidor
//        router.route().handler(CookieHandler.create());
//        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
       // router.route().handler(UserSessionHandler.create(auth));
        
        //Redirección a páginas estáticas web
        router.route(STATIC_CONTEXT).
                handler(StaticHandler.create());
        
//        AuthHandler authHandler = RedirectAuthHandler.create(auth, "/login"); 
        router.route(WEB_APP_CONTEXT + "/*");//.handler(authHandler);
        router.route(REST_API_CONTEXT + "*");//.handler(authHandler);

        //App Web
        defineTemplateRendering(router, templateEngine, null); //auth);

        //REST Api
        defineGetAll(router);
        defineSearch(router);
        definePost(router);
        definePut(router);
        defineDelete(router);
        defineWebSocketSuscription(router);
//        defineOtherMethods(router);

        //definición de métodos http específicos
        server.requestHandler(router).listen(
            //Integer.getInteger("http.port"), System.getProperty("http.address", "0.0.0.0"), hndlr -> {
            8090, hndlr -> {
                if (hndlr.succeeded()) {
                    System.out.println("Server up n running");
                    promise.complete();
                } else {
                    promise.fail(hndlr.cause());
                }
            }//
        );
    }

    /**
     * Método llamado cuando se repliega el verticle
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        System.out.println("WebServer Verticle undeploy");
    }
}

package com.conciencia.vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * Clase que crea la instancia de vertx y configura todo lo necesario.
 * 
 * 1. Se crea la instancia de Vertx
 * 2. Se registran los verticles de servicio de la aplicación.
 * 
 * Los verticles son clases que funcionan a manera de microservicios. Vertx, mediante
 * un bus de mensajes, solicita a un verticle la ejecución de una funcionalidad
 * particular. 
 * 
 * Un ejemplo sería un servidor web. Como tal, se crea un vertcile que escucha en
 * un puerto web. Cuando recibe una petición, mediante un mensaje, solicita datos
 * a otro verticle que se encuentra tambien en el objeto vertx actual.
 * 
 * @author Ernesto Cantu
 */
public class VertxConfig {
    
    /* Instancia de vertx que controla la aplicación */
    public static Vertx vertx;
    
    /* Objeto de conexión con cliente*/ 
    public static JDBCClient client;
    
    /* Variables para conexión de BD*/
    private static String host;
    private static String db;
    private static String user;
    private static String password;
    private static String driverClass;

    /* Objeto de configuración para bd*/
    private static JsonObject config;
    
    /**
     * Configuración de Vertx.
     * 
     * 1.- Se crea la instancia
     * 2.- Se despliegan los Verticles
     * 3.- Se inicializa el objeto de Base de Datos
     */
    public static void config(){
        vertx = Vertx.vertx();
        Future<Void> steps = deployVerticles();//initDBClient().compose(v-> );   
        steps.setHandler(Promise.promise());
        steps.setHandler(hndlr->{
            if(hndlr.failed()){
                System.out.println(hndlr.cause());
            }else{
                System.out.println("Mangiamo has started.");
            }
            
        });
    }
    
    /**
     * Método de apoyo para configurar cliente de datos.
     */
    private static Future<Void> initDBClient(){
        Promise<Void> promise = Promise.promise();
        
        host = "localhost:3306";
        db = "controlgastos";
        user = "root";
        password = "4747819";
        driverClass = "com.mysql.cj.jdbc.Driver";//System.getenv("driverClass");
        
        config = new JsonObject()
            .put("url", "jdbc:mysql://" + host+"/" + db + "?useSSL=false&useTimezone=true&serverTimezone=America/Mexico_City&user=" + user + "&password=" + password)
            .put("driver_class", driverClass)
            .put("max_pool_size", 30);
        
        client = JDBCClient.createNonShared(vertx, config);
        
        client.getConnection(connectionHndlr->{
            if(connectionHndlr.failed()){
                promise.fail(connectionHndlr.cause().toString());
            }else{
                connectionHndlr.result().close();
                promise.complete();
            }
        });
        
        return promise.future();
    }
    
     /**
     * Método que despliega todos los verticles necesitados por la aplicación.
     * 

     */
    private static Future<Void> deployVerticles() {
        Promise<Void> promise = Promise.promise();
        
        vertx.deployVerticle("com.conciencia.vertx.verticles.web.WebServerVerticle",hndlr->{
            if(hndlr.succeeded()){
                promise.complete();
                vertx.deployVerticle("com.conciencia.vertx.verticles.websocket.WebSocketMessageAPI");
                vertx.deployVerticle("com.conciencia.vertx.verticles.websocket.WebSocketTestMsg");
                vertx.deployVerticle("com.conciencia.vertx.verticles.websocket.WebSocketOutTestMsg");
            }else{
                promise.fail(hndlr.cause());
            }
        });
        
        return promise.future();
    }
}

package com.conciencia.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Clase que crea la instancia de vertx. Crea una instancia de los siguiente servicios:
 * 
 * 1. MenuRepository. Servicio que lee el menú completo de la base de datos y lo
 *    mantiene en memoria. El menú no podrá ser actualizado durante el periodo 
 *    de operación del restaurante.
 * 
 * 2. CustomersDatabaseRepository. Servicio que funciona como repositorio de datos
 *    de clientes. Permite registrar nuevos clientes y buscarlos por #telefono.
 * 
 * 
 * @author Ernesto Cantu
 */
public class VertxConfig {
    
    /* Instancia de vertx que controla la aplicación */
    public static Vertx vertx;
    
    /**
     * Configuración de Vertx.
     * 
     * 1.- Se crea la instancia
     * 2.- Se despliegan los Verticles
     */
    public static void config(){
        vertx = Vertx.vertx();
        Future<Void> steps = deployMenu().compose(v-> deployClientesRepository());   
        steps.setHandler(hndlr->{
            if(hndlr.failed()){
                System.out.println(hndlr.cause());
            }else{
                System.out.println("Mangiamo has started.");
                vertx.eventBus().request("get_menu", null, menuHndlr->{
                    System.out.println((JsonArray) menuHndlr.result().body());
                });
                
                vertx.eventBus().request("get_customer","8112124616",custHndlr->{
                    System.out.println((JsonObject) custHndlr.result().body());
                });
            }
            
        });
    }
    
    /**
     * Método que arranca el Repositorio del menú.
     */
    private static Future<Void> deployMenu() {
        Promise<Void> promise = Promise.promise();
        
        vertx.deployVerticle("com.conciencia.repositories.MenuRepository", hndlr->{
            if(hndlr.succeeded())
                promise.complete();
            else
                promise.fail(hndlr.cause());
        });
        return promise.future();
    }
    
    /**
     * Método que arranca el repositorio de clientes
     */
    private static Future<Void> deployClientesRepository() {
        Promise<Void> promise = Promise.promise();
        vertx.deployVerticle("com.conciencia.repositories.CustomersDatabaseVerticle",hndlr->{
            if(hndlr.succeeded())
                promise.complete();
            else
                promise.fail(hndlr.cause());
        });
        return promise.future();
    }
}

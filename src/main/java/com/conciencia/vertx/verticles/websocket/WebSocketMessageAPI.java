package com.conciencia.vertx.verticles.websocket;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

/**
 * Api de comunicación que recibe mensajes desde cualquier punto del sistema hacia
 * los clientes suscritos.
 * 
 * Formato de mensajes de entrada a eb en Java:
 * 
 * msg = {
 *   'address':'dirección_eb',
 *   'action':'',
 *   'entity':{
 *      objeto enviado
 *   } 
 * }
 * 
 * 
 * Formato de mensajes de salida a clientes JS:
 * 
 * msg = {
 *   'action':'',
 *   'entity':''
 * }
 * 
 * 
 * @author Ernesto Cantu
 * 21/07/2019
 * 
 * Update: 02/102019
 */
public class WebSocketMessageAPI extends AbstractVerticle {
    
    //URL para acceder al bus de eventos desde un cliente JS.
    public final static String SOCKET_EB_OUT_ADDRESS = "socket_msg_out";
    
    @Override
    public void start(Promise<Void> promise) throws Exception {
        //bus de eventos que recibe mensajes desde adentro de la aplicación y lo
        //envía a cualquier cliente suscrito via SockJS.
        vertx.eventBus().consumer(SOCKET_EB_OUT_ADDRESS, hndlr->{
            //JsonObject msg = (JsonObject)hndlr.body();
            vertx.eventBus().publish("out", new JsonObject().put("action", "test_msg"));
        });
        
        //NO UTILIZADO EN ControlGastos.
        //El punto de acceso in sirve como una central de recepción de mensajes
        //desde clientes Javascript. Al recibirse un 
        vertx.eventBus().consumer("in", hndlr->{
            JsonObject msg = new JsonObject((String)hndlr.body());
            if(!msg.containsKey("addess")){
                hndlr.fail(0, "Se requiere una dirección destino para el mensaje");
            }
            String address = msg.getString("address");
            vertx.eventBus().request(address,msg,socketHndlr->{
                if(socketHndlr.failed()){
                    hndlr.fail(0, socketHndlr.cause().getMessage());
                }
                hndlr.reply(socketHndlr.result().body());
            });
        });
        
        promise.complete();
    }
}

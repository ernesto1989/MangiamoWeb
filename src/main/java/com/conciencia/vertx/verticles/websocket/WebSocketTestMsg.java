package com.conciencia.vertx.verticles.websocket;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Verticle de ejemplo para procesamiento de mensajes provenientes de SockJS.
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
 * @author Ernesto Cantu Valle
 * 02/10/2019
 */
public class WebSocketTestMsg extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("eb_incomming_msg", hndlr->{
            JsonObject msg = (JsonObject) hndlr.body();
            System.out.println("action:" + msg.getString("action"));
            System.out.println("msg:" + msg.getJsonObject("entity"));
        });
    }
    
    
}

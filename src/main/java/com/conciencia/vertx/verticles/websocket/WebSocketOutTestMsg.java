package com.conciencia.vertx.verticles.websocket;

import static com.conciencia.vertx.verticles.websocket.WebSocketMessageAPI.SOCKET_EB_OUT_ADDRESS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Verticle de ejemplo para envio de mensajes a SockJS.
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
public class WebSocketOutTestMsg extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.setPeriodic(10000, hndlr->{
            System.out.println("Enviando mensaje a ventana web");
            vertx.eventBus().publish(SOCKET_EB_OUT_ADDRESS,null);
        });
    }
    
    
}

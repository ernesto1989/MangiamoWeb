package com.conciencia.main;


import com.conciencia.vertx.VertxConfig;


/**
 * Proyecto de MangiamoWeb
 *
 * V 0.9 - 10/07/2021
 */
public class MainApp {
    
    

    /**
     * Método main. Punto de entrada a la aplicación.
     * 
     * ¿Cómo funciona Mangiamo?
     * 
     * Al estar implementato sobre Vertx, se parte de configurar un objeto Vertx,
     * indicandole lo siguiente:
     * 
     * 1. 
     *
     * @param args argumentos de entrada pasados por consola
     */
    public static void main(String[] args) {
        VertxConfig.config();        
    }
}

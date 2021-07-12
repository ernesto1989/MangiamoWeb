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
     * 1. Se configura el objeto de conexión a la base de datos.
     * 2. Se crean instancias de todos los servicios disponibles en la aplicación.
     * 
     * Existen 2 Servicios "Genéricos":
     * 
     * 1. Web Server Verticle. Este servicio crea una API Rest genérica recibe todas
     * las peticiones desde los clientes web. Este servicio redirecciona al repositorio
     * correspondiente o responde con error de método no implementado.
     * 
     * 2. DatabaseRepository. Este servicio define de forma genérica el manejo de
     * conexiones a la base de datos. Las implementaciones de esta clase permiten
     * definir el acceso particular a cada tabla de la base de datos.
     *
     * @param args argumentos de entrada pasados por consola
     */
    public static void main(String[] args) {
        VertxConfig.config();        
    }
}

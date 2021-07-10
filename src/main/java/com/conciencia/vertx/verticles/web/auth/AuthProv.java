package com.conciencia.vertx.verticles.web.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que provee a la aplicación de autenticación sencilla basada solamente
 * en memoria. 
 * 
 * Para más detalle: https://github.com/vert-x3/vertx-auth/blob/master/vertx-auth-jdbc/src/main/java/io/vertx/ext/auth/jdbc/JDBCAuth.java
 * 
 * El método de autenticación es provisional y debe ser sustituido.
 * @author Ernesto Cantu
 * 20/09-2019
 * 
 * Actualización: 03/10/2019
 * Se incluye hash map para simular conexion a bd en auth
 */
public class AuthProv implements AuthProvider {
    
    Map<String,String> users;

    public AuthProv() {
        users = new HashMap<>();
        users.put("root", "root");
        users.put("ecv","4747819");
        users.put("dv","13julio");
    }
    
    /**
     * Método que determina si un usuario/password es válido o no.
     * 
     * @param authInfo usuario
     * @param hndlr objeto que espera respuesta de si el usuario/password es
     * correcto
     */
    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> hndlr) {
        String username = authInfo.getString("username");
        if (username == null) {
            hndlr.handle(Future.failedFuture("authInfo must contain username in 'username' field"));
            return;
        }
        String password = authInfo.getString("password");
        if (password == null) {
            hndlr.handle(Future.failedFuture("authInfo must contain password in 'password' field"));
            return;
        }
        
        if(users.containsKey(username) && password.equals(users.get(username))){
            hndlr.handle(Future.succeededFuture(new AuthUser(username, this)));
        }
        else{
            hndlr.handle(Future.failedFuture("Invalid username/password"));
        }
    }

}

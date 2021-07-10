package com.conciencia.vertx.verticles.web.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

/**
 * Clase que representa al usuario loggeado en la aplicación
 * 
 * Para detalles de Roles y Permisos: 
 * https://github.com/vert-x3/vertx-auth/blob/master/vertx-auth-jdbc/src/main/java/io/vertx/ext/auth/jdbc/impl/JDBCUser.java
 * 
 * @author Ernesto Cantu
 */
public class AuthUser extends AbstractUser {

    private JsonObject principal;
    private String username;
    private AuthProv authProvider;
    
    public AuthUser(String username, AuthProv authProvider){
        this.username = username;
        this.authProvider = authProvider;
    }
    
    @Override
    protected void doIsPermitted(String string, Handler<AsyncResult<Boolean>> hndlr) {
        hndlr.handle(Future.succeededFuture(true));
    }

    @Override
    public JsonObject principal() {
        if (principal == null) {
            principal = new JsonObject().put("username", username);
        }
        return principal;
    }

    @Override
    public void setAuthProvider(AuthProvider ap) {
        if (authProvider instanceof AuthProv) {
            this.authProvider = (AuthProv)authProvider;
        } else {
            throw new IllegalArgumentException("Not a AuthProv");
        }
    }
    
}

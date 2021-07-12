package com.conciencia.vertx.verticles.db;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


/**
 * Vérticle que contiene todas las operaciones permitidas con el repositorio 
 * de Clientes.
 * 
 * @author Ernesto Cantu
 * 11/07/2021
 */
public class ClientesDatabaseVerticle extends DatabaseRepositoryVerticle {
    
    @Override
    public void initInfo(){
        this.entityName = "clientes";
        this.getAllQuery = "SELECT id, nombre, telefono, calle, numero, colonia, ecalle1, ecalle2 FROM clientes";
        this.searchQuery = "SELECT id, nombre, telefono, calle, numero, colonia, ecalle1, ecalle2 FROM clientes ";
        this.addQuery = "INSERT into clientes(nombre, telefono, calle, numero, colonia, ecalle1, ecalle2) values (?,?,?,?,?,?,?)";
        this.updateQuery = "UPDATE clientes set nombre = ?, telefono = ?, calle = ?, numero = ?, colonia = ?, ecalle1 = ?, ecalle2 = ? where id = ?";        
    }
    
    @Override
    public Boolean methodAllowed(String method) {
        if(method.equals(getAllMethod) || method.equals(searchMethod)
            || method.equals(addMethod)
        ) return true;
        return false;
    }
    
    @Override
    public JsonArray initParams(JsonObject entity,String transaction){
        JsonArray params = new JsonArray();
//        
//        if(transaction.equals("add")){
//            params.add(entity.getDouble("total"))
//                .add(entity.getString("fecha"));
//        }
//        
        return params;
    } 
    
    
    @Override
    public JsonArray configSearch(JsonObject entity, StringBuilder query) {
        String sep = "";
        JsonArray params = new JsonArray();
        
        if(entity.size() >= 1){
            query.append(" where ");
            
            if(entity.containsKey("nombre")){
                query.append(" nombre = ?");
                params.add(entity.getString("nombre"));
                sep = " or ";
            }
            
            if(entity.containsKey("telefono")){
                query.append(" telefono = ?");
                params.add(entity.getString("telefono"));
                sep = " or ";
            }
        }
        return params;
    }
    
    @Override
    public void informTransaction(String transaction) {
//        if(transaction.equals(addMethod)){
//            JsonObject bitacoraObject = new JsonObject().
//                    put("descripcion", "Ahorro realizado: " + entity.getDouble("total")).
//                    put("fecha",entity.getString("fecha"));
//            vertx.eventBus().publish("add_bitacora", bitacoraObject);
//        }
    }

    /**
     * Método que se ejecuta cuando el verticle se detiene
     * @throws Exception 
     */
    @Override
    public void stop() throws Exception {
        System.out.println("ClientesDatabaseVerticle undeploy");
    }
}

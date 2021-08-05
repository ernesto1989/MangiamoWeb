package com.conciencia.repositories;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Vérticle que contiene todas las operaciones permitidas con el repositorio 
 * de clientes.
 * 
 * @author Ernesto Cantu
 */
public class CustomersDatabaseVerticle extends AbstractVerticle{
    
    private final String DB_URL = "jdbc:sqlite:db/MangiamoDB.db";
    private final String DRIVER = "org.sqlite.JDBC";
    private final String SEARCH_BY_PHONE = "Select id,nombre,telefono,calle,numero,colonia,ecalle1,ecalle2 from Clientes where telefono = ?";
    private final String SAVE_CUSTOMER = "Insert into Clientes (nombre,telefono,calle,numero,colonia,ecalle1,ecalle2) values(?,?,?,?,?,?,?)";
    
    /**
     * Método que regresa una conexión de BD
     * @return conexión a la bd de Mangiamo.
     */
    private Connection getConnection(){
        Connection conn = null;
        try{
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally{
            return conn;
        }
    }
    
    /**
     * Método que busca a un cliente por su teléfono
     * @param phone telefono del cliente
     * @return cliente encontrado
     */
    private JsonObject getCustomer(String phone){
        JsonObject customer = null;
        try{
            if(phone != null){
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SEARCH_BY_PHONE);
                stmt.setString(1,phone);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()){
                    //Select id,nombre,telefono,calle,numero,colonia,ecalle1,ecalle2 from Clientes
                    customer = new JsonObject();
                    customer.put("id", rs.getInt("id"));
                    customer.put("nombre", rs.getString("nombre"));
                    customer.put("telefono", rs.getString("telefono"));
                    customer.put("calle", rs.getString("calle"));
                    customer.put("numero", rs.getString("numero"));
                    customer.put("colonia", rs.getString("colonia"));
                    customer.put("ecalle1", rs.getString("ecalle1"));
                    customer.put("ecalle2", rs.getString("ecalle2"));
                }
                rs.close();
                stmt.close();
                conn.close();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return customer;
    }
    
    /**
     * Método eu permite insertar en la bd un nuevo cliente.
     * @param c cliente a insertar.
     * @return id si el cliente fue generado,-1 si el cliente existía, 0 si fue
     * error interno de insersión
     */
    private int insertCustomer(JsonObject c){
        if(c == null)
            return -3;
        JsonObject clienteExistente =  getCustomer(c.getString("telefono"));
        Long id = null;
        if(clienteExistente != null)
            return -1;
        else{
            try{
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SAVE_CUSTOMER);
                stmt.setString(1, c.getString("nombre"));
                stmt.setString(2, c.getString("telefono"));
                stmt.setString(3, c.getString("calle"));
                stmt.setString(4, c.getString("numero"));
                stmt.setString(5, c.getString("colonia"));
                stmt.setString(6, c.getString("ecalle1"));
                stmt.setString(7, c.getString("ecalle2"));
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if(rs.next()){
                    id = rs.getLong(1);
                    c.put("id", id);
                }
                rs.close();
                stmt.close();
                conn.close();
                return id.intValue();
                
            }catch(Exception e){
                return -2;
            }
        }
    }

    /**
     * Método sobreescrito del verticle.
     * 
     * 1.- Se crea el objeto de conexión a bd
     * 2.- Sse crea el consumidor de mensajes para buscar clientes
     * 3.- Sse crea el consumidor de mensajes para crear clientes
     * 
     * @param startFuture
     * @throws Exception 
     */
    @Override
    public void start(Promise<Void> promise) throws Exception {
        vertx.eventBus().consumer("get_customer", msg->{
            String phone = (String) msg.body();
            vertx.executeBlocking(executeHndlr->{
                JsonObject c = getCustomer(phone);
                executeHndlr.complete(c);
            }, finishHndlr->{
                if(finishHndlr.succeeded()){
                    JsonObject customer = (JsonObject)finishHndlr.result();
                    msg.reply(customer);
                }else{
                    msg.fail(0, "No se encontró el cliente solicitado");
                }
            });
        });
        
        vertx.eventBus().consumer("save_customer", msg->{
            JsonObject customer = (JsonObject)msg.body();
            vertx.executeBlocking(executeHndlr->{
                int result = insertCustomer(customer);
                executeHndlr.complete(result);
            }, finishHndlr->{
                Integer result = (Integer) finishHndlr.result();
                if(result == -1)
                    msg.fail(0, "Teléfono existente");
                if(result == -2)
                    msg.fail(0, "Error en BD");
                if(result == -3)
                    msg.fail(0, "No se recibió objeto");
                msg.reply(result);
            });
        });
        promise.complete();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Customers Verticle undeploy");
    }
    
    
}

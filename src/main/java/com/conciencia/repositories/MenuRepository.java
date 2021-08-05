package com.conciencia.repositories;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase de apoyo que crea el menu al arranque de Mangiamo.
 *
 * @author Ernesto Cantu
 * 12 marzo 2019
 */
public class MenuRepository extends AbstractVerticle{

    private final JsonArray menu = new JsonArray();
    private final String DB_URL = "jdbc:sqlite:db/MangiamoDB.db";
    private final String DRIVER = "org.sqlite.JDBC";
    private final String SEARCH_ALL = "Select id,seccion,nombre,precio_unitario,es_orden,cantidad_orden from menu where seccion = ?";
    private final String GET_SECTIONS = "Select distinct seccion from menu order by seccion asc";
    private final String GET_RELATED_ITEMS = "Select id,seccion,nombre,0.0 as precio_unitario,es_orden,cantidad_orden from menu "
            + "where id in(Select item_relacionado from items_relacionados where item_ordenado = ?)";
    
    
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
    
    private JsonArray getSections(){
        JsonArray sections = new JsonArray();
        JsonObject section = null;        
        try{
            Connection conn = getConnection();
            if(conn != null){
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(GET_SECTIONS);
                while(rs.next()){
                    section = new JsonObject();
                    String s = rs.getString("seccion");
                    section.put("nombre", s);
                    sections.add(section);
                }
                rs.close();
                stmt.close();
                conn.close();
            }else{
                return null;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return sections;
    }
    
    private JsonArray getItems(String section){
        JsonArray items = new JsonArray();
        JsonObject item;
        try{
            Connection conn = getConnection();
            if(conn != null){
                PreparedStatement stmt = conn.prepareStatement(SEARCH_ALL);
                stmt.setString(1,section);
                ResultSet rs = stmt.executeQuery();
                while(rs.next()){
                    item = new JsonObject();
                    item.put("seccion",rs.getString("seccion"));
                    item.put("id",rs.getInt("id"));
                    item.put("nombre", rs.getString("nombre"));
                    item.put("precioUnitario", rs.getBigDecimal("precio_unitario").doubleValue());
                    item.put("esOrden", rs.getBoolean("es_orden"));
                    item.put("cantidadOrden", rs.getInt("cantidad_orden"));
                    items.add(item);
                }
                rs.close();
                stmt.close();
                conn.close();
            }else{
                return null;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return items;
    }

     private JsonArray getItemsRelacionados(Integer id){
        JsonArray itemsRelacionados = new JsonArray();
        JsonObject item;
        try{
            Connection conn = getConnection();
            if(conn != null){
                PreparedStatement stmt = conn.prepareStatement(GET_RELATED_ITEMS);
                stmt.setInt(1,id);
                ResultSet rs = stmt.executeQuery();
                while(rs.next()){
                    item = new JsonObject();
                    String s = rs.getString("seccion");
                    item.put("nombre", rs.getString("nombre"));
                    item.put("precioUnitario", rs.getBigDecimal("precio_unitario").doubleValue());
                    item.put("esOrden", rs.getBoolean("es_orden"));
                    item.put("cantidadOrden", rs.getInt("cantidad_orden"));
                    itemsRelacionados.add(item);
                }
                rs.close();
                stmt.close();
                conn.close();
            }else
                return null;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return itemsRelacionados;
    }
    
    /**
     * Método que inicializa el Menu desde la base de datos. 
     * 1.- Lee las distintas secciones de la base de datos
     * 2.- Lee los distintos items de la base de datos
     * 3.- Asigna los elementos de menu a cada sección y lee sus elementos 
     *     relacionados.
     * 
     */
    public void initMenu() throws Exception{
        JsonArray secciones = getSections();
        JsonArray  items, relacionados;
        JsonObject seccion = null;
        JsonObject item = null;
        
        if(secciones == null)
            throw new Exception("error en inicialización de menu");
        
        for(Object s: secciones){
            seccion = (JsonObject) s;
            menu.add(seccion);
            items = getItems(seccion.getString("nombre"));
            for(Object i:items){
                item = (JsonObject) i;
                relacionados = getItemsRelacionados(item.getInteger("id"));
                item.put("relacionados", relacionados);
            }
            seccion.put("items", items);
        }
    }

    @Override
    public void start(Promise<Void> promise) throws Exception {        
        vertx.executeBlocking(executeHndlr->{
            try{
                initMenu();
                vertx.eventBus().consumer("get_menu", hndlr->{
                    hndlr.reply(menu);
                });
                executeHndlr.complete();
            }catch(Exception e){
                executeHndlr.fail(e.getCause());
            }
        }, finishHndlr->{
            if(finishHndlr.succeeded()){
                promise.complete();
            }else{
                promise.fail("Error en lectura de menu");
            }
            
        });
    }
}

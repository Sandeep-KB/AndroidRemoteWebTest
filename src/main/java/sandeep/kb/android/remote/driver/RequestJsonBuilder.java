/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.driver;

import org.json.JSONObject;

/**
 *Used to build JSON as required by remote websocket.
 * 
 * @author Sandeep
 */
public class RequestJsonBuilder {

    public RequestJsonBuilder(int id, String method, String expression) {
        this.id = id;
        this.method = method;
        this.expression = expression;
    }

    private int id;
    private String method;
    private String expression;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getJsonString(){
        
        JSONObject parent = new JSONObject();
        JSONObject child = new JSONObject();
        
        parent.put("id",this.id);
        parent.put("method", this.method);
        
        child.put("expression", this.expression);
        
        parent.put("params",child);
        
        return parent.toString();
    
    }
}


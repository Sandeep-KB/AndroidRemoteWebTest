/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.websocket;

/**
 * A class to hold response of the websocket
 * @author Sandeep
 */
public class WebSocketResponse {
    
    private String data;
    private boolean error;
    private WebSocketStatus status;

    public WebSocketStatus getStatus() {
        return status;
    }

    public void setStatus(WebSocketStatus status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

   
    
}

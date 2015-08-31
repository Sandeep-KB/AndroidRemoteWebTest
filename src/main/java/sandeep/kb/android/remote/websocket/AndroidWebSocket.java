/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.websocket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sandeep.kb.android.remote.utils.Utils;

;

/**
 *Websocket client.
 * 
 * @author Sandeep
 */
@WebSocket
public class AndroidWebSocket extends WebSocketClient {

    private final CountDownLatch closeLatch;
    private WebSocketResponse result;
    private String json;
     

    public AndroidWebSocket(String json) {
        result = new WebSocketResponse();
        this.closeLatch = new CountDownLatch(1);
        this.json=json;
    }

    @SuppressWarnings("unused")
    private Session session;

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        //System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
        result.setStatus(WebSocketStatus.CLOSED);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        //System.out.printf("Got connect: %s%n", session);
        this.session = session;
        try {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture(json);
            fut.get(2, TimeUnit.SECONDS);
            Utils.sleep(2000);
            session.close(StatusCode.NORMAL, "I'm done");
        } catch (Throwable t) {
            t.printStackTrace();
        }
        result.setStatus(WebSocketStatus.OPEN);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        //System.out.printf("Got msg: %s%n", msg);
        result.setStatus(WebSocketStatus.RESULT);
        result.setData(msg);
    }

    WebSocketResponse getResult() {
       return this.result;
    }
}

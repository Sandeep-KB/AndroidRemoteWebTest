/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.websocket;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import sandeep.kb.android.remote.constants.AndroidWebDriverConstants;

/**
 *
 * @author Sandeep
 */
public class AndroidWebSocketClient {

    AndroidWebSocket socket;
    WebSocketClient client;
    URI uri;

    public AndroidWebSocketClient(URI uri,String json) {
        this.uri = uri;
        socket = new AndroidWebSocket(json);
        client = new WebSocketClient();
    }

    public void send() {

        try {
            client.start();
            URI echoUri = uri;
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, echoUri, request);
            //System.out.printf("Connecting to : %s%n", echoUri);
            socket.awaitClose((int)AndroidWebDriverConstants.SLEEP_LONG, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public WebSocketResponse getResult() {
        return socket.getResult();
    }

}

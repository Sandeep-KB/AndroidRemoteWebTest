/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.driver;

import java.net.URI;
import org.json.JSONObject;
import sandeep.kb.android.remote.constants.AndroidWebDriverConstants;
import sandeep.kb.android.remote.utils.Utils;
import sandeep.kb.android.remote.websocket.AndroidWebSocketClient;
import sandeep.kb.android.remote.websocket.AndroidWebSocketException;
import sandeep.kb.android.remote.websocket.WebSocketResponse;
import sandeep.kb.android.remote.websocket.WebSocketStatus;

/**
 * Its the class which is used to execute the script. The script is obtained by
 * reading required selenium atoms placed in resource folder at the runtime. The
 * required is constructed and passed to websocket. The result is parsed and
 * sent back
 *
 * @author Sandeep
 */
public class JSEvaluate {

    public String evaluate(String js, String wsUrl) throws AndroidWebSocketException {
        String json = getJsonString(js);
        AndroidWebSocketClient client = new AndroidWebSocketClient(URI.create(wsUrl), json);
        client.send();
        WebSocketResponse res = client.getResult();
        Utils.log("JSEvaluate", "Status = " + res.getStatus());
        if (WebSocketStatus.ERROR == res.getStatus()) {
            throw new AndroidWebSocketException(res.getData());
        }

        return getResultFromJson(res.getData());
    }

    private String getResultFromJson(String string) {
        try {
            JSONObject json = new JSONObject(string);
            //Utils.log("JSON", json.toString());
            JSONObject result1 = json.getJSONObject("result");
            //Utils.log("JSON", result1.toString());
            JSONObject result2 = result1.getJSONObject("result");
            //Utils.log("JSON", result2.toString());
            String value = (String) result2.get("value");
            //Utils.log("JSON", value.toString());
            return value;
        } catch (Exception ex) {
            Utils.log("JSEvaluate", "Response json error  = " + ex);
            return null;
        }

    }

    private String getJsonString(String js) {
        RequestJsonBuilder req = new RequestJsonBuilder(1, AndroidWebDriverConstants.RUNTIME_EVALUATE, js);
        String reqJson = req.getJsonString();
        //Utils.log("JSEvaluate","Request json = " + reqJson);
        return reqJson;
    }

}

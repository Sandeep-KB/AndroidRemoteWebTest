/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.driver;

import org.openqa.selenium.WebDriverException;
import sandeep.kb.android.remote.android.AndroidWebDriver;
import sandeep.kb.android.remote.utils.AdbHelper;
import sandeep.kb.android.remote.constants.AndroidWebDriverConstants;
import sandeep.kb.android.remote.utils.Utils;
import sandeep.kb.android.remote.websocket.AndroidWebSocketException;

/**
 * Used as an entry class to the remote driver. Its extending AndroidWebDriver,
 * which has implemented all the selenium webdriver methods
 *
 * @author Sandeep
 */
public class AndroidRemoteWebDriver extends AndroidWebDriver {

    public AndroidRemoteWebDriver(String webSocketUrl,String deviceId) {
        super();
        setWsUrl(webSocketUrl);
        setDeviceId(deviceId);
        
    }

    public AndroidRemoteWebDriver(String webSocketUrl) {
        super();
        
        setWsUrl(webSocketUrl);
        Utils.log("AndroidWebDriver", "Url = " + getWsUrl());
        
        String deviceID = System.getProperty("deviceID");     
        if(deviceID != null && !deviceID.equals("null")){
            setDeviceId(deviceID);
        }
        Utils.log("AndroidWebDriver", "DeviceId = " + deviceID);   
        
       
    }

   
    public AndroidRemoteWebDriver() {
        super();
        
        String deviceID = System.getProperty("deviceID");
        String wsUrl = System.getProperty("wsUrl");
        
        Utils.log("AndroidWebDriver", "DeviceId = " + deviceID);
        setDeviceId(deviceID);
        
        Utils.log("AndroidWebDriver", "Url = " + wsUrl);      
        if (wsUrl != null && !wsUrl.equals("null")) {
            this.setWsUrl(wsUrl);
        } else {
            Utils.log("AndroidRemoteWebDriver", "WsUrl is null");
           throw new WebDriverException("Web socket url not specified");
        }
       
    }

  
 

}

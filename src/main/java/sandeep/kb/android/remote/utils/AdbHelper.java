/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Has all the methods related to AndroidDebugBridg.
 *
 * @author Sandeep
 */
public class AdbHelper {

    public static void executeShellCommand(String deviceId, String command) {
        if (deviceId != null && !deviceId.equals("null")) {
            Utils.log("Adb Helper", "Device ID specified");
            executeInProcess("adb -d " + deviceId + " shell " + command);
        } else {
            executeInProcess("adb shell " + command);
        }
    }

    public static void executeCommand(String cmd) {
        executeInProcess("adb " + cmd);
    }

    private static StringBuffer executeInProcess(String cmd) {
        Utils.log("AdbHelper", "Command to execute =" + cmd);
        StringBuffer sb = new StringBuffer();
        try {
            Process pr = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            pr.waitFor();
            in.close();

        } catch (IOException ex) {
            Logger.getLogger(AdbHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(AdbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        Utils.log("ADBHELPER", "Executed command, res =" + sb.toString());
        return sb;
    }

    public static void sendKeysViaAdb(String deviceId, CharSequence cseq) {
        String str = cseq.toString();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '"':
                    executeShellCommand(deviceId, "input text '\\\"'");
                    break;
                case '\'':
                    executeShellCommand(deviceId, "input keyevent 75");
                    break;
                case ' ':
                    executeShellCommand(deviceId, "input keyevent 62");
                    break;
                case '*':
                     executeShellCommand(deviceId, "input keyevent 17");
                     break;   
                default:
                    executeShellCommand(deviceId, "input text '" + ch + "'");
                    break;
            }
            Utils.sleep(10);

        }
    }

}

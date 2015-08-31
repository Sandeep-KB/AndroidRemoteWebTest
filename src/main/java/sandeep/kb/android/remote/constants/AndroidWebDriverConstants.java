/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandeep.kb.android.remote.constants;

/**
 *Constants used in the project
 * @author Sandeep
 */
public class AndroidWebDriverConstants {

    public static final String JS_LOAD_URL = "window.location.href = ";
    public static final String JS_GET_URL = "window.location.toString()";
    public static final String JS_GET_TITLE = "window.document.title";
     
    public static final int DEFAULT_WS_PORT = 9222;
   
    public static final String PACKAGE_GOOGLE_CHROME = "com.android.chrome";
    
    public static final long SLEEP_LONG = 5000;
    public static final long SLEEP_SMALL = 2000;
    
    public static final String RUNTIME_EVALUATE = "Runtime.evaluate";
    public static final String ATOM_PATH = "/selenium/android/atoms/";
    public static final String FILE_EXTN_ATOM = ".atom";
    
}

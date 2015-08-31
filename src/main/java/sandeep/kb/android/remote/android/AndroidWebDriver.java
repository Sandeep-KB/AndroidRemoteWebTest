/*
 Copyright 2011 Selenium committers

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 A copy of the source code from selenium, removed android dependency
 */
package sandeep.kb.android.remote.android;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Beta;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
//import org.openqa.selenium.HasTouchScreen;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
//import org.openqa.selenium.TouchScreen;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.AppCacheStatus;
import org.openqa.selenium.html5.ApplicationCache;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.ErrorCodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.SessionStorage;
import sandeep.kb.android.remote.driver.JSEvaluate;
import sandeep.kb.android.remote.utils.AdbHelper;
import sandeep.kb.android.remote.utils.Utils;
import sandeep.kb.android.remote.websocket.AndroidWebSocketException;

public class AndroidWebDriver implements WebDriver, SearchContext,
        JavascriptExecutor, TakesScreenshot, HasTouchScreen, WebStorage,
        ApplicationCache {

    private static final String ELEMENT_KEY = "ELEMENT";
    private static final String WINDOW_KEY = "WINDOW";
    private static final String STATUS = "status";
    private static final String VALUE = "value";

    private AndroidWebElement element;
    private DomWindow currentWindowOrFrame;
    private long implicitWait = 0;
    private long loadingTimeOut = 60000L;

    // Maps the element ID to the AndroidWebElement
    private Map<String, AndroidWebElement> store;
    private AndroidNavigation navigation;
    private AndroidOptions options;
	//private AndroidLocalStorage localStorage;
    //private AndroidSessionStorage sessionStorage;
    private AndroidFindBy findBy;

	// Use for control redirect, contains the last url loaded (updated after
    // each redirect)
    private volatile String lastUrlLoaded;

    //private ViewAdapter view;
    private volatile boolean pageDoneLoading;
    private volatile boolean editAreaHasFocus;

    private volatile String result;
    private volatile boolean resultReady;

	// Timeouts in milliseconds
    private static final long START_LOADING_TIMEOUT = 700L;
    static final long RESPONSE_TIMEOUT = 10000L;
    private static final long FOCUS_TIMEOUT = 1000L;
    private static final long POLLING_INTERVAL = 50L;
    static final long UI_TIMEOUT = 3000L;

    private boolean acceptSslCerts;
    private volatile boolean pageStartedLoading;
    private boolean done = false;

    private String deviceId;
    private String wsUrl;
    private JSEvaluate jseval;

    private AndroidWebElement getOrCreateWebElement(String id) {
        if (store.get(id) != null) {
            return store.get(id);
        } else {
            AndroidWebElement toReturn = new AndroidWebElement(this, id);
            store.put(id, toReturn);
            return toReturn;
        }
    }

    public void setAcceptSslCerts(boolean accept) {
        acceptSslCerts = accept;
    }

    public boolean getAcceptSslCerts() {
        return acceptSslCerts;
    }

    private void initDriverState() {
        store = Maps.newHashMap();
        findBy = new AndroidFindBy();
        currentWindowOrFrame = new DomWindow("");
        store = Maps.newHashMap();
        navigation = new AndroidNavigation();
        options = new AndroidOptions();
        element = getOrCreateWebElement("");
		//localStorage = new AndroidLocalStorage(this);
        //sessionStorage = new AndroidSessionStorage(this);
        jseval = new JSEvaluate();
    }

    /**
     * Use this contructor to use WebDriver with a WebView that has the same
     * settings as the Android browser.
     *
     * @param activity the activity context where the WebView will be created.
     */
    protected AndroidWebDriver() {
        initDriverState();
    }


    String getLastUrlLoaded() {
        return lastUrlLoaded;
    }

    void setLastUrlLoaded(String url) {
        this.lastUrlLoaded = url;
    }

    void setEditAreaHasFocus(boolean focused) {
        editAreaHasFocus = focused;
    }

    boolean getEditAreaHasFocus() {
        return editAreaHasFocus;
    }

    void waitForPageToLoad() {
        sleepQuietly(200);
        pageDoneLoading = false;
        pageStartedLoading = true;
        long endTime = System.currentTimeMillis() + loadingTimeOut;
        while (System.currentTimeMillis() < endTime) {
            String pageLoadState = executeJavascriptInWebView("document.readyState");
            if ("complete".equals(pageLoadState)) {
                resetPageIsLoading();
                return;
            } else {
                continue;
            }
        }
        throw new WebDriverException("Web Page failed to load after waiting for " + loadingTimeOut + " millis. Please increase the timeout in WebDriver manager.");

    }

    void waitUntilEditAreaHasFocus() {
        long timeout = System.currentTimeMillis() + FOCUS_TIMEOUT;
        while (!editAreaHasFocus && (System.currentTimeMillis() < timeout)) {
            try {
                Thread.sleep(POLLING_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getCurrentUrl() {

        waitForPageToLoad();
        return executeJavascriptInWebView("document.URL");
    }

    public String getTitle() {

        waitForPageToLoad();
        return executeJavascriptInWebView("document.title");
    }

    public void get(String url) {
        navigation.to(url);
    }

    public String getPageSource() {
        return (String) executeScript("document.documentElement");
    }

    public void close() {

        done = false;
        long end = System.currentTimeMillis() + RESPONSE_TIMEOUT;

    }

    public void quit() {

    }

    public WebElement findElement(By by) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                return by.findElement(findBy);
            } catch (NoSuchElementException e) {
                if (System.currentTimeMillis() - start > implicitWait) {
                    throw e;
                }
                sleepQuietly(100);
            }
        }
    }

    public List<WebElement> findElements(By by) {
        long start = System.currentTimeMillis();
        List<WebElement> found = by.findElements(findBy);
        while (found.isEmpty()
                && (System.currentTimeMillis() - start <= implicitWait)) {
            sleepQuietly(100);
            found = by.findElements(findBy);
        }
        return found;
    }

    public AppCacheStatus getStatus() {
        Long scriptRes = (Long) executeRawScript("("
                + AndroidAtoms.GET_APPCACHE_STATUS.getValue() + ")()");
        return AppCacheStatus.getEnum(scriptRes.intValue());
    }

    @Override
    public LocalStorage getLocalStorage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SessionStorage getSessionStorage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class AndroidFindBy implements SearchContext, FindsByTagName,
            FindsById, FindsByLinkText, FindsByName, FindsByXPath,
            FindsByCssSelector, FindsByClassName {

        public WebElement findElement(By by) {
            long start = System.currentTimeMillis();
            while (true) {
                try {
                    return by.findElement(findBy);
                } catch (NoSuchElementException e) {
                    if (System.currentTimeMillis() - start > implicitWait) {
                        throw e;
                    }
                    sleepQuietly(100);
                }
            }
        }

        public List<WebElement> findElements(By by) {
            long start = System.currentTimeMillis();
            List<WebElement> found = by.findElements(findBy);
            while (found.isEmpty()
                    && (System.currentTimeMillis() - start <= implicitWait)) {
                sleepQuietly(100);
                found = by.findElements(this);
            }
            return found;
        }

        public WebElement findElementByLinkText(String using) {
            return element.getFinder().findElementByLinkText(using);
        }

        public List<WebElement> findElementsByLinkText(String using) {
            return element.getFinder().findElementsByLinkText(using);
        }

        public WebElement findElementById(String id) {
            return element.getFinder().findElementById(id);
        }

        public List<WebElement> findElementsById(String id) {
            return findElementsByXPath("//*[@id='" + id + "']");
        }

        public WebElement findElementByName(String using) {
            return element.getFinder().findElementByName(using);
        }

        public List<WebElement> findElementsByName(String using) {
            return element.getFinder().findElementsByName(using);
        }

        public WebElement findElementByTagName(String using) {
            return element.getFinder().findElementByTagName(using);
        }

        public List<WebElement> findElementsByTagName(String using) {
            return element.getFinder().findElementsByTagName(using);
        }

        public WebElement findElementByXPath(String using) {
            return element.getFinder().findElementByXPath(using);
        }

        public List<WebElement> findElementsByXPath(String using) {
            return element.getFinder().findElementsByXPath(using);
        }

        public WebElement findElementByPartialLinkText(String using) {
            return element.getFinder().findElementByPartialLinkText(using);
        }

        public List<WebElement> findElementsByPartialLinkText(String using) {
            return element.getFinder().findElementsByPartialLinkText(using);
        }

        public WebElement findElementByCssSelector(String using) {
            return element.getFinder().findElementByCssSelector(using);
        }

        public List<WebElement> findElementsByCssSelector(String using) {
            return element.getFinder().findElementsByCssSelector(using);
        }

        public WebElement findElementByClassName(String using) {
            return element.getFinder().findElementByClassName(using);
        }

        public List<WebElement> findElementsByClassName(String using) {
            return element.getFinder().findElementsByClassName(using);
        }
    }

    public static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new WebDriverException(cause);
        }
    }

    public Navigation navigate() {
        return navigation;
    }

    public boolean isJavascriptEnabled() {
        return true;
    }

    public Object executeScript(String script, Object... args) {
        return injectJavascript(script, false, args);
    }

    public Object executeAsyncScript(String script, Object... args) {
        throw new UnsupportedOperationException(
                "This is feature will be implemented soon!");
    }

    /**
     * Converts the arguments passed to a JavaScript friendly format.
     *
     * @param args The arguments to convert.
     * @return Comma separated Strings containing the arguments.
     */
    private String convertToJsArgs(final Object... args) {
        StringBuilder toReturn = new StringBuilder();
        int length = args.length;
        for (int i = 0; i < length; i++) {
            toReturn.append((i > 0) ? "," : "");
            if (args[i] instanceof List<?>) {
                toReturn.append("[");
                List<Object> aList = (List<Object>) args[i];
                for (int j = 0; j < aList.size(); j++) {
                    String comma = ((j == 0) ? "" : ",");
                    toReturn.append(comma + convertToJsArgs(aList.get(j)));
                }
                toReturn.append("]");
            } else if (args[i] instanceof Map<?, ?>) {
                Map<Object, Object> aMap = (Map<Object, Object>) args[i];
                String toAdd = "{";
                for (Object key : aMap.keySet()) {
                    toAdd += key + ":" + convertToJsArgs(aMap.get(key)) + ",";
                }
                toReturn.append(toAdd.substring(0, toAdd.length() - 1) + "}");
            } else if (args[i] instanceof WebElement) {
				// A WebElement is represented in JavaScript by an Object as
                // follow: {"ELEMENT":"id"} where "id" refers to the id
                // of the HTML element in the javascript cache that can
                // be accessed throught bot.inject.cache.getCache_()
                toReturn.append("{\"" + ELEMENT_KEY + "\":\""
                        + ((AndroidWebElement) args[i]).getId() + "\"}");
            } else if (args[i] instanceof DomWindow) {
				// A DomWindow is represented in JavaScript by an Object as
                // follow {"WINDOW":"id"} where "id" refers to the id of the
                // DOM window in the cache.
                toReturn.append("{\"" + WINDOW_KEY + "\":\""
                        + ((DomWindow) args[i]).getKey() + "\"}");
            } else if (args[i] instanceof Number || args[i] instanceof Boolean) {
                toReturn.append(String.valueOf(args[i]));
            } else if (args[i] instanceof String) {
                toReturn.append(escapeAndQuote((String) args[i]));
            } else {
                throw new IllegalArgumentException(
                        "Javascript arguments can be "
                        + "a Number, a Boolean, a String, a WebElement, "
                        + "or a List or a Map of those. Got: "
                        + ((args[i] == null) ? "null" : args[i]
                                .getClass()
                                + ", value: "
                                + args[i].toString()));
            }
        }
        return toReturn.toString();
    }

    /**
     * Wraps the given string into quotes and escape existing quotes and
     * backslashes. "foo" -> "\"foo\"" "foo\"" -> "\"foo\\\"\"" "fo\o" ->
     * "\"fo\\o\""
     *
     * @param toWrap The String to wrap in quotes
     * @return a String wrapping the original String in quotes
     */
    private static String escapeAndQuote(final String toWrap) {
        StringBuilder toReturn = new StringBuilder("\"");
        for (int i = 0; i < toWrap.length(); i++) {
            char c = toWrap.charAt(i);
            if (c == '\"') {
                toReturn.append("\\\"");
            } else if (c == '\\') {
                toReturn.append("\\\\");
            } else {
                toReturn.append(c);
            }
        }
        toReturn.append("\"");
        return toReturn.toString();
    }

    void writeTo(String name, String toWrite) {
        try {
            File f = new File(".", name);
            FileWriter w = new FileWriter(f);
            w.append(toWrite);
            w.flush();
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object executeRawScript(String toExecute) {
        String result = null;
        /*
         * result = executeJavascriptInWebView("window.webdriver.resultMethod("
         * + toExecute + ")");
         */
        result = executeJavascriptInWebView(toExecute);

        System.out.println("Result === " + result);

        if (result == null || "undefined".equals(result) || "null".equals(result)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(result);
            throwIfError(json);
            Object value = json.get(VALUE);
            return convertJsonToJavaObject(value);
        } catch (JSONException e) {
            throw new RuntimeException("Failed to parse JavaScript result: "
                    + result.toString(), e);
        }
    }

    Object executeAtom(String toExecute, Object... args) {
        String scriptInWindow = "(function(){ " + " var win; try{win="
                + getWindowString() + "}catch(e){win=window;}"
                + "with(win){return (" + toExecute + ")("
                + convertToJsArgs(args) + ")}})()";
        return executeRawScript(scriptInWindow);
    }

    private String getWindowString() {
        String window = "";
        if (!currentWindowOrFrame.getKey().equals("")) {
            window = "document['$wdc_']['" + currentWindowOrFrame.getKey()
                    + "'] ||";
        }
        return (window += "window;");
    }

    Object injectJavascript(String toExecute, boolean isAsync, Object... args) {
        String executeScript = AndroidAtoms.EXECUTE_SCRIPT.getValue();
        toExecute = "var win_context; try{win_context= " + getWindowString()
                + "}catch(e){" + "win_context=window;}with(win_context){"
                + toExecute + "}";
        String wrappedScript = "(function(){" + "var win; try{win="
                + getWindowString() + "}catch(e){win=window}"
                + "with(win){return (" + executeScript + ")("
                + escapeAndQuote(toExecute) + ", [" + convertToJsArgs(args)
                + "], true)}})()";
        return executeRawScript(wrappedScript);
    }

    private Object convertJsonToJavaObject(final Object toConvert) {
        try {
            if (toConvert == null || toConvert.equals(null)
                    || "undefined".equals(toConvert)
                    || "null".equals(toConvert)) {
                return null;
            } else if (toConvert instanceof Boolean) {
                return toConvert;
            } else if (toConvert instanceof Double
                    || toConvert instanceof Float) {
                return Double.valueOf(String.valueOf(toConvert));
            } else if (toConvert instanceof Integer
                    || toConvert instanceof Long) {
                return Long.valueOf(String.valueOf(toConvert));
            } else if (toConvert instanceof JSONArray) { // List
                return convertJsonArrayToList((JSONArray) toConvert);
            } else if (toConvert instanceof JSONObject) { // Map or WebElment
                JSONObject map = (JSONObject) toConvert;
                if (map.opt(ELEMENT_KEY) != null) { // WebElement
                    return getOrCreateWebElement((String) map.get(ELEMENT_KEY));
                } else if (map.opt(WINDOW_KEY) != null) { // DomWindow
                    return new DomWindow((String) map.get(WINDOW_KEY));
                } else { // Map
                    return convertJsonObjectToMap(map);
                }
            } else {
                return toConvert.toString();
            }
        } catch (JSONException e) {
            throw new RuntimeException("Failed to parse JavaScript result: "
                    + toConvert.toString(), e);
        }
    }

    private List<Object> convertJsonArrayToList(final JSONArray json) {
        List<Object> toReturn = Lists.newArrayList();
        for (int i = 0; i < json.length(); i++) {
            try {
                toReturn.add(convertJsonToJavaObject(json.get(i)));
            } catch (JSONException e) {
                throw new RuntimeException("Failed to parse JSON: "
                        + json.toString(), e);
            }
        }
        return toReturn;
    }

    private Map<Object, Object> convertJsonObjectToMap(final JSONObject json) {
        Map<Object, Object> toReturn = Maps.newHashMap();
        for (Iterator it = json.keys(); it.hasNext();) {
            String key = (String) it.next();
            try {
                Object value = json.get(key);
                toReturn.put(convertJsonToJavaObject(key),
                        convertJsonToJavaObject(value));
            } catch (JSONException e) {
                throw new RuntimeException("Failed to parse JSON:"
                        + json.toString(), e);
            }
        }
        return toReturn;
    }

    private void throwIfError(final JSONObject jsonObject) {
        int status;
        String errorMsg;
        try {
            status = (Integer) jsonObject.get(STATUS);
            errorMsg = String.valueOf(jsonObject.get(VALUE));
        } catch (JSONException e) {
            throw new RuntimeException("Failed to parse JSON Object: "
                    + jsonObject, e);
        }
        switch (status) {
            case ErrorCodes.SUCCESS:
                return;
            case ErrorCodes.NO_SUCH_ELEMENT:
                throw new NoSuchElementException("Could not find " + "WebElement.");
            case ErrorCodes.STALE_ELEMENT_REFERENCE:
                throw new StaleElementReferenceException("WebElement is stale.");
            default:
                if (jsonObject.toString().contains(
                        "Result of expression 'd.evaluate' [undefined] is"
                        + " not a function.")) {
                    throw new WebDriverException(
                            "You are using a version of Android WebDriver APK"
                            + " compatible with ICS SDKs or more recent SDKs. For more info take a look at"
                            + " http://code.google.com/p/selenium/wiki/AndroidDriver#Supported_Platforms. Error:"
                            + " " + jsonObject.toString());
                }
                throw new WebDriverException("Error: " + errorMsg);
        }
    }

    /**
     * Executes the given Javascript in the WebView and wait until it is done
     * executing. If the Javascript executed returns a value, the later is
     * updated in the class variable jsResult when the event is broadcasted.
     *
     * @param script the Javascript to be executed
     */
    private String executeJavascriptInWebView(final String script) {

        result = null;
        resultReady = false;
        try {
            result = jseval.evaluate(script, wsUrl);
        } catch (AndroidWebSocketException ex) {
            Logger.getLogger(AndroidWebDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
        resultReady = true;
        return result;

    }

    protected Object processJsonObject(Object res) throws JSONException {
        if (res instanceof JSONArray) {
            return convertJsonArray2List((JSONArray) res);
        } else if ("undefined".equals(res)) {
            return null;
        }
        return res;
    }

    private List<Object> convertJsonArray2List(JSONArray arr)
            throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(processJsonObject(arr.get(i)));
        }
        return list;
    }

    public void setProxy(String host, int port) {
        if ((host != null) && (host.length() > 0)) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", host);
            System.getProperties().put("proxyPort", port);
        }
    }

    public Options manage() {
        return options;
    }

    private class AndroidOptions implements Options {

        public Logs logs() {
            throw new UnsupportedOperationException(
                    "Not implementing Logs just yet.");
        }

        public Timeouts timeouts() {
            return new AndroidTimeouts();
        }

        public ImeHandler ime() {
            throw new UnsupportedOperationException(
                    "Not implementing IME input just yet.");
        }

        @Beta
        public Window window() {
            throw new UnsupportedOperationException(
                    "Window handling not supported on Android");
        }

        @Override
        public void addCookie(Cookie arg0) {
            throw new UnsupportedOperationException(
                    "Not implementing Cookies just yet.");

        }

        @Override
        public void deleteAllCookies() {
            throw new UnsupportedOperationException(
                    "Not implementing Cookies just yet.");

        }

        @Override
        public void deleteCookie(Cookie arg0) {
            throw new UnsupportedOperationException(
                    "Not implementing Cookies just yet.");

        }

        @Override
        public void deleteCookieNamed(String arg0) {
            throw new UnsupportedOperationException(
                    "Not implementing Cookies just yet.");

        }

        @Override
        public Cookie getCookieNamed(String arg0) {
            throw new UnsupportedOperationException(
                    "Not implementing Cookies just yet.");

        }

        @Override
        public Set<Cookie> getCookies() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException(
                    "Not implementing Cookies just yet.");

        }

    }

    private class AndroidTimeouts implements Timeouts {

        public Timeouts implicitlyWait(long time, TimeUnit unit) {
            implicitWait = TimeUnit.MILLISECONDS.convert(Math.max(0, time),
                    unit);
            return this;
        }

        public Timeouts setScriptTimeout(long time, TimeUnit unit) {
			// asyncScriptTimeout = TimeUnit.MILLISECONDS.convert(Math.max(0,
            // time), unit);
            return this;
        }

        public Timeouts pageLoadTimeout(long time, TimeUnit unit) {
            loadingTimeOut = TimeUnit.MILLISECONDS.convert(Math.max(0, time),
                    unit);
            return this;
        }
    }

    /**
     * Implementing all the navigate methods in javascript.
     */
    private class AndroidNavigation implements Navigation {

        @Override
        public void back() {
            resetPageIsLoading();
            executeJavascriptInWebView("window.history.back();");
            waitForPageToLoad();
        }

        @Override
        public void forward() {
            resetPageIsLoading();
            executeJavascriptInWebView("window.history.forward();");
            waitForPageToLoad();
        }

        @Override
        public void refresh() {
            resetPageIsLoading();
            executeJavascriptInWebView("location.reload();");
            waitForPageToLoad();
        }

        @Override
        public void to(String url) {
            resetPageIsLoading();
            executeJavascriptInWebView("window.document.location.href=\"" + url + "\"");
            waitForPageToLoad();
        }

        @Override
        public void to(URL Url) {
            String url = Url.toString();
            executeJavascriptInWebView("window.document.location.href=\"" + url + "\"");
            waitForPageToLoad();
        }
    }

    @Override
    public TouchScreen getTouch() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        
        if (target.equals(OutputType.FILE)) {
            File screenshot = new File(".", "Screenshot.png");
            AdbHelper.executeShellCommand(getDeviceId(),"screencap -p /sdcard/AndroidRemoteTest_screenshot.png");
            AdbHelper.executeCommand("pull /sdcard/AndroidRemoteTest_screenshot.png " + screenshot.getAbsolutePath());
            AdbHelper.executeShellCommand(getDeviceId(),"rm /sdcard/AndroidRemoteTest_screenshot.png");
            return (X) screenshot;
        }
        return null;
        
    }

    @Override
    public TargetLocator switchTo() {
        // TODO Auto-generated method stub
        return null;
    }

    public void resetPageIsLoading() {
        pageStartedLoading = false;
        pageDoneLoading = true;
    }

    @Override
    public String getWindowHandle() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Not implementing WindowHandle just yet.");
    }

    @Override
    public Set<String> getWindowHandles() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Not implementing WindowHandle just yet.");
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

}

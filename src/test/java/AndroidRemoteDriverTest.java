/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import sandeep.kb.android.remote.driver.AndroidRemoteWebDriver;
import sandeep.kb.android.remote.utils.Utils;

/**
 *
 * @author Sandeep
 */
public class AndroidRemoteDriverTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        AndroidRemoteWebDriver driver = new AndroidRemoteWebDriver("ws://localhost:9222/devtools/page/4");    
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.get("http://www.google.com");
        Utils.sleep(2000);
        WebElement elm = driver.findElement(By.name("q"));
        Utils.sleep(2000);
        elm.sendKeys(" // \\ ' * [@id=\"rg_s\"]/div[1]/a ");
        elm.submit();
        //Utils.sleep(2000);
        elm = driver.findElement(By.partialLinkText("Images"));
        elm.click();
        Utils.sleep(2000);
        elm = driver.findElement(By.xpath("//*[@id=\"rg_s\"]/div[1]/a"));
        elm.click();
        Utils.sleep(2000);
        System.out.println("Page source :/n"+driver.getPageSource());
        
    }
    
}

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ReCaptchaV2 extends CaptchaSolver {
    private URL pageUrl;
    private String reqId;
    private String token;
    private String googleKey;
    private final String method = "userrecaptcha";

    /**
     * Implemented to work with Selenium WebDrivers.
     * solve get the required data from the website using DOM and solves the ReCaptchaV2.
     * @param d The Selenium webdriver required from method to work.
     * @throws IOException when driver cant find the required data from the website.
     * @throws InterruptedException when Thread.sleep() fails.
     * @return the updated WebDriver to the user.
     */
    public WebDriver solve(WebDriver d) throws IOException, InterruptedException {
        pageUrl = new URL(d.getCurrentUrl());

        try{
            WebElement captchaElement = d.findElement(By.cssSelector("*[data-sitekey]"));
            googleKey = captchaElement.getAttribute("data-sitekey");
        }catch (Exception e){
            throw new IOException("No Recaptcha V2 Found.");
        }

        postToServer();
        getFromServer();

        JavascriptExecutor js = (JavascriptExecutor) d;
        js.executeScript("document.getElementById(\"g-recaptcha-response\").innerHTML=\"" + token + "\";");

        try{
            WebElement submitButton = d.findElement(By.cssSelector("*[type='submit']"));
            submitButton.click();
        }catch (Exception e){
            throw new IOException("Captcha Solver: No Submit button found.");
        }
        return d;
    }

    /**
     * Making a POST request to 2Captcha's API with all the required parameters.
     * @throws IOException when 2Captcha's server is rejecting the request.
     */
    public void postToServer() throws IOException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("key", getApiKey());
        parameters.put("method", method);
        parameters.put("googlekey", googleKey);
        parameters.put("pageUrl", pageUrl.toString());
        HttpURLConnection con = (HttpURLConnection) new URL(getInPath().toString() + paramsToQuery(parameters)).openConnection();
        con.setRequestMethod("POST");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String s = in.readLine();
        if(s.contains("OK|")){
            reqId = s.substring(3);
        }else{
            throw new IOException("Captcha Solver: Error message from server on post. (" + s +")");
        }
    }

    /**
     * Making a GET request to 2Captcha's API with all the required parameters.
     * @throws IOException when 2Captcha's server is rejecting the request.
     * @throws InterruptedException when Thread.sleep() fails.
     */
    public void getFromServer() throws IOException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        boolean longSleep = true;
        parameters.put("key", getApiKey());
        parameters.put("action", "get");
        parameters.put("id", reqId);
        do{
            HttpURLConnection con = (HttpURLConnection) new URL(getResPath().toString() + paramsToQuery(parameters)).openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String s = in.readLine();
            if(s.contains("CAPCHA_NOT_READY")){
                if(longSleep) {
                    Thread.sleep(15000);
                    longSleep = false;
                }else {
                    Thread.sleep(5000);
                }
                System.out.println("Captcha Solver: Not ready yet, sleeping for 15 seconds.");
            }else if(s.contains("OK|")){
                token = s.substring(3);
                break;
            }else{
                System.out.println(s);
                throw new IOException("Captcha Solver: Error message from server on post. (" + s +")");
            }
        }while (true);
    }
}

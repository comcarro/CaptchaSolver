import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @author ebektasiadis
 * @version 1.0
 * Abstract class describing a Captcha Solver including variables / methods that are required from all the solvers to work.
 */
abstract class CaptchaSolver {
    private String apiKey;
    private URL inPath = null, resPath = null;

    /**
     * When a CaptchaSolver object is getting constructed we set inPath, resPath to certain URLs.
     */
    CaptchaSolver() {
        try{
            inPath = new URL("http://2captcha.com/in.php");
            resPath = new URL("http://2captcha.com/res.php");
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    //Setters
    public void setApiKey(String apiKey) {
        System.out.println("Captcha Solver: API Key has been set");
        this.apiKey = apiKey;
    }

    //Getters
    public String getApiKey() {
        return this.apiKey;
    }

    public URL getInPath() {
        return inPath;
    }

    public URL getResPath() {
        return resPath;
    }

    /**
     * Converting the parameters Map to Query
     * @param params    The Map with the parameters we want to include to POST/GET method.
     * @return          Returns a string with all parameters concat'd.
     */
    public String paramsToQuery(Map<String, String> params){
        String query = "?";

        for (Map.Entry<String, String> entry : params.entrySet()) {
            query += entry.getKey() + "=" + entry.getValue() + "&";
        }

        query = query.substring(0, query.length()-1);
        return query;
    }
}
package server;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.*;
import java.io.IOException;
import java.util.*;

public class TwitterToJSON {
    private String consumerKeyStr;
    private String consumerSecretStr;
    private String accessTokenStr;
    private String accessTokenSecretStr;
    private OAuthConsumer oAuthConsumer;
    private String listId = "1042387432266772480";

    public TwitterToJSON(String consumerKeyStr, String consumerSecretStr, String accessTokenStr, String accessTokenSecretStr){
        this.consumerKeyStr = consumerKeyStr;
        this.consumerSecretStr = consumerSecretStr;
        this.accessTokenStr = accessTokenStr;
        this.accessTokenSecretStr = accessTokenSecretStr;
        oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr, consumerSecretStr);
        oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
    }

    private String get(String uri) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException {
        HttpGet req = new HttpGet(uri);
        oAuthConsumer.sign(req);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(req);
       // int statusCode = httpResponse.getStatusLine().getStatusCode();
        //System.out.println(statusCode + ':' + httpResponse.getStatusLine().getReasonPhrase());
        return IOUtils.toString(httpResponse.getEntity().getContent());
    }

    public List<String> getTwitterTags(String listId) throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException {
        ArrayList<String> tags = new ArrayList<String>();
        JSONObject jo = new JSONObject(get("https://api.twitter.com/1.1/lists/members.json?list_id=" + listId +"&count=300&skip_status=true"));
        JSONArray ja = jo.getJSONArray("users");
        for (int i = 0; i < ja.length(); i++) {
            tags.add(ja.getJSONObject(i).getString("screen_name"));
        }
        return tags;
    }


    public static void main(String[] args) throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException {
        TwitterToJSON tj = new TwitterToJSON("5BX8LnhM6Xuc4XIl8zTtQtbZO", "WYlyvmUZOJr4RC4HK3OQnPGEncPk4kCZUcXhEZkEVsqmzLl1MC",
                "1206462660885262336-FsDtNrFoR1cWrS0ueVw2WuseJx8BtK", "OFFipEoXAinyP6oDzAYhpEPMenK79VEyl6VgF6L37KeKc");
        tj.getTwitterTags("1042387432266772480");
    }

}


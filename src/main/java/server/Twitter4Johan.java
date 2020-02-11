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

import java.io.IOException;
import java.util.*;

/**
 * Klass som tar Oauth1-uppgifter för att koppla upp sig mot Twitters API via en HTTP-förbindelse.
 * Efter ett Twitter4Johan-objekt skapats går det att hämta:
 *  - Twittertaggar och namn från de medlemmar som befinner sig i listan.
 *  - Tweets där en viss användare är taggad.
 */
public class Twitter4Johan {
    private String consumerKeyStr;
    private String consumerSecretStr;
    private String accessTokenStr;
    private String accessTokenSecretStr;
    private OAuthConsumer oAuthConsumer;

    /**
     * Konstruktor.
     * @param consumerKeyStr Twitter consumer key.
     * @param consumerSecretStr Twitter consumer secret.
     * @param accessTokenStr Twitter access token.
     * @param accessTokenSecretStr Twitter token secret.
     */
    public Twitter4Johan(String consumerKeyStr, String consumerSecretStr, String accessTokenStr, String accessTokenSecretStr){
        this.consumerKeyStr = consumerKeyStr;
        this.consumerSecretStr = consumerSecretStr;
        this.accessTokenStr = accessTokenStr;
        this.accessTokenSecretStr = accessTokenSecretStr;
        oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr, consumerSecretStr);
        oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
    }

    /**
     * Gör en HTTP-get request mot en uri och returnerar svaret.
     * @param uri En URI till en api.
     * @return String innehållande svaret från servern.
     * @throws OAuthCommunicationException
     * @throws OAuthExpectationFailedException
     * @throws OAuthMessageSignerException
     * @throws IOException
     */
    private String get(String uri) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException {
        HttpGet req = new HttpGet(uri);
        oAuthConsumer.sign(req);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(req);
        return IOUtils.toString(httpResponse.getEntity().getContent());
    }

    /**
     *
     * @param listId Twitter list-id.
     * @return List <String[]>. Arr[0] = Screen Name, Arr[1] = Name.
     * @throws OAuthExpectationFailedException
     * @throws OAuthCommunicationException
     * @throws OAuthMessageSignerException
     * @throws IOException
     */
    public List<String[]> getTwitterTags(String listId) throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException {
        ArrayList<String[]> tags = new ArrayList<String[]>();
        JSONObject jo = new JSONObject(get("https://api.twitter.com/1.1/lists/members.json?list_id=" + listId +"&count=300&skip_status=true"));
        JSONArray ja = jo.getJSONArray("users");
        for (int i = 0; i < ja.length(); i++) {
            tags.add(new String[] {ja.getJSONObject(i).getString("screen_name"), ja.getJSONObject(i).getString("name")});
        }
        return tags;
    }

    /**
     * Tar ett Twitter-användarnamn och returnerar en lista av Tweets i form av Status-objekt.
     * @param username
     * @return List<Status> List of tweets.
     * @throws OAuthExpectationFailedException
     * @throws OAuthCommunicationException
     * @throws OAuthMessageSignerException
     * @throws IOException
     */
    public List<Status> getTweets(String username) throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException {
        ArrayList<Status> tweets = new ArrayList<Status>();
        JSONObject jo = new JSONObject(get("https://api.twitter.com/1.1/search/tweets.json?q=%40" + username));
        JSONArray ja = jo.getJSONArray("statuses");
        for (int i = 0; i < ja.length(); i++){
            JSONObject temp = ja.getJSONObject(i);
            String author = "@" + temp.getJSONObject("user").getString("screen_name");
            String createdDate = temp.getString("created_at");
            String text = temp.getString("text");
            String location = temp.getJSONObject("user").getString("location");
            String profilePicture = temp.getJSONObject("user").getString("profile_image_url_https");
            tweets.add(new Status(author, createdDate, text, location, profilePicture));
        }
        return tweets;
    }


    public static void main(String[] args) throws OAuthExpectationFailedException, OAuthCommunicationException, OAuthMessageSignerException, IOException {
        Twitter4Johan tj = new Twitter4Johan("5BX8LnhM6Xuc4XIl8zTtQtbZO", "WYlyvmUZOJr4RC4HK3OQnPGEncPk4kCZUcXhEZkEVsqmzLl1MC",
                "1206462660885262336-FsDtNrFoR1cWrS0ueVw2WuseJx8BtK", "OFFipEoXAinyP6oDzAYhpEPMenK79VEyl6VgF6L37KeKc");
       
    }

}


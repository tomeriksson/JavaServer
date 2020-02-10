package server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Importer {

    /*
    Hämtar en json-fil från en URL och returnerar ett json-objekt av det.
     */
    private static javax.json.JsonObject urlToJson(URL url) throws IOException {
        javax.json.JsonObject obj;
        try (
                InputStream is = url.openStream();
                JsonReader rdr = Json.createReader(is)) {
            obj = rdr.readObject();
        }
        return obj;
    }

    /*
    Hämtar ledamöter från Riksdagens hemsida, filtrerar ut namn, parti och länk till en bild från riksdagens API.
     */
    public static String importLedamoter() throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {

        String urlStr = "http://data.riksdagen.se/personlista/?iid=&fnamn=&enamn=&f_ar=&kn=&parti=&valkrets=&rdlstatus=&org=&utformat=json&sort=parti&sortorder=asc&termlista=";
        URL url = new URL(urlStr);
        javax.json.JsonObject obj = Importer.urlToJson(url);
        javax.json.JsonObject personlista = obj.getJsonObject("personlista");
        JsonArray pArr = personlista.getJsonArray("person");
        JsonObject inner;
        com.google.gson.JsonArray outer = new com.google.gson.JsonArray();
        JsonObject shell = new JsonObject();
        String id, namn, parti, bild, bild_stor, tagg = null, status, valkrets, fodd, kon;
        ArrayList<LinkedList<String>> nameAndTagg = importTwittertags();

        for (int i = 0; i < pArr.size(); i++) {
            inner = new JsonObject();
            id = pArr.get(i).asJsonObject().getString("intressent_id");
            namn = pArr.get(i).asJsonObject().getString("tilltalsnamn") + " " + pArr.get(i).asJsonObject().getString("efternamn");
            parti = pArr.get(i).asJsonObject().getString("parti");
            bild = pArr.get(i).asJsonObject().getString("bild_url_80");
            bild_stor = pArr.get(i).asJsonObject().getString("bild_url_192");
            valkrets = pArr.get(i).asJsonObject().getString("valkrets");
            status = pArr.get(i).asJsonObject().getString("status");
            fodd = pArr.get(i).asJsonObject().getString("fodd_ar");
            kon = pArr.get(i).asJsonObject().getString("kon");
            int counter = 0;
            for (int j = 0; j < nameAndTagg.size(); j++){
                 LinkedList<String> row = nameAndTagg.get(j);
                for (int k = 0; k < row.size()-1; k++) {
                    if (namn.contains(row.get(k))) counter++;
                }
                String tag = row.getLast();
                if (counter == row.size()-1){
                    tagg = tag;
                    inner.addProperty("id", id);
                    inner.addProperty("namn", namn);
                    inner.addProperty("parti", parti);
                    inner.addProperty("bild", bild);
                    inner.addProperty("bild_stor", bild_stor);
                    inner.addProperty("tagg", tagg);
                    inner.addProperty("valkrets", valkrets);
                    inner.addProperty("status", status);
                    inner.addProperty("fodd", fodd);
                    inner.addProperty("kon", kon);
                    outer.add(inner);
                }else{
                    if (namn.contains(tag)){
                        tagg = tag;
                        inner.addProperty("id", id);
                        inner.addProperty("namn", namn);
                        inner.addProperty("parti", parti);
                        inner.addProperty("bild", bild);
                        inner.addProperty("bild_stor", bild_stor);
                        inner.addProperty("tagg", tagg);
                        inner.addProperty("valkrets", valkrets);
                        inner.addProperty("status", status);
                        inner.addProperty("fodd", fodd);
                        inner.addProperty("kon", kon);
                        outer.add(inner);
                    }
                }
                counter = 0;
            }
        }
        shell.add("ledamoter", outer);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String pretty = gson.toJson(shell);
        return pretty;
    }

    /*
     * Hämtar användare från en medlemslista på twitter och parar ihop namn på konton med namn på riksdagsledamöter.
     */
    private static ArrayList<LinkedList<String>> importTwittertags() throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        Twitter4Johan t4j = new Twitter4Johan("5BX8LnhM6Xuc4XIl8zTtQtbZO", "WYlyvmUZOJr4RC4HK3OQnPGEncPk4kCZUcXhEZkEVsqmzLl1MC",
                "1206462660885262336-FsDtNrFoR1cWrS0ueVw2WuseJx8BtK", "OFFipEoXAinyP6oDzAYhpEPMenK79VEyl6VgF6L37KeKc");
        ArrayList<LinkedList<String>> res = new ArrayList<>();
               List<String[]> users = t4j.getTwitterTags("1042387432266772480");

                String namn;
                String twitterTag;
                for (int i = 0; i < users.size(); i++) {
                    twitterTag = users.get(i)[0];
                    namn = users.get(i)[1];
                    // ttnamn
                    String[] a = namn.split(" ");

                    LinkedList<String> elem = new LinkedList<>();
                    for (int j = 0; j < a.length; j++) {
                        String gNamn = a[j];
                        char first = gNamn.charAt(0);
                        elem.add(Character.toTitleCase(first) + gNamn.substring(1));
                    }
                    if (!twitterTag.isEmpty()) {
                        elem.add("@" + twitterTag);
                    } else {
                        elem.add(null);
                    }
                    res.add(elem);
                }
            return res;
    }
    /*
     * Retrieves tweets from a specified politician. Uses Twitter4j library to
     * integrate our application with the Twitter services.
     */
    public static String getTweets(String keyWords) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        Twitter4Johan t4j = new Twitter4Johan("5BX8LnhM6Xuc4XIl8zTtQtbZO", "WYlyvmUZOJr4RC4HK3OQnPGEncPk4kCZUcXhEZkEVsqmzLl1MC",
                "1206462660885262336-FsDtNrFoR1cWrS0ueVw2WuseJx8BtK", "OFFipEoXAinyP6oDzAYhpEPMenK79VEyl6VgF6L37KeKc");
        List<Status> tweets = t4j.getTweets(keyWords);

        JsonObject shell = new JsonObject();
        com.google.gson.JsonArray outer = new com.google.gson.JsonArray();
        String userName, postedAt, tweetText, userLocation, profileImageURL;

        for (Status tweet : tweets) {
            // Build JSON File
            JsonObject inner = new JsonObject();

            userName = tweet.getScreenName();

            postedAt = tweet.getCreateDate();
            tweetText = tweet.getText();
            userLocation = tweet.getUserLocation();
            profileImageURL = tweet.getUserProfileImage();

            inner.addProperty("userName", userName);
            inner.addProperty("postedAt", postedAt);
            inner.addProperty("tweetText", tweetText);
            inner.addProperty("userLocation", userLocation);
            inner.addProperty("profileImageURL", profileImageURL);

            outer.add(inner);
        }
        shell.add("tweets", outer);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String JSON = gson.toJson(shell);
        return JSON;
    }


    public static void main (String[] args) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        System.out.println(getTweets("hanifbali"));

        }
}

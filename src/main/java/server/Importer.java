package server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

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
    public static String importLedamoter() throws IOException, TwitterException {

        String urlStr = "http://data.riksdagen.se/personlista/?iid=&fnamn=&enamn=&f_ar=&kn=&parti=&valkrets=&rdlstatus=&org=&utformat=json&sort=parti&sortorder=asc&termlista=";
        URL url = new URL(urlStr);
        javax.json.JsonObject obj = Importer.urlToJson(url);
        javax.json.JsonObject personlista = obj.getJsonObject("personlista");
        JsonArray pArr = personlista.getJsonArray("person");
        JsonObject inner;
        com.google.gson.JsonArray outer = new com.google.gson.JsonArray();
        JsonObject shell = new JsonObject();
        String namn, parti, bild, tagg = "null";
        ArrayList<LinkedList<String>> nameAndTagg = importTwittertags();

        for (int i = 0; i < pArr.size(); i++) {
            inner = new JsonObject();
            namn = pArr.get(i).asJsonObject().getString("tilltalsnamn") + " " + pArr.get(i).asJsonObject().getString("efternamn");
            parti = pArr.get(i).asJsonObject().getString("parti");
            bild = pArr.get(i).asJsonObject().getString("bild_url_80");
            int counter = 0;
            for (int j = 0; j < nameAndTagg.size(); j++){
                 LinkedList<String> row = nameAndTagg.get(j);
                for (int k = 0; k < row.size()-1; k++) {
                    if (namn.contains(row.get(k))) counter++;
                }
                String tag = row.getLast();
                if (counter == row.size()-1){
                    tagg = tag;
                }else{
                    if (namn.contains(tag)){
                        tagg = tag;
                    }
                }
                counter = 0;
            }
            inner.addProperty("namn", namn);
            inner.addProperty("parti", parti);
            inner.addProperty("bild", bild);
            inner.addProperty("tagg", tagg);
            outer.add(inner);
            tagg = "null";
        }
        shell.add("ledamoter", outer);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String pretty = gson.toJson(shell);
        return pretty;
    }

    private static ArrayList<LinkedList<String>> importTwittertags() throws IOException, TwitterException {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true).setOAuthConsumerKey("5BX8LnhM6Xuc4XIl8zTtQtbZO")
                    .setOAuthConsumerSecret("WYlyvmUZOJr4RC4HK3OQnPGEncPk4kCZUcXhEZkEVsqmzLl1MC")
                    .setOAuthAccessToken("1206462660885262336-FsDtNrFoR1cWrS0ueVw2WuseJx8BtK")
                    .setOAuthAccessTokenSecret("OFFipEoXAinyP6oDzAYhpEPMenK79VEyl6VgF6L37KeKc");
        ArrayList<String[]> twitterList = new ArrayList<String[]>();
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();
            long cursor = -1;
            PagableResponseList<User> users;
       ArrayList<LinkedList<String>> res = new ArrayList<>();
            do {
                users = twitter.getUserListMembers(Long.parseLong("1042387432266772480"), cursor);
                String namn;
                String twitterTag;
                for (int i = 0; i < users.size(); i++) {
                    User u = users.get(i);
                    namn = u.getName();
                    twitterTag = u.getScreenName();
                    // ttnamn
                    String[] a = namn.split(" ");

                    LinkedList<String> elem = new LinkedList<>();
                    for (int j = 0; j < a.length; j++){
                            String gNamn = a[j];
                            char first = gNamn.charAt(0);
                            elem.add(Character.toTitleCase(first) + gNamn.substring(1));
                    }
                    if(!twitterTag.isEmpty()){
                        elem.add("@" + twitterTag);
                    }else {
                        elem.add(null);
                    }
                    res.add(elem);
                }
            } while ((cursor = users.getNextCursor()) != 0);
            return res;
    }

        public static void main (String[] args) throws IOException, TwitterException {
            System.out.println(importLedamoter());

        }
}

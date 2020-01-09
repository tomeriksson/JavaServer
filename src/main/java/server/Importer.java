package server;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.*;
import java.net.URL;

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
    public static String importLedamoter() throws IOException {

        String urlStr = "http://data.riksdagen.se/personlista/?iid=&fnamn=&enamn=&f_ar=&kn=&parti=&valkrets=&rdlstatus=&org=&utformat=json&sort=parti&sortorder=asc&termlista=";
        URL url = new URL(urlStr);
        javax.json.JsonObject obj = Importer.urlToJson(url);
        javax.json.JsonObject personlista = obj.getJsonObject("personlista");
        JsonArray pArr = personlista.getJsonArray("person");
        JsonObject inner;
        com.google.gson.JsonArray outer = new com.google.gson.JsonArray();
        JsonObject shell = new JsonObject();
        String namn, parti, bild;
        for (int i = 0; i < pArr.size(); i++) {
            inner = new JsonObject();
            namn = pArr.get(i).asJsonObject().getString("tilltalsnamn") + " " + pArr.get(i).asJsonObject().getString("efternamn");
            parti = pArr.get(i).asJsonObject().getString("parti");
            bild = pArr.get(i).asJsonObject().getString("bild_url_80");
            inner.addProperty("namn", namn);
            inner.addProperty("parti", parti);
            inner.addProperty("bild", bild);
            outer.add(inner);
        }
        shell.add("ledamoter", outer);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String pretty = gson.toJson(shell);
      return pretty;
    }

    public static void main(String[] args) {
        try {
            System.out.println(Importer.importLedamoter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package Server;


import javax.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Importer {
    private static JsonObject urlToJson (URL url) throws IOException {
        JsonObject obj;
        try (
                InputStream is = url.openStream();
             JsonReader rdr = Json.createReader(is)) {
            obj = rdr.readObject();
        }
        return obj;
    }

    public static String  importLedamoter() throws IOException {

        String urlStr = "http://data.riksdagen.se/personlista/?iid=&fnamn=&enamn=&f_ar=&kn=&parti=&valkrets=&rdlstatus=&org=&utformat=json&sort=parti&sortorder=asc&termlista=";
            URL url = new URL(urlStr);
            JsonObject obj = Importer.urlToJson(url);

            JsonObject personlista = obj.getJsonObject("personlista");
            JsonArray pArr = personlista.getJsonArray("person");

        // facotry
            JsonBuilderFactory jbf = Json.createBuilderFactory(null);

            JsonArrayBuilder ledamotBuilder = jbf.createArrayBuilder();

            for (int i = 0; i < pArr.size(); i++) {
                String namn = pArr.get(i).asJsonObject().getString("tilltalsnamn") + " " + pArr.get(i).asJsonObject().getString("efternamn");

                ledamotBuilder.add(jbf.createObjectBuilder().add("person", jbf.createObjectBuilder().add("namn", namn)
                        .add("parti", pArr.get(i).asJsonObject().getString("parti"))
                        .add("bild", pArr.get(i).asJsonObject().getString("bild_url_80"))));
            }

        JsonObjectBuilder personBuilder = jbf.createObjectBuilder().add("ledamoter", ledamotBuilder);

      return personBuilder.build().toString();
    }

    public static void main(String[] args) {
        try {
           Importer.importLedamoter();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

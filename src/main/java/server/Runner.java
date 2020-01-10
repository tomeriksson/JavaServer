package server;
import twitter4j.TwitterException;

import java.io.*;
import static spark.Spark.*;

public class Runner {
    private static final int LEDAMOTER = 0;
    private static String[] resources = new String[3];

    private static void init() throws IOException, TwitterException {
        resources[LEDAMOTER] = Importer.importLedamoter();
    }

    public static void main(String[] args) {
        try {
            Runner.init();
        } catch (IOException | TwitterException e) {
            e.printStackTrace();
        }
        //Startar servern på port 5000.
        port(5000);

        //Skapar metoden get för URL/ledamoter, returnerar en json-sträng med namn, bild och parti.
        get("/ledamoter", (req, res) -> {
            res.type("application/json");
            res.status(200);
            return resources[LEDAMOTER];
        });
        System.out.println("Running on 5000...");
    }
}
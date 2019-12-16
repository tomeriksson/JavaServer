package Server;
import java.io.*;

import twitter4j.TwitterException;

import static spark.Spark.*;

public class Runner {
    private static final int LEDAMOTER = 0;
    private static final int TWEETS = 1;
    private static String[] resources = new String[3];

    private static void init() throws IOException {
        resources[LEDAMOTER] = Importer.importLedamoter();
        try {
			resources[TWEETS] = Importer.importTweets("Hanif Bali");
		} catch (TwitterException e) {
			e.printStackTrace();
		}
    }

    public static void main(String[] args) {
        try {
            Runner.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Starts the server at port 5000.
        port(5000);

        //Skapar metoden get för URL/ledamoter, returnerar en json-sträng med namn, bild och parti.
        get("/ledamoter", (req, res) -> {
            res.type("application/json");
            res.status(200);
            return resources[LEDAMOTER];
        });
        
        get("/tweets", (req, res) -> {
        	res.type("application/json");
        	res.status(200);
        	return resources[TWEETS];
        });
    }
}

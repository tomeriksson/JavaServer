package server;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import twitter4j.TwitterException;

import java.io.*;

import spark.Filter;
import spark.Spark;

import static spark.Spark.*;

public class Runner {
    private static final int LEDAMOTER = 0;
    private static String[] resources = new String[3];

    private static void init() throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
       resources[LEDAMOTER] = Importer.importLedamoter();
    }

    public static void main(String[] args) {
        try {
            Runner.init();
        } catch (IOException | OAuthCommunicationException | OAuthExpectationFailedException | OAuthMessageSignerException e) {
            e.printStackTrace();
        }
        //Startar servern på port 5000.
        port(5000);
        
        Spark.after((Filter) (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET");
        });


        //Skapar metoden get för URL/ledamoter, returnerar en json-sträng med namn, bild och parti.
        get("/ledamoter", (req, res) -> {

            res.type("application/json");
            res.status(200);
            return resources[LEDAMOTER];
        });
        //Skapar metoden get för URL/tweets/@<twittertag> som hämtar tweets utifrån en viss tag.
        get("/tweets/*", (req, res) -> {
            String tag = req.splat()[0];
            if (tag.charAt(0) == '@'){
                res.type("application/json");
                res.status(200);
                return Importer.getTweets(tag.substring(1));
            }else{
                res.status(401);
                return "not a twitter tag.";
            }
        });

        System.out.println("Running on 5000...");
    }
}

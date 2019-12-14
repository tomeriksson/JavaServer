package Server;

import static spark.Spark.*;

public class Runner {

    public static void main(String[] args) {

        //Startar servern på port 5000.
        port(5000);

        //Skapar metoden get för URL/ledamoter, returnerar en json-sträng med namn, bild och parti.
        get("/ledamoter", (req, res) -> {
            return Importer.importLedamoter();
        });
    }
}

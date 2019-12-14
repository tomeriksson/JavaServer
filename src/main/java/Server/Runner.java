package Server;

import static spark.Spark.*;

public class Runner {

    public static void main(String[] args) {
        port(5000);

        get("/ledamoter", (req, res) -> {
            return Importer.importLedamoter();
        });
    }
}

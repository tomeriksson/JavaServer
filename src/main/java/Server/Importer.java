package Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

//import javax.json.*;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Importer {

	private static String urlStr;

	private static ConfigurationBuilder cb;
	private static TwitterFactory tf;

	private static QueryResult result;

	/*
	 * Collects a JSON file from a URL and returns it as a JSON object.
	 */
	private static javax.json.JsonObject urlToJson(URL url) throws IOException {
		javax.json.JsonObject obj;
		try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
			obj = rdr.readObject();
		}
		return obj;
	}

	/*
	 * Collects politicians from the Swedish Parliament, filters their name, party
	 * and link to image.
	 */
	public static String importLedamoter() throws IOException {

		urlStr = "http://data.riksdagen.se/personlista/?iid=&fnamn=&enamn=&f_ar=&kn=&parti=&valkrets=&rdlstatus=&org=&utformat=json&sort=parti&sortorder=asc&termlista=";
		URL url = new URL(urlStr);
		javax.json.JsonObject obj = Importer.urlToJson(url);
		javax.json.JsonObject personlista = obj.getJsonObject("personlista");
		JsonArray pArr = personlista.getJsonArray("person");
		JsonObject inner;
		com.google.gson.JsonArray outer = new com.google.gson.JsonArray();
		JsonObject shell = new JsonObject();
		String namn, parti, bild, twitterTag, valkrets;
		for (int i = 0; i < pArr.size(); i++) {
			inner = new JsonObject();
			namn = pArr.get(i).asJsonObject().getString("tilltalsnamn") + " "
					+ pArr.get(i).asJsonObject().getString("efternamn");
			parti = pArr.get(i).asJsonObject().getString("parti");
			bild = pArr.get(i).asJsonObject().getString("bild_url_80");

			// Add Twitter-tag

			valkrets = pArr.get(i).asJsonObject().getString("valkrets");

			inner.addProperty("namn", namn);
			inner.addProperty("parti", parti);
			inner.addProperty("bild", bild);
			inner.addProperty("valkrets", valkrets);
			outer.add(inner);
		}

		shell.add("ledamoter", outer);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String pretty = gson.toJson(shell);
		return pretty;

	}

	/*
	 * Retrieves tweets from a specified politician. Uses Twitter4j library to
	 * integrate our application with the Twitter services.
	 */
	public static String getTweets(String keyWords) throws IOException, TwitterException {
		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("5BX8LnhM6Xuc4XIl8zTtQtbZO")
				.setOAuthConsumerSecret("WYlyvmUZOJr4RC4HK3OQnPGEncPk4kCZUcXhEZkEVsqmzLl1MC")
				.setOAuthAccessToken("1206462660885262336-FsDtNrFoR1cWrS0ueVw2WuseJx8BtK")
				.setOAuthAccessTokenSecret("OFFipEoXAinyP6oDzAYhpEPMenK79VEyl6VgF6L37KeKc");

		tf = new TwitterFactory(cb.build());
		twitter4j.Twitter twitter = tf.getInstance();

		Query query = new Query(keyWords);
		result = twitter.search(query);

		JsonObject shell = new JsonObject();
		com.google.gson.JsonArray outer = new com.google.gson.JsonArray();
		String userName, postedAt, tweetText, userLocation, profileImageURL;

		for (Status status : result.getTweets()) {
			// Build JSON File
			JsonObject inner = new JsonObject();

			userName = status.getUser().getScreenName();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
			postedAt = dateFormat.format(status.getCreatedAt());
			tweetText = status.getText();
			userLocation = status.getUser().getLocation();
			profileImageURL = status.getUser().get400x400ProfileImageURL();

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

	public static void main(String[] args) {

		try {
//			System.out.println(Importer.importLedamoter());
			System.out.println(getTweets("@hanifbali"));
		} catch (IOException | TwitterException e) {
			e.printStackTrace();
		}

	}
}

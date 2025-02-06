import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MongoDBConnection {
    public static void main(String[] args) {
        String connectionString = "mongodb://localhost:27017";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("mongoDB");
            System.out.println("Connesso al database: " + database.getName());

            MongoCollection<Document> collection = database.getCollection("myCollection");
            List<Document> movies = getData();
            collection.insertMany(movies);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Document> getData() {
        List<Document> movies = new ArrayList<>();

        try (FileReader reader = new FileReader("Movies.json")) {

            JSONTokener tokener = new JSONTokener(reader);
            JSONArray jsonArray = new JSONArray(tokener);


            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String imdbId = jsonObject.optString("ImdbId", "");
                String id = jsonObject.optString("_id", "");
                String name = jsonObject.optString("name", "");
                String posterUrl = jsonObject.optString("poster_url", "");
                String year = jsonObject.optString("year", "");
                String certificate = jsonObject.optString("certificate", "");
                String runtime = jsonObject.optString("runtime", "");
                String ratingValue = jsonObject.optString("ratingValue", "");
                String summaryText = jsonObject.optString("summary_text", "");
                String ratingCount = jsonObject.optString("ratingCount", "");

                JSONArray genreArray = jsonObject.optJSONArray("genre");
                List<String> genre = new ArrayList<>();
                if (genreArray != null) {
                    for (int j = 0; j < genreArray.length(); j++) {
                        genre.add(genreArray.optString(j, ""));
                    }
                }

                JSONObject directorObject = jsonObject.optJSONObject("director");
                Person director = null;
                if (directorObject != null) {
                    String directorName = directorObject.optString("name", "");
                    String directorNameId = directorObject.optString("name_id", "");
                    director = new Person(directorName, directorNameId);
                }

                JSONArray castArray = jsonObject.getJSONArray("cast");
                List<Person> cast = new ArrayList<>();
                for (int j = 0; j < castArray.length(); j++) {
                    JSONObject personObject = castArray.getJSONObject(j);
                    String personName = personObject.optString("name", "");
                    String personNameId = personObject.optString("name_id", "");
                    Person person = new Person(personName, personNameId);
                    cast.add(person);
                }

                Film movie = new Film(imdbId, id, name, posterUrl, year, certificate, runtime,
                        genre, ratingValue, summaryText, ratingCount, director, cast);
                movies.add(movie.toDocument());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movies;
    }
}

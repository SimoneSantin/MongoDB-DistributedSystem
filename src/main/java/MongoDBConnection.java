import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
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
        long startTime = 0;
        int docs = 0;
        String connectionString = "mongodb://localhost:27017";
        ClientSession session = null;

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("mongoDB");
            MongoCollection<Document> collection = database.getCollection("collection");

            // ------ STEP 1: Caricamento e conteggio dati
            collection.drop();
            List<Document> movies = getData();
            startTime = System.currentTimeMillis();
            collection.insertMany(movies);
            docs = (int) collection.countDocuments(Filters.eq("year", "1914"));

            // ------ STEP 2: Transazione multi-documento
            Document movie1 = collection.find(Filters.eq("name", "Elskovsbarnet")).first();
            Document movie2 = collection.find(Filters.eq("name", "Jack's First Major")).first();
            System.out.println("Film prima di essere aggiornati: \n" + movie1 + movie2);
            session = mongoClient.startSession();
            session.startTransaction(TransactionOptions.builder()
                                                       .writeConcern(WriteConcern.ACKNOWLEDGED )
                                                       .build());

            collection.updateOne(session, Filters.eq("name", "Elskovsbarnet"),
                    new Document("$set", new Document("ratingValue", "4.0")));
            Thread.sleep(10000);
            collection.updateOne(session, Filters.eq("name", "Jack's First Major"),
                    new Document("$set", new Document("runtime", "35 min")));

            session.commitTransaction();
            System.out.println("Transazione completata con successo.");
            Document updatedMovie1 = collection.find(Filters.eq("name", "Elskovsbarnet")).first();
            Document updatedMovie2 = collection.find(Filters.eq("name", "Jack's First Major")).first();
            System.out.println("Film dopo essere aggiornati: \n" + updatedMovie1 + updatedMovie2);

        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) {
                session.abortTransaction();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("\n Trovati " +docs+ " documenti, in " + duration + " millisecondi");
    }


    public static List<Document> getData() {
        List<Document> movies = new ArrayList<>();

        try (FileReader reader = new FileReader("sample.json")) {

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
                JSONArray castArray = jsonObject.optJSONArray("cast");
                List<Person> cast = new ArrayList<>();
                if (castArray != null) {
                    castArray = jsonObject.getJSONArray("cast");
                    for (int j = 0; j < castArray.length(); j++) {
                        JSONObject personObject = castArray.getJSONObject(j);
                        String personName = personObject.optString("name", "");
                        String personNameId = personObject.optString("name_id", "");
                        Person person = new Person(personName, personNameId);
                        cast.add(person);
                    }
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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
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
import static com.mongodb.client.model.Updates.set;


public class MongoDBConnection {

    public static void main(String[] args) {
        long startTime = 0;
        int docs = 0;
        String connectionString = "mongodb://34.154.253.247:27017";
    
        ThreadLocal<ClientSession> sessionThreadLocal = new ThreadLocal<>();

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("mongoDB");
            MongoCollection<Document> collection = database.getCollection("collection");
            System.out.println("Connesso");
            // ------ STEP 1: Caricamento e conteggio dati
            collection.drop();
            List<Document> movies = getData();
            startTime = System.currentTimeMillis();
            collection.insertMany(movies);
            docs = (int) collection.countDocuments(Filters.eq("ImdbId", "tt2421546"));
            System.out.println("trovati " + docs + " documenti con anno 2014");
            // ------ STEP 2: Politiche di isolamento
            //Loss updates
            /*Runnable userA = () -> {
                 try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    System.out.println("Prima della scrittura di A" + collection.find(session, Filters.eq("name", "Elskovsbarnet")).first());
                    Document doc = collection.find(session, Filters.eq("name", "Elskovsbarnet")).first();
                    int ratingValue = Integer.parseInt(doc.getString("ratingValue")) - 1;
                    collection.updateOne(session, Filters.eq("name", "Elskovsbarnet"), set("ratingValue", ratingValue));
                    session.commitTransaction();
                    System.out.println("A ha scritto: " + collection.find(session, Filters.eq("name", "Elskovsbarnet")).first());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable userB = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    System.out.println("Prima della scrittura di B" + collection.find(session, Filters.eq("name", "Elskovsbarnet")).first());
                    Document doc = collection.find(session, Filters.eq("name", "Elskovsbarnet")).first();
                    int ratingValue = Integer.parseInt(doc.getString("ratingValue")) - 2;
                    collection.updateOne(session, Filters.eq("name", "Elskovsbarnet"), set("ratingValue", ratingValue));
                    session.commitTransaction();
                    System.out.println("B ha scritto: " + collection.find(session, Filters.eq("name", "Elskovsbarnet")).first());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            
            Thread tA = new Thread(userA);
            Thread tB = new Thread(userB);

            tA.start();
            tB.start();

            tA.join();
            tB.join();

            Document finalDoc = collection.find(Filters.eq("name", "Elskovsbarnet")).first();
            System.out.println("Valore finale nel database: " + finalDoc);*/

            //Dirty reads
            /*Runnable writer = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("name", "Elskovsbarnet")).first();
                    int ratingValue = Integer.parseInt(doc.getString("ratingValue")) - 3;
                    collection.updateOne(session, Filters.eq("name", "Elskovsbarnet"), set("ratingValue", ratingValue));
                    System.out.println("Writer ha scritto: " + collection.find(session, Filters.eq("name", "Elskovsbarnet")).first());
                    Thread.sleep(5000);
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable reader = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("name", "Elskovsbarnet")).first();
                    System.out.println("Reader ha letto: " + doc);
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            
            Thread tA = new Thread(writer);
            Thread tB = new Thread(reader);

            tA.start();
            tB.start();

            tA.join();
            tB.join();*/

            //Non-repeatable reads
            Runnable writer = () -> {
               try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("name", "Elskovsbarnet")).first();
                    int ratingValue = Integer.parseInt(doc.getString("ratingValue")) - 3;
                    collection.updateOne(session, Filters.eq("name", "Elskovsbarnet"), set("ratingValue", ratingValue));
                    System.out.println("Writer ha scritto: " + collection.find(session, Filters.eq("name", "Elskovsbarnet")).first());
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable reader = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document docFirstRead = collection.find(session, Filters.eq("name", "Elskovsbarnet")).first();
                    System.out.println("Prima lettura: " + docFirstRead);
                    Thread.sleep(5000);
                    Document docSecondRead = collection.find(session, Filters.eq("name", "Elskovsbarnet")).first();
                    System.out.println("Seconda lettura: " + docSecondRead);
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            
            Thread tA = new Thread(writer);
            Thread tB = new Thread(reader);

            tA.start();
            tB.start();

            tA.join();
            tB.join();

            //Phantom reads
            /*Runnable userA = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    List<Document> firstRead = collection.find(session, Filters.gt("ratingValue", 8)).into(new ArrayList<>());
                    System.out.println("UserA: Primo risultato count = " + firstRead.size());
                    session.commitTransaction();
                     Thread.sleep(3000);
                    session.startTransaction();
                    List<Document> secondRead = collection.find(session, Filters.gt("ratingValue", 8)).into(new ArrayList<>());
                    System.out.println("UserA: Secondo risultato count = " + secondRead.size());
                    session.commitTransaction();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable userB = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document newDoc = new Document("name", "NuovoFilm")
                        .append("ratingValue", 9)
                        .append("ImdbId", "phantom123");

                    collection.insertOne(session, newDoc);
                    session.commitTransaction();
                    System.out.println("UserB: Inserimento completato");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Thread tA = new Thread(userA);
            Thread tB = new Thread(userB);

            tA.start();
            Thread.sleep(1000); 
            tB.start();

            tA.join();
            tB.join();*/



            /*Document movie1 = collection.find(Filters.eq("name", "Elskovsbarnet")).first();
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
            System.out.println("Film dopo essere aggiornati: \n" + updatedMovie1 + updatedMovie2);*/

        } catch (Exception e) {
            e.printStackTrace();
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.ClientSessionOptions;
import com.mongodb.ReadConcern;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import static com.mongodb.client.model.Updates.set;


public class MongoDBConnection {

    public static void main(String[] args) {
        long startTime = 0;
        int docs = 0;
        String connectionString = "mongodb://34.154.179.44:27017";
        

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("mongoDB");
            MongoCollection<Document> collection = database.getCollection("collection");
            System.out.println("Connesso");
            // ------ STEP 1: Caricamento e conteggio dati
            collection.drop();
            List<Document> songs = getData();
            startTime = System.currentTimeMillis();
            collection.insertMany(songs);
            /*docs = (int) collection.countDocuments(Filters.eq("artist", "Metallica"));
            System.out.println("trovati " + docs + " canzoni dei Metallica");*/

            // ------ STEP 2: Politiche di isolamento
            //Loss updates
            Runnable userA = () -> {
                Document doc = collection.find(Filters.eq("song", "Master of Puppets")).first();
                int popularity = doc.getInteger("Tempo") - 5;
                collection.updateOne(Filters.eq("song", "Master of Puppets"), set("Tempo", popularity));
                System.out.println("A ha scritto: " + popularity);
            };

            Runnable userB = () -> {
                Document doc = collection.find(Filters.eq("song", "Master of Puppets")).first();
                int popularity = doc.getInteger("Tempo") - 10;
                collection.updateOne(Filters.eq("song", "Master of Puppets"), set("Tempo", popularity));
                System.out.println("B ha scritto: " + popularity);
            };

                        
            Thread tA = new Thread(userA);
            Thread tB = new Thread(userB);

            tA.start();
            tB.start();

            tA.join();
            tB.join();

            Document finalDoc = collection.find(Filters.eq("song", "Master of Puppets")).first();
            System.out.println("Valore finale nel database: " + finalDoc);

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
                    Thread.sleep(1000);
                    session.abortTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable reader = () -> {
                    try (ClientSession session = mongoClient.startSession(
                        ClientSessionOptions.builder()
                            .defaultTransactionOptions(TransactionOptions.builder()
                                .readConcern(ReadConcern.LOCAL)
                                .build())
                            .build())) {
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
            Thread.sleep(1000);
            tB.start();

            tA.join();
            tB.join();*/

            //Non-repeatable reads
           /*  Runnable writer = () -> {
               try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("artist", "ABBA")).first();
                    int tempo = Integer.parseInt(doc.getString("Tempo")) - 15;
                    collection.updateOne(session, Filters.eq("artist", "ABBA"), set("Tempo", tempo));
                    System.out.println("Writer ha scritto: " + collection.find(session, Filters.eq("artist", "ABBA")).first());
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable reader = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document docFirstRead = collection.find(session, Filters.eq("artist", "ABBA")).first();
                    System.out.println("Prima lettura: " + docFirstRead);
                    Thread.sleep(5000);
                    Document docSecondRead = collection.find(session, Filters.eq("artist", "ABBA")).first();
                    System.out.println("Seconda lettura: " + docSecondRead);
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            
           /*  Runnable reader = () -> {
                try {
                    Document docFirstRead = collection.find(Filters.eq("name", "Elskovsbarnet")).first();
                    System.out.println("Prima lettura : " + docFirstRead);
                    Thread.sleep(2000);
                    Document docSecondRead = collection.find(Filters.eq("name", "Elskovsbarnet")).first();
                    System.out.println("Seconda lettura : " + docSecondRead);
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

            //Phantom reads

            /*Runnable userA = () -> {
                try {
                    List<Document> firstRead = collection.find(Filters.gt("ratingValue", 8)).into(new ArrayList<>());
                    System.out.println("UserA: Primo risultato count = " + firstRead.size());
                    Thread.sleep(5000); 
                    List<Document> secondRead = collection.find(Filters.gt("ratingValue", 8)).into(new ArrayList<>());
                    System.out.println("UserA: Secondo risultato count = " + secondRead.size());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };


            Runnable userA = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    List<Document> firstRead = collection.find(session, Filters.gt("ratingValue", 8)).into(new ArrayList<>());
                    System.out.println("UserA: Primo risultato count = " + firstRead.size());
                     Thread.sleep(3000);
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
        List<Document> documents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("spotify_dataset.json"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JSONObject jsonObject = new JSONObject(line);
                Document doc = Document.parse(jsonObject.toString());
                documents.add(doc);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return documents;
    }

}


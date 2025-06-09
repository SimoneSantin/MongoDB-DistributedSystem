import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.ClientSessionOptions;
import com.mongodb.ReadConcern;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import static com.mongodb.client.model.Updates.set;


public class MongoDBConnection {

    public static void main(String[] args) {
        long startTime = 0;
        int docs = 0;
        String connectionString = "mongodb://34.154.155.105:27017";
        

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("mongoDB");
            MongoCollection<Document> collection = database.getCollection("collection");
            System.out.println("Connesso");
            collection.drop();
            List<Document> songs = getData();
            startTime = System.currentTimeMillis();
            collection.insertMany(songs);
            docs = (int) collection.countDocuments(Filters.eq("artist", "Metallica"));
            System.out.println("trovati " + docs + " canzoni dei Metallica");
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("\n Trovati " +docs+ " documenti, in " + duration + " millisecondi");

            // ------ STEP 2: Politiche di isolamento

            //Loss updates 
            /*Runnable userA = () -> {
                 try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("song", "Master of Puppets")).first();
                    System.out.println("A legge: " + doc.getInteger("Tempo"));
                    int tempo = doc.getInteger("Tempo") - 5;
                    collection.updateOne(session, Filters.eq("song", "Master of Puppets"), set("Tempo", tempo));
                    session.commitTransaction();
                    System.out.println("A ha scritto: " + tempo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable userB = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("song", "Master of Puppets")).first();
                    System.out.println("B legge: " + doc.getInteger("Tempo"));
                    int tempo = doc.getInteger("Tempo") - 10;
                    collection.updateOne(session, Filters.eq("song", "Master of Puppets"), set("Tempo", tempo));
                    session.commitTransaction();
                    System.out.println("B ha scritto: " + tempo);
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

            Document finalDoc = collection.find(Filters.eq("song", "Master of Puppets")).first();
            System.out.println("Valore finale nel database: " + finalDoc);*/

            //Dirty reads
            /*Runnable writer = () -> {
                try (ClientSession session = mongoClient.startSession()) {
    	            session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("song", "Master of Puppets")).first();
                    System.out.println("A legge: " + doc.getInteger("Tempo"));
                    int tempo = doc.getInteger("Tempo") + 5;
                    collection.updateOne(session, Filters.eq("song", "Master of Puppets"), set("Tempo", tempo));
                    System.out.println("A ha scritto senza committare: " + tempo);
                    Thread.sleep(2000);  // Simula un ritardo per il lettore
                    session.abortTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable reader = () -> {
                
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find(session, Filters.eq("song", "Master of Puppets")).first();
                    System.out.println("B legge prima: " + doc.getInteger("Tempo"));
                    Thread.sleep(5000);
                    Document doc2 = collection.find(session, Filters.eq("song", "Master of Puppets")).first();
                    System.out.println("B legge dopo: " + doc2.getInteger("Tempo"));
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            
            Thread tA = new Thread(writer);
            Thread tB = new Thread(reader);

            tA.start();
            Thread.sleep(500);
            tB.start();

            tA.join();
            tB.join();*/

            //Non-repeatable reads
             /*Runnable writer = () -> {
               try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document doc = collection.find( Filters.eq("artist", "ABBA")).first();
                    int tempo = doc.getInteger("Tempo") - 15;
                    collection.updateOne( Filters.eq("artist", "ABBA"), set("Tempo", tempo));
                    Document doc2 = collection.find( Filters.eq("artist", "ABBA")).first();
                    System.out.println("Writer ha scritto: " + doc2.getInteger("Tempo"));
                    session.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable reader = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    Document docFirstRead = collection.find( Filters.eq("artist", "ABBA")).first();
                    System.out.println("Prima lettura: " + docFirstRead.getInteger("Tempo"));
                    Thread.sleep(5000);
                    Document docSecondRead = collection.find( Filters.eq("artist", "ABBA")).first();
                    System.out.println("Seconda lettura: " + docSecondRead.getInteger("Tempo"));
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

            //Phantom reads

            /*  Runnable userA = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();
                    List<Document> firstRead = collection.find(Filters.gt("artist", "ABBA")).into(new ArrayList<>());
                    System.out.println("UserA: Primo risultato count = " + firstRead.size());
                     Thread.sleep(3000);
                    List<Document> secondRead = collection.find( Filters.gt("artist", "ABBA")).into(new ArrayList<>());
                    System.out.println("UserA: Secondo risultato count = " + secondRead.size());
                    session.commitTransaction();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Runnable userB = () -> {
                try (ClientSession session = mongoClient.startSession()) {
                    session.startTransaction();

                    Document newDoc = new Document("artist", "ABBA")
                        .append("song", "example song")
                        .append("emotion", "joy")
                        .append("variance", 0.4714285714285714)
                        .append("Genre", "pop")
                        .append("Release Date", 1975)
                        .append("Key", "A# Maj")
                        .append("Tempo", 82)
                        .append("Loudness", -10.57)
                        .append("Explicit", "No")
                        .append("Popularity", 38)
                        .append("Energy", 47)
                        .append("Danceability", 66)
                        .append("Positiveness", 84)
                        .append("Speechiness", 6)
                        .append("Liveness", 7)
                        .append("Acousticness", 53)
                        .append("Instrumentalness", 0);

                    collection.insertOne( newDoc);
                    session.commitTransaction();
                    System.out.println("UserB: Inserimento completato");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            Thread tA = new Thread(userA);
            Thread.sleep(2000);
            Thread tB = new Thread(userB);

            tA.start();
            tB.start();

            tA.join();
            tB.join();*/

            try (ClientSession session = mongoClient.startSession()) {
                Document song1 = collection.find(Filters.eq("song", "Master of Puppets")).first();
                Document song2 = collection.find(Filters.eq("song", "King Nothing")).first();
                System.out.println("Canzoni prima di essere aggiornati: \n" + song1.getInteger("Tempo") +" "+ song2.getInteger("Tempo"));
                session.startTransaction();

                collection.updateOne(session, Filters.eq("song", "Master of Puppets"),
                        new Document("$set", new Document("Tempo", 115)));
                Thread.sleep(10000);
                collection.updateOne(session, Filters.eq("song", "King Nothing"),
                        new Document("$set", new Document("Tempo", 120)));

                session.commitTransaction();
                System.out.println("Transazione completata con successo.");
                Document updatedSong1 = collection.find(Filters.eq("song", "Master of Puppets")).first();
                Document updatedSong2 = collection.find(Filters.eq("song", "King Nothing")).first();
                System.out.println("Canzoni dopo essere aggiornati: \n" + updatedSong1.getInteger("Tempo") +" "+ updatedSong2.getInteger("Tempo"));
            } catch (Exception e) {
                e .printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<Document> getData() {
        int MAX_ROWS = 236988;
        int currentLine = 0;
        List<Document> documents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("spotify_dataset.json"))) {
            String line;
            while ((line = reader.readLine()) != null && currentLine < MAX_ROWS) {
                currentLine++;
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


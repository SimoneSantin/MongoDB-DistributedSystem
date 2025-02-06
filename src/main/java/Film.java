import java.util.List;
import org.bson.Document;
import java.util.ArrayList;
public class Film {
    private String ImdbId;
    private String _id;
    private String name;
    private String poster_url;
    private String year;
    private String certificate;
    private String runtime;
    private List<String> genre;
    private String ratingValue;
    private String summary_text;
    private String ratingCount;
    private Person director;
    private List<Person> cast;

    public Film(String imdbId, String id, String name, String posterUrl, String year, String certificate,
                 String runtime, List<String> genre, String ratingValue, String summaryText,
                 String ratingCount, Person director, List<Person> cast) {
        this.ImdbId = imdbId;
        this._id = id;
        this.name = name;
        this.poster_url = posterUrl;
        this.year = year;
        this.certificate = certificate;
        this.runtime = runtime;
        this.genre = genre;
        this.ratingValue = ratingValue;
        this.summary_text = summaryText;
        this.ratingCount = ratingCount;
        this.director = director;
        this.cast = cast;
    }

    @Override
    public String toString() {
        return "Film{" +
                "ImdbId='" + ImdbId + '\'' +
                ", _id='" + _id + '\'' +
                ", name='" + name + '\'' +
                ", poster_url='" + poster_url + '\'' +
                ", year='" + year + '\'' +
                ", certificate='" + certificate + '\'' +
                ", runtime='" + runtime + '\'' +
                ", genre=" + genre +
                ", ratingValue='" + ratingValue + '\'' +
                ", summary_text='" + summary_text + '\'' +
                ", ratingCount='" + ratingCount + '\'' +
                ", director=" + director +
                ", cast=" + cast +
                '}';
    }
    public Document toDocument() {
        Document document = new Document("ImdbId", this.ImdbId)
                .append("_id", this._id)
                .append("name", this.name)
                .append("poster_url", this.poster_url)
                .append("year", this.year)
                .append("certificate", this.certificate)
                .append("runtime", this.runtime)
                .append("genre", this.genre)
                .append("ratingValue", this.ratingValue)
                .append("summary_text", this.summary_text)
                .append("ratingCount", this.ratingCount);

        Document directorDocument = this.director != null ? this.director.toDocument() : null;
        document.append("director", directorDocument);

        List<Document> castDocuments = new ArrayList<>();
        for (Person person : this.cast) {
            castDocuments.add(person.toDocument());
        }
        document.append("cast", castDocuments);

        return document;
    }
}
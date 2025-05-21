import org.bson.Document;

public class Song {
    private String artist;
    private String song;
    private String emotion;
    private double variance;
    private String genre;
    private int releaseDate;
    private String key;
    private int tempo;
    private double loudness;
    private String explicit;
    private int popularity;
    private int energy;
    private int danceability;
    private int positiveness;
    private int speechiness;
    private int liveness;
    private int acousticness;
    private int instrumentalness;

    public Song(String artist, String song, String emotion, double variance, String genre,
                int releaseDate, String key, int tempo, double loudness, String explicit,
                int popularity, int energy, int danceability, int positiveness,
                int speechiness, int liveness, int acousticness, int instrumentalness) {
        this.artist = artist;
        this.song = song;
        this.emotion = emotion;
        this.variance = variance;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.key = key;
        this.tempo = tempo;
        this.loudness = loudness;
        this.explicit = explicit;
        this.popularity = popularity;
        this.energy = energy;
        this.danceability = danceability;
        this.positiveness = positiveness;
        this.speechiness = speechiness;
        this.liveness = liveness;
        this.acousticness = acousticness;
        this.instrumentalness = instrumentalness;
    }

    @Override
    public String toString() {
        return "Song{" +
                "artist='" + artist + '\'' +
                ", song='" + song + '\'' +
                ", emotion='" + emotion + '\'' +
                ", variance=" + variance +
                ", genre='" + genre + '\'' +
                ", releaseDate=" + releaseDate +
                ", key='" + key + '\'' +
                ", tempo=" + tempo +
                ", loudness=" + loudness +
                ", explicit='" + explicit + '\'' +
                ", popularity=" + popularity +
                ", energy=" + energy +
                ", danceability=" + danceability +
                ", positiveness=" + positiveness +
                ", speechiness=" + speechiness +
                ", liveness=" + liveness +
                ", acousticness=" + acousticness +
                ", instrumentalness=" + instrumentalness +
                '}';
    }

    public Document toDocument() {
        return new Document("artist", artist)
                .append("song", song)
                .append("emotion", emotion)
                .append("variance", variance)
                .append("Genre", genre)
                .append("Release Date", releaseDate)
                .append("Key", key)
                .append("Tempo", tempo)
                .append("Loudness", loudness)
                .append("Explicit", explicit)
                .append("Popularity", popularity)
                .append("Energy", energy)
                .append("Danceability", danceability)
                .append("Positiveness", positiveness)
                .append("Speechiness", speechiness)
                .append("Liveness", liveness)
                .append("Acousticness", acousticness)
                .append("Instrumentalness", instrumentalness);
    }
}

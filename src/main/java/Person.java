import org.bson.Document;

public class Person {
    private String name;
    private String name_id;

    public Person(String name, String nameId) {
        this.name = name;
        this.name_id = nameId;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", name_id='" + name_id + '\'' +
                '}';
    }

    public Document toDocument() {
        Document document = new Document("name", this.name)
                .append("name_id", this.name_id);

        return document;
    }
}
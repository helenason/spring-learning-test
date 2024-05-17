package cholog;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    @ManyToOne//(fetch = FetchType.EAGER)
    private Publisher publisher;
    @OneToMany(mappedBy = "book")
    private Set<BookAuthor> authors;

    public Book() {
    }

    public Book(String name, Publisher publisher) {
        this.name = name;
        this.publisher = publisher;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Set<BookAuthor> getAuthors() {
        return authors;
    }
}

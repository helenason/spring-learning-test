package cholog;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    @OneToMany(mappedBy = "publisher")//, fetch = FetchType.EAGER)
    private Set<Book> books;

    public Publisher(String name) {
        this.name = name;
        this.books = new HashSet<>();
    }

    public Publisher() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public Set<Book> getBooks() {
        return books;
    }
}

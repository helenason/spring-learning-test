package cholog;

import jakarta.persistence.*;

@Entity
public class BookAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne
    private Book book;
    @ManyToOne
    private Author author;

    public BookAuthor(Book book, Author author) {
        this.book = book;
        this.author = author;
    }

    public BookAuthor() {
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Author getAuthor() {
        return author;
    }
}

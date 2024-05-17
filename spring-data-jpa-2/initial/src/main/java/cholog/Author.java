package cholog;

import jakarta.persistence.*;

import java.util.Set;

@Entity
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @OneToOne
    private Person person;
    @OneToMany(mappedBy = "author")
    private Set<BookAuthor> books;


    public Author(Person person) {
        this.person = person;
    }

    public Author() {
    }

    public Long getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public Set<BookAuthor> getBooks() {
        return books;
    }
}

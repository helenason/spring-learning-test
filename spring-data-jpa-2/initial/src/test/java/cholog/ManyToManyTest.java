package cholog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ManyToManyTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private BookRepository bookRepository;

    @Test
    void intermediateEntity() {
        Person person = new Person("사람");
        entityManager.persist(person);

        Author author = new Author(person);
        entityManager.persist(author);

        Publisher publisher = new Publisher("출판사");
        entityManager.persist(publisher);

        Book book = new Book("책", publisher);
        entityManager.persist(book);

        BookAuthor bookAuthor = new BookAuthor(book, author);
        entityManager.persist(bookAuthor);

        entityManager.flush();
        entityManager.clear();

        Optional<Book> persistBook = bookRepository.findById(book.getId());
        assertThat(persistBook).isNotEmpty();
        assertThat(persistBook.get().getAuthors()).isNotEmpty();

        /*
        select
            a1_0.book_id,
            a1_0.id,
            a2_0.id,
            p1_0.id,
            p1_0.name
        from
            book_author a1_0
        left join
            author a2_0
                on a2_0.id=a1_0.author_id
        left join
            person p1_0
                on p1_0.id=a2_0.person_id
        where
            a1_0.book_id=?

        person 테이블까지 참조하는 이유는, Author 클래스의 Person 이 @OneToOne (EAGER)이기 때문
         */
    }
}

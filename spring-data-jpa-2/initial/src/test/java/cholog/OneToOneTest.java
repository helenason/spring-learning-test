package cholog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class OneToOneTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private PersonRepository personRepository;

    @Test
    void uniDirection() {
        // Author 클래스의 person 필드에만 @OneToOne 붙였을 때
        Person person = new Person("사람");
        entityManager.persist(person);

        Author author = new Author(person);
        entityManager.persist(author);

        entityManager.flush();
        entityManager.clear();

        Author persistAuthor = entityManager.find(Author.class, author.getId());
        assertThat(persistAuthor).isNotNull();
        assertThat(persistAuthor.getPerson()).isNotNull();

        /*
        select
            a1_0.id,
            p1_0.id,
            p1_0.name
        from
            author a1_0
        left join
            person p1_0
                on p1_0.id=a1_0.person_id
        where
            a1_0.id=?
         */
    }

    @Test
    void biDirection() {
        Person person = new Person("사람");
        entityManager.persist(person);

        Author author = new Author(person);
        entityManager.persist(author);

        entityManager.flush();
        entityManager.clear();

        Person persistPerson = entityManager.find(Person.class, person.getId());
        assertThat(persistPerson).isNotNull();
        assertThat(persistPerson.getAuthor()).isNotNull();

        // mappedBy는 flush 하는 시점에 매핑되는 듯.
        /* Author @OneToOne <-> Person @OneToOne(mappedBy = "person")
        select
            p1_0.id,
            a1_0.id,
            p1_0.name
        from
            person p1_0
        left join
            author a1_0
                on p1_0.id=a1_0.person_id
        where
            p1_0.id=?
         */

        // TODO: 왜 이렇게 두! 번!의 참조를 진행하나? (구구에게 여쭤보기!)
        // 상황) mappedBy 설정 X + 둘 다 EAGER
        // 가설1) p->a 행위와 a->p 행위에 따라 p->a->p 완성
        // 가설2) p 내 a 에 접근 -> a 내 p 에 접근 -> p 내 a 에 접근 -> ... => 그런데 이건 무한 참조 아닌가?
        /* Author @OneToOne <-> Person @OneToOne
        select
            p1_0.id,
            a1_0.id,
            p2_0.id,
            p2_0.author_id,
            p2_0.name,
            p1_0.name
        from
            person p1_0
        left join
            author a1_0
                on a1_0.id=p1_0.author_id
        left join
            person p2_0
                on p2_0.id=a1_0.person_id
        where
            p1_0.id=?
         */
    }

    @Test
    void findByIdForAuthor() {
        Person person = new Person("사람");
        entityManager.persist(person);

        Author author = new Author(person);
        entityManager.persist(author);

        entityManager.flush();
        entityManager.clear();

        Optional<Author> persistAuthor = authorRepository.findById(author.getId());
        assertThat(persistAuthor).isPresent();
        assertThat(persistAuthor.get().getPerson()).isNotNull();
        // author left join person
    }

    @Test
    void findByIdForPerson() {
        Person person = new Person("사람");
        entityManager.persist(person);

        Author author = new Author(person);
        entityManager.persist(author);

        entityManager.flush();
        entityManager.clear();

        Optional<Person> persistPerson = personRepository.findById(person.getId());
        assertThat(persistPerson).isPresent();
        assertThat(persistPerson.get().getAuthor()).isNotNull();
        // person left join author
        // @OneToOne 은 default fetchType 이 EAGER 이기 때문
    }
}

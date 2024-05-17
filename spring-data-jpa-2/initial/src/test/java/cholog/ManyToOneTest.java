package cholog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ManyToOneTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private PublisherRepository publisherRepository;

    @Test
    void uniDirection() {
        Publisher publisher = new Publisher("출판사");
        entityManager.persist(publisher);

        Book book = new Book("책", publisher);
        entityManager.persist(book);

        Book persistBook = entityManager.find(Book.class, book.getId());

        assertThat(persistBook).isNotNull();
        assertThat(persistBook.getPublisher()).isNotNull();

        // @ManyToOne 어노테이션을 통해 외래키 설정 쿼리가 추가됨
    }

    @Test
    void biDirection() {
        /*
            - [X] 단방향일 경우 -> JOIN 없이 Publisher 에만 접근 -> Book 접근 불가능
            - [O] mappedBy(추적 대상) 없는 경우 -> 양방향 관계가 아닌, 두 개의 단방향 관계 -> publisher_books 테이블 생성
            - [O] 올바른 mappedBy 설정 -> JOIN 없이 차례대로 접근
         */
        Publisher publisher = new Publisher("출판사");
        entityManager.persist(publisher);

        Book book = new Book("책", publisher);
        entityManager.persist(book);
        publisher.addBook(book);

        // Q: 없어도 동작하는데 왜 추가한 걸까?
        // 없어도 insert 문이 실행되는 게 아니라,
        // 엔티티의 ID 전략이 IDENTITY 인 경우 DB에 접근을 해야 id를 가져올 수 있기 때문에 insert 쿼리가 실행된 것이다.
        // SEQUENCE 전략을 사용할 경우 select 쿼리를 통해 id를 받아온다.
        entityManager.flush(); // DB에 쿼리 보내기
        entityManager.clear(); // 영속성 컨텍스트 비우기

        Publisher persistPublisher = entityManager.find(Publisher.class, publisher.getId());
        // persistence context 에 존재하는 경우 찾고, 존재하지 않는 경우 DB 접근
        assertThat(persistPublisher).isNotNull();
        assertThat(persistPublisher.getBooks()).hasSize(1);
    }

    @Test
    void findByIdForBook() {
        Publisher publisher = new Publisher("출판사");
        entityManager.persist(publisher);
        Publisher publisher2 = new Publisher("출판사2");
        entityManager.persist(publisher2);

        Book book = new Book("책", publisher);
        entityManager.persist(book);
        Book book2 = new Book("책2", publisher);
        entityManager.persist(book2);
        Book book3 = new Book("책3", publisher2);
        entityManager.persist(book3);
        Book book4 = new Book("책4", publisher2);
        entityManager.persist(book4);

        entityManager.flush();
        entityManager.clear();

        Optional<Book> persistBook = bookRepository.findById(book.getId());
        Optional<Book> persistBook3 = bookRepository.findById(book3.getId());
        assertThat(persistBook).isPresent();
        assertThat(persistBook.get().getPublisher()).isNotNull();

        /* [default] @ManyToOne EAGER && @OneToMany LAZY 경우 findById
        select
            b1_0.id,
            b1_0.name,
            p1_0.id,
            p1_0.name
        from
            book b1_0
        left join
            publisher p1_0
                on p1_0.id=b1_0.publisher_id
        where
            b1_0.id=?
         */

        /* @ManyToOne EAGER && @OneToMany EAGER 경우 findById
        select
            b1_0.id,
            b1_0.name,
            p1_0.id,
            p1_0.name
        from
            book b1_0
        left join
            publisher p1_0
                on p1_0.id=b1_0.publisher_id
        where
            b1_0.id=?
        ---
        select
            b1_0.publisher_id,
            b1_0.id,
            b1_0.name
        from
            book b1_0
        where
            b1_0.publisher_id=?
        ---
        Q: 두 방향 모두를 EAGER 로 설정하면 왜 불필요한 쿼리가 생길까?
        1. bookRepository 에서 book join publisher 로 publisher 정보까지 찾음
        2. Publisher 클래스에 들어갔는데 Set<Book>이 EAGER 타입!
            -> book 테이블에 publisherId로 book 들 정보 가져옴. book 여러개면 행이 여러개가 되겠지!
         */

        // => 결론: LAZY 사용하자 ^_^
    }

    @Test
    void findByIdForPublisher() {
        Publisher publisher = new Publisher("출판사");
        entityManager.persist(publisher);

        Book book = new Book("책", publisher);
        entityManager.persist(book);

        entityManager.flush();
        entityManager.clear();

        Optional<Publisher> persistPublisher = publisherRepository.findById(publisher.getId());
        assertThat(persistPublisher).isPresent();
        assertThat(persistPublisher.get().getBooks()).isNotNull();

        /* [default] Publisher 클래스의 books FetchType == LAZY
        => 지연 로딩. 필요할 때마다 조회 쿼리를 날린다.

        - find 할 때
        select
            p1_0.id,
            p1_0.name
        from
            publisher p1_0
        where
            p1_0.id=?

        - getBooks() 할 때
        select
            b1_0.publisher_id,
            b1_0.id,
            b1_0.name
        from
            book b1_0
        where
            b1_0.publisher_id=?

         */

        /* Publisher 클래스의 books FetchType == EAGER
        => 즉시 로딩. 참조되는 모든 테이블에 접근하여 데이터를 가져온다.

        select
            p1_0.id,
            b1_0.publisher_id,
            b1_0.id,
            b1_0.name,
            p1_0.name
        from
            publisher p1_0
        left join
            book b1_0
                on p1_0.id=b1_0.publisher_id
        where
            p1_0.id=?
         */
    }

    @Test
    void lazy_loading_when_findAll() {
        Publisher publisher = new Publisher("출판사");
        entityManager.persist(publisher);
        Publisher publisher2 = new Publisher("출판사2");
        entityManager.persist(publisher2);

        Book book = new Book("책", publisher);
        entityManager.persist(book);
        Book book2 = new Book("책2", publisher);
        entityManager.persist(book2);
        Book book3 = new Book("책3", publisher2);
        entityManager.persist(book3);
        Book book4 = new Book("책4", publisher2);
        entityManager.persist(book4);

        entityManager.flush();
        entityManager.clear();

        Iterable<Publisher> publisherIterable = publisherRepository.findAll();
        // publisher findAll

        Iterable<Book> bookIterable = bookRepository.findAll();
        // book findAll -> publisher findById 여러개
        // TODO: findAll() 메서드를 사용하면 ManyToOne 을 사용해도 지연 로딩을 사용한다고? - 감자에게 물어보기?
        // TODO: 내가 확인한 쿼리(findAll -> findById)가 지연 로딩인가? Join 안 하면 지연 로딩인 것임?

        // 두 동작 모두 실행 시(p.findAll -> b.findAll): publisher findAll -> book findAll
        // 두 동작 모두 실행 시(b.findAll -> p.findAll): book findAll -> publisher findById 여러개 -> publisher findAll
    }
}

package cholog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EntityManagerTest {

    @PersistenceContext // the container creates the EntityManager from the EntityManagerFactory
    private EntityManager entityManager; // interface

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("truncate table customer restart identity").executeUpdate();
    }

    /**
     * 비영속 -> 영속
     */
    @Test
    void persist() {
        Customer customer = new Customer("Jack", "Bauer");
        entityManager.persist(customer); // 비영속 -> 영속

        assertThat(entityManager.find(Customer.class, 1L)).isNotNull();
    }

    /**
     * 영속 -> DB
     */
    @Test
    void flush() {
        String sqlForSelectCustomer = "select * from customer where id = 1";

        Customer customer = new Customer("Jack", "Bauer");
        entityManager.persist(customer); // 1. persist
        customer.updateFirstName("Danial"); // 2. update

        Customer savedCustomer = selectCustomerBySql(sqlForSelectCustomer);
        assertThat(savedCustomer.getFirstName()).isEqualTo("Jack");

        entityManager.flush(); // 3. flush 영속 -> DB

        Customer updatedCustomer = selectCustomerBySql(sqlForSelectCustomer);
        assertThat(updatedCustomer.getFirstName()).isEqualTo("Danial");
        // 4. update 데이터가 반영된다.
    }

    private Customer selectCustomerBySql(String sqlForSelectCustomer) {
        return jdbcTemplate.query(sqlForSelectCustomer, rs -> {
            rs.next();
            return new Customer(
                    rs.getLong("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"));
        });
    }
}

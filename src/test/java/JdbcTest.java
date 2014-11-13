import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JdbcTest {

    @Rule
    public CaptureRule capture = new CaptureRule();

    private Connection conn;

    @Before
    public void setup() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

        String createPerson = "create table Person (" +
                "id bigint identity, " +
                "firstname varchar2(50) not null, " +
                "lastname varchar2(50) not null, " +
                "age int)";

        try (PreparedStatement ps = conn.prepareStatement(createPerson)) {
            ps.executeUpdate();
        }
    }

    private void insertPerson() throws SQLException {
        insertPerson("John", "Doe", 25);
    }

    private void insertPerson(String firstname, String lastname, int age) throws SQLException {
        capture.disable();
        try (PreparedStatement ps = conn.prepareStatement(person(firstname, lastname, age))) {
            ps.executeUpdate();
        }
        capture.enable();
    }

    private String person(String firstname, String lastname, int age) {
        return "insert into Person (firstname, lastname, age) " +
                "values ('" + firstname + "', '" + lastname + "', " + age + ")";
    }

    private String person() {
        return person("John", "Doe", 25);
    }

    @After
    public void tearDown() {
        try {
            conn.close();
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateStatement() throws IOException, SQLException {
        try (Statement statement = conn.createStatement()) {
            assertTrue(statement instanceof StatementWrapper);
        }
        try (Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            assertTrue(statement instanceof StatementWrapper);
        }
        try (Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT)) {
            assertTrue(statement instanceof StatementWrapper);
        }
    }

    @Test
    @CaptureRule.CompareTo("jdbc-insert.json")
    public void testInsert() throws IOException, SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute(person());
            statement.execute(person("Billy", "Idol", 20), Statement.RETURN_GENERATED_KEYS);
            statement.execute(person("Guy", "Ritchie", 30), new int[]{0});
            statement.execute(person("Madonna", "Mia", 40), new String[]{"id"});
        }
    }

    @Test
    @CaptureRule.CompareTo("jdbc-update.json")
    public void testUpdate() throws IOException, SQLException {
        insertPerson();

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("update Person set firstname = 'James' where id = 1");
            statement.executeUpdate("update Person set lastname = 'Bond' where firstname = 'James'", Statement.RETURN_GENERATED_KEYS);
            statement.executeUpdate("update Person set age = 30 where lastname = 'Bond'", new int[]{0});
            statement.executeUpdate("update Person set id = 10 where id = 10", new String[]{"id"});
        }
    }

    @Test
    @CaptureRule.CompareTo("jdbc-delete.json")
    public void testDelete() throws IOException, SQLException {
        insertPerson();
        insertPerson("John", "McLane", 30);
        insertPerson("Big", "Lebowski", 40);

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("delete from Person where id = 1");
            statement.executeUpdate("delete from Person where lastname = 'McLane'", Statement.RETURN_GENERATED_KEYS);
            statement.executeUpdate("delete from Person where firstname = 'Big'", new int[]{0});
            statement.executeUpdate("delete from Person", new String[]{"id"});
        }
    }

    @Test
    @CaptureRule.CompareTo("jdbc-select.json")
    public void testSelect() throws IOException, SQLException {
        insertPerson();
        insertPerson("Lenny", "Kravitz", 25);

        try (Statement statement = conn.createStatement()) {
            ResultSet rs = statement.executeQuery("select * from Person");
            extractResultSet(rs); // 2 results

            statement.execute("select * from Person where id = 1");
            extractResultSet(statement.getResultSet()); // 1 result

            statement.execute("select firstname from Person where lastname in ('Doe', 'Kravitz')", Statement.RETURN_GENERATED_KEYS);
            extractResultSet(statement.getResultSet()); // 2 results

            statement.execute("select age from Person where age > 30", new int[]{0});
            extractResultSet(statement.getResultSet()); // 0 results

            statement.execute("select p.id from Person p where p.id = 2", new String[]{"id"});
            extractResultSet(statement.getResultSet()); // 1 result
        }
    }

    private void extractResultSet(ResultSet rs) throws SQLException {
        while (rs != null && rs.next()) {
            // noop
        }
    }

}

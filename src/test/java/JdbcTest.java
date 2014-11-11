import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;

import static org.junit.Assert.fail;

public class JdbcTest {

    @Rule
    public CaptureRule capture = new CaptureRule();

    private Connection conn;

    private final String insertPerson = "insert into Person (firstname, lastname, age) " +
            "values ('John', 'Doe', '25');";

    @Before
    public void setup() throws SQLException {
        conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");

        String createPerson = "create table Person (" +
                "id bigint identity, " +
                "firstname varchar2(50) not null, " +
                "lastname varchar2(50) not null, " +
                "age int);";

        try (PreparedStatement ps = conn.prepareStatement(createPerson)) {
            ps.executeUpdate();
        }
    }

    private void insertPerson() throws SQLException {
        capture.disable();
        try (PreparedStatement ps = conn.prepareStatement(insertPerson)) {
            ps.executeUpdate();
        }
        capture.enable();
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
    @CaptureRule.CompareTo("jdbc-insert.json")
    public void testInsert() throws IOException, SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(insertPerson);
        }
    }

    @Test
    @CaptureRule.CompareTo("jdbc-update.json")
    public void testUpdate() throws IOException, SQLException {
        insertPerson();
        String updatePerson = "update Person set firstname = 'Thomas', age = 27 where id = 1";

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(updatePerson);
        }
    }

    @Test
    @CaptureRule.CompareTo("jdbc-select.json")
    public void testSelect() throws IOException, SQLException {
        insertPerson();
        String selectPerson = "select * from Person where id = 1;";

        try (Statement ps = conn.createStatement()) {
            ps.executeQuery(selectPerson);
        }
    }

}

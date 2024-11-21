import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Manages database connections for the PoisePMS application. */
public class DatabaseConnectionManager {
  private static final String DB_URL = "jdbc:mysql://localhost:3306/PoisePMS";
  private static final String USER = "otheruser";
  private static final String PASS = "swordfish";

  /**
   * Establishes a connection to the database.
   *
   * @return Connection object.
   * @throws SQLException If the connection fails.
   */
  public static Connection connect() throws SQLException {
    return DriverManager.getConnection(DB_URL, USER, PASS);
  }
}

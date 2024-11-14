import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * The PoisePMS class provides functionality for managing projects and 
 * associated people for the "Poised" firm. 
 * It includes features to add, update, finalize and view projects using JDBC.
 */
public class PoisePMS {

  private static final String DB_URL = "jdbc:mysql://localhost:3306/PoisePMS";
  private static final String USER = "root";
  private static final String PASS = "password";

  /**
   * Main method to display the menu and execute the program.
   *
   * @param args Command-line arguments (not used).
   */
  public static void main(String[] args) {
    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
      System.out.println("Connected to PoisePMS database.");
      Scanner scanner = new Scanner(System.in);

      int choice;
      do {
        System.out.println("\n1. View All Projects");
        System.out.println("2. Add New Project");
        System.out.println("3. Update Existing Project");
        System.out.println("4. Finalise Project");
        System.out.println("5. View Incomplete Projects");
        System.out.println("6. View Overdue Projects");
        System.out.println("7. Search Project");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
        choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
          case 1 -> viewAllProjects(conn);
          case 2 -> addNewProject(conn, scanner);
          case 3 -> updateProject(conn, scanner);
          case 4 -> finaliseProject(conn, scanner);
          case 5 -> viewIncompleteProjects(conn);
          case 6 -> viewOverdueProjects(conn);
          case 7 -> searchProject(conn, scanner);
          case 0 -> System.out.println("Exiting program.");
          default -> System.out.println("Invalid choice. Try again.");
        }
      } while (choice != 0);

      scanner.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Displays all projects in the database.
   *
   * @param conn The database connection.
   */
  public static void viewAllProjects(Connection conn) {
    String query = "SELECT * FROM Project";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        System.out.println("\nProject Number: " + rs.getString("project_number"));
        System.out.println("Project Name: " + rs.getString("name"));
        System.out.println("Building Type: " + rs.getString("building_type"));
        System.out.println("Deadline: " + rs.getDate("deadline"));
        System.out.println("Finalised: " + rs.getBoolean("finalised"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds a new project to the database.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void addNewProject(Connection conn, Scanner scanner) {
    try {
      System.out.print("Enter Project Number: ");
      String projectNumber = scanner.nextLine();
      System.out.print("Enter Building Type: ");
      String buildingType = scanner.nextLine();
      System.out.print("Enter Project Name (leave blank to auto-generate): ");
      String name = scanner.nextLine();
      System.out.print("Enter Address: ");
      String address = scanner.nextLine();
      System.out.print("Enter ERF Number: ");
      String erfNumber = scanner.nextLine();
      System.out.print("Enter Total Fee: ");
      double totalFee = scanner.nextDouble();
      System.out.print("Enter Amount Paid: ");
      double amountPaid = scanner.nextDouble();
      scanner.nextLine(); // Consume newline
      System.out.print("Enter Deadline (YYYY-MM-DD): ");
      String deadline = scanner.nextLine();

      // Select or create related people
      int customerId = selectOrCreatePerson(conn, scanner, "Customer");
      int architectId = selectOrCreatePerson(conn, scanner, "Architect");
      int contractorId = selectOrCreatePerson(conn, scanner, "Contractor");

      // Auto-generate project name if blank
      if (name.isEmpty()) {
        name = buildingType + " " + getLastName(conn, "Customer", customerId);
        System.out.println("Generated Project Name: " + name);
      }

      String insertQuery =
          """
          INSERT INTO Project (project_number, name, building_type, address, 
          erf_number, total_fee, amount_paid, deadline, finalised, customer_id, 
          architect_id, contractor_id)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?, ?, ?);
          """;

      try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
        pstmt.setString(1, projectNumber);
        pstmt.setString(2, name);
        pstmt.setString(3, buildingType);
        pstmt.setString(4, address);
        pstmt.setString(5, erfNumber);
        pstmt.setDouble(6, totalFee);
        pstmt.setDouble(7, amountPaid);
        pstmt.setDate(8, Date.valueOf(deadline));
        pstmt.setInt(9, customerId);
        pstmt.setInt(10, architectId);
        pstmt.setInt(11, contractorId);

        int rowsAffected = pstmt.executeUpdate();
        System.out.println(rowsAffected + " project(s) added successfully.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Updates details of an existing project.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void updateProject(Connection conn, Scanner scanner) {
    System.out.print("Enter Project Number to update: ");
    String projectNumber = scanner.nextLine();
    System.out.print("Enter new Amount Paid: ");
    double amountPaid = scanner.nextDouble();

    String updateQuery = "UPDATE Project SET amount_paid = ? WHERE project_number = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
      pstmt.setDouble(1, amountPaid);
      pstmt.setString(2, projectNumber);

      int rowsAffected = pstmt.executeUpdate();
      System.out.println(rowsAffected + " project(s) updated successfully.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Finalises a project and sets the completion date.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void finaliseProject(Connection conn, Scanner scanner) {
    System.out.print("Enter Project Number to finalise: ");
    String projectNumber = scanner.nextLine();

    String finalizeQuery =
        """
        UPDATE Project SET finalised = TRUE, completion_date = ?
        WHERE project_number = ? AND finalised = FALSE;
        """;
    try (PreparedStatement pstmt = conn.prepareStatement(finalizeQuery)) {
      pstmt.setDate(1, Date.valueOf(LocalDate.now()));
      pstmt.setString(2, projectNumber);

      int rowsAffected = pstmt.executeUpdate();
      System.out.println(rowsAffected + " project(s) finalised successfully.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Displays incomplete projects.
   *
   * @param conn The database connection.
   */
  public static void viewIncompleteProjects(Connection conn) {
    String query = "SELECT * FROM Project WHERE finalised = FALSE";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        System.out.println(
            "Project Number: "
                + rs.getString("project_number")
                + ", Name: "
                + rs.getString("name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Displays overdue projects.
   *
   * @param conn The database connection.
   */
  public static void viewOverdueProjects(Connection conn) {
    String query = "SELECT * FROM Project WHERE finalised = FALSE AND " 
    + "deadline < CURDATE()";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        System.out.println(
            "Project Number: "
                + rs.getString("project_number")
                + ", Name: "
                + rs.getString("name")
                + ", Deadline: "
                + rs.getDate("deadline"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Searches for a project by number or name.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void searchProject(Connection conn, Scanner scanner) {
    System.out.print("Enter Project Number or Name to search: ");
    String search = scanner.nextLine();

    String query = "SELECT * FROM Project WHERE project_number = ? OR name = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setString(1, search);
      pstmt.setString(2, search);

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        System.out.println("\nProject Number: " + rs.getString("project_number"));
        System.out.println("Project Name: " + rs.getString("name"));
        System.out.println("Building Type: " + rs.getString("building_type"));
        System.out.println("Deadline: " + rs.getDate("deadline"));
        System.out.println("Finalised: " + rs.getBoolean("finalised"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Prompts the user to select or create a person in the database.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   * @param tableName The table name to operate on.
   * @return The ID of the selected or created person.
   */
  private static int selectOrCreatePerson(Connection conn, Scanner scanner, String tableName) {
    try {
      System.out.println("\nAvailable " + tableName + "s:");
      String query = "SELECT * FROM " + tableName;
      try (Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery(query)) {
        while (rs.next()) {
          System.out.println(
              rs.getInt(1) + ": " + rs.getString("first_name") + " " + "" 
              + " " + rs.getString("last_name"));
        }
      }

      System.out.print("\nEnter " + tableName + " ID or type 'new' to create: ");
      String input = scanner.nextLine();
      if (input.equalsIgnoreCase("new")) {
        return createPerson(conn, scanner, tableName);
      } else {
        return Integer.parseInt(input);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * Creates a new person in the database.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   * @param tableName The table name to operate on.
   * @return The ID of the created person.
   */
  private static int createPerson(Connection conn, Scanner scanner, String tableName) {
    try {
      System.out.print("Enter First Name: ");
      String firstName = scanner.nextLine();
      System.out.print("Enter Last Name: ");
      String lastName = scanner.nextLine();
      System.out.print("Enter Email: ");
      String email = scanner.nextLine();
      System.out.print("Enter Phone: ");
      String phone = scanner.nextLine();
      System.out.print("Enter Address: ");
      String address = scanner.nextLine();

      String insertQuery =
          "INSERT INTO "
              + tableName
              + " (first_name, last_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
      try (PreparedStatement pstmt =
          conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
        pstmt.setString(1, firstName);
        pstmt.setString(2, lastName);
        pstmt.setString(3, email);
        pstmt.setString(4, phone);
        pstmt.setString(5, address);

        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
          ResultSet rs = pstmt.getGeneratedKeys();
          if (rs.next()) {
            int personId = rs.getInt(1);
            System.out.println(tableName + " created successfully with ID: " + personId);
            return personId;
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  /**
   * Retrieves the last name of a person based on their ID.
   *
   * @param conn The database connection.
   * @param tableName The table name to operate on.
   * @param id The ID of the person.
   * @return The last name of the person.
   */
  private static String getLastName(Connection conn, String tableName, int id) {
    try {
      String query =
          "SELECT last_name FROM " + tableName + " WHERE " + " "
          + tableName.toLowerCase() + "_id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
          return rs.getString("last_name");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return "";
  }
}

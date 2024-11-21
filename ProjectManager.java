import java.sql.*;
import java.util.Scanner;

/** Manages project-related operations for the PoisePMS application. */
public class ProjectManager {

  /**
   * Displays all projects in the database, including associated project managers.
   *
   * @param conn The database connection.
   */
  public static void viewAllProjects(Connection conn) {
    String query =
        """
        SELECT
            p.id AS project_id,
            p.name AS project_name,
            p.building_type,
            p.deadline,
            p.finalised,
            pm.first_name AS manager_first_name,
            pm.last_name AS manager_last_name,
            pm.email AS manager_email
        FROM
            Project p
        JOIN
            ProjectManager pm ON p.project_manager_id = pm.id;
        """;

    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        System.out.println("\nProject ID: " + rs.getInt("project_id"));
        System.out.println("Project Name: " + rs.getString("project_name"));
        System.out.println("Building Type: " + rs.getString("building_type"));
        System.out.println("Deadline: " + rs.getDate("deadline"));
        System.out.println("Finalised: " + rs.getBoolean("finalised"));
        System.out.println(
            "Project Manager: "
                + rs.getString("manager_first_name")
                + " "
                + rs.getString("manager_last_name"));
        System.out.println("Manager Email: " + rs.getString("manager_email"));
      }
    } catch (SQLException e) {
      System.out.println("Error fetching projects: " + e.getMessage());
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
      String buildingType = InputValidator.getValidString("Enter Building Type (e.g., House)");
      System.out.print("Enter Project Name (leave blank to auto-generate): ");
      String name = scanner.nextLine();

      String address = InputValidator.getValidString("Enter Address");
      String erfNumber = InputValidator.getValidString("Enter ERF Number");
      double totalFee = InputValidator.getValidDouble("Enter Total Fee");
      double amountPaid = InputValidator.getValidDouble("Enter Amount Paid");
      String deadline = InputValidator.getValidDate("Enter Deadline");

      // Select or create related entities
      int customerId = selectOrCreatePerson(conn, scanner, "Customer");
      int architectId = selectOrCreatePerson(conn, scanner, "Architect");
      int projectManagerId = selectOrCreatePerson(conn, scanner, "ProjectManager");

      System.out.print("Enter Structural Engineer's Name: ");
      String structuralEngineer = scanner.nextLine();

      // Auto-generate project name if blank
      if (name.isEmpty()) {
        name = buildingType + " " + getLastName(conn, "Customer", customerId);
        System.out.println("Generated Project Name: " + name);
      }

      // Insert new project
      String insertQuery =
          """
INSERT INTO Project (name, building_type, address, erf_number, total_fee, amount_paid, deadline, finalised, customer_id, architect_id, project_manager_id, structural_engineer)
VALUES (?, ?, ?, ?, ?, ?, ?, FALSE, ?, ?, ?, ?);
""";

      try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
        pstmt.setString(1, name);
        pstmt.setString(2, buildingType);
        pstmt.setString(3, address);
        pstmt.setString(4, erfNumber);
        pstmt.setDouble(5, totalFee);
        pstmt.setDouble(6, amountPaid);
        pstmt.setDate(7, Date.valueOf(deadline));
        pstmt.setInt(8, customerId);
        pstmt.setInt(9, architectId);
        pstmt.setInt(10, projectManagerId);
        pstmt.setString(11, structuralEngineer);

        int rowsAffected = pstmt.executeUpdate();
        System.out.println(rowsAffected + " project(s) added successfully.");
      }
    } catch (SQLException e) {
      System.out.println("Error adding project: " + e.getMessage());
    }
  }

  /**
   * Prompts the user to select or create a related person (e.g., customer, architect).
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   * @param tableName The table name.
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
              rs.getInt("id")
                  + ": "
                  + rs.getString("first_name")
                  + " "
                  + rs.getString("last_name"));
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
      System.out.println("Error fetching " + tableName + " details: " + e.getMessage());
      return -1;
    }
  }

  /**
   * Creates a new person in the database.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   * @param tableName The table name.
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
      System.out.println("Error creating " + tableName + ": " + e.getMessage());
    }
    return -1;
  }

  /**
   * Retrieves the last name of a person based on their ID.
   *
   * @param conn The database connection.
   * @param tableName The table name.
   * @param id The ID of the person.
   * @return The last name of the person.
   */
  private static String getLastName(Connection conn, String tableName, int id) {
    String query = "SELECT last_name FROM " + tableName + " WHERE id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
      pstmt.setInt(1, id);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return rs.getString("last_name");
      }
    } catch (SQLException e) {
      System.out.println("Error fetching last name: " + e.getMessage());
    }
    return "";
  }

  /**
   * Updates an existing project.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void updateProject(Connection conn, Scanner scanner) {
    try {
      System.out.print("Enter Project ID to update: ");
      int projectId = scanner.nextInt();
      scanner.nextLine(); // Consume newline

      String name =
          InputValidator.getValidString("Enter New Name (or leave blank to keep current)", true);
      String deadline =
          InputValidator.getValidDate("Enter New Deadline (or leave blank to keep current)", true);

      String updateQuery =
          """
          UPDATE Project
          SET name = COALESCE(NULLIF(?, ''), name),
              deadline = COALESCE(NULLIF(?, ''), deadline)
          WHERE id = ?;
          """;

      try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
        pstmt.setString(1, name.isEmpty() ? null : name);
        pstmt.setString(2, deadline.isEmpty() ? null : deadline);
        pstmt.setInt(3, projectId);

        int rowsAffected = pstmt.executeUpdate();
        if (rowsAffected > 0) {
          System.out.println("Project updated successfully.");
        } else {
          System.out.println("No project found with the given ID.");
        }
      }
    } catch (SQLException e) {
      System.out.println("Error updating project: " + e.getMessage());
    }
  }

  /**
   * Deletes a project from the database.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void deleteProject(Connection conn, Scanner scanner) {
    try {
      System.out.print("Enter Project ID to delete: ");
      int projectId = scanner.nextInt();
      scanner.nextLine();

      String deleteQuery = "DELETE FROM Project WHERE id = ?";
      try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
        pstmt.setInt(1, projectId);

        int rowsAffected = pstmt.executeUpdate();
        System.out.println(rowsAffected + " project(s) deleted successfully.");
      }
    } catch (SQLException e) {
      System.out.println("Error deleting project: " + e.getMessage());
    }
  }

  /**
   * Marks a project as finalized and sets a completion date.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void finaliseProject(Connection conn, Scanner scanner) {
    try {
      System.out.print("Enter Project ID to finalize: ");
      int projectId = scanner.nextInt();
      scanner.nextLine();

      String completionDate = InputValidator.getValidDate("Enter Completion Date (YYYY-MM-DD)");

      String updateQuery =
          """
          UPDATE Project SET finalised = TRUE, completion_date = ? WHERE id = ?;
          """;

      try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
        pstmt.setDate(1, Date.valueOf(completionDate));
        pstmt.setInt(2, projectId);

        int rowsAffected = pstmt.executeUpdate();
        System.out.println(rowsAffected + " project(s) finalized successfully.");
      }
    } catch (SQLException e) {
      System.out.println("Error finalizing project: " + e.getMessage());
    }
  }

  /**
   * Displays all incomplete projects.
   *
   * @param conn The database connection.
   */
  public static void viewIncompleteProjects(Connection conn) {
    String query = "SELECT * FROM Project WHERE finalised = FALSE";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        System.out.println("\nProject ID: " + rs.getInt("id"));
        System.out.println("Project Name: " + rs.getString("name"));
        System.out.println("Deadline: " + rs.getDate("deadline"));
      }
    } catch (SQLException e) {
      System.out.println("Error fetching incomplete projects: " + e.getMessage());
    }
  }

  /**
   * Displays all overdue projects.
   *
   * @param conn The database connection.
   */
  public static void viewOverdueProjects(Connection conn) {
    String query = "SELECT * FROM Project WHERE deadline < CURDATE() AND finalised = FALSE";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        System.out.println("\nProject ID: " + rs.getInt("id"));
        System.out.println("Project Name: " + rs.getString("name"));
        System.out.println("Deadline: " + rs.getDate("deadline"));
      }
    } catch (SQLException e) {
      System.out.println("Error fetching overdue projects: " + e.getMessage());
    }
  }

  /**
   * Searches for a project by ID or name.
   *
   * @param conn The database connection.
   * @param scanner The scanner to read user input.
   */
  public static void searchProject(Connection conn, Scanner scanner) {
    try {
      System.out.print("Enter Project ID or Name to search: ");
      String input = scanner.nextLine();

      String query = "SELECT * FROM Project WHERE id = ? OR name LIKE ?";
      try (PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setString(1, input);
        pstmt.setString(2, "%" + input + "%");

        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            System.out.println("\nProject ID: " + rs.getInt("id"));
            System.out.println("Project Name: " + rs.getString("name"));
            System.out.println("Building Type: " + rs.getString("building_type"));
            System.out.println("Deadline: " + rs.getDate("deadline"));
            System.out.println("Finalised: " + rs.getBoolean("finalised"));
          } else {
            System.out.println("No project found with the given ID or name.");
          }
        }
      }
    } catch (SQLException e) {
      System.out.println("Error searching for project: " + e.getMessage());
    }
  }
}

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class PoisePMS {

  private static final String DB_URL = "jdbc:mysql://localhost:3306/PoisePMS";
  private static final String USER = "otheruser"; // MySQL username
  private static final String PASS = "swordfish"; // MySQL password

  public static void main(String[] args) {
    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
      System.out.println("Connected to PoisePMS database.");
      Scanner scanner = new Scanner(System.in);

      int choice;
      do {
        System.out.println("\n1. View All Projects");
        System.out.println("2. Add New Project");
        System.out.println("3. Update Existing Project");
        System.out.println("4. Delete Project");
        System.out.println("5. Finalise Project");
        System.out.println("6. View Incomplete Projects");
        System.out.println("7. View Overdue Projects");
        System.out.println("8. Search Project by Number or Name");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
        choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
          case 1 -> viewAllProjects(conn);
          case 2 -> addNewProject(conn, scanner);
          case 3 -> updateProject(conn, scanner);
          case 4 -> deleteProject(conn, scanner);
          case 5 -> finaliseProject(conn, scanner);
          case 6 -> viewIncompleteProjects(conn);
          case 7 -> viewOverdueProjects(conn);
          case 8 -> searchProject(conn, scanner);
          case 0 -> System.out.println("Exiting program.");
          default -> System.out.println("Invalid choice. Try again.");
        }
      } while (choice != 0);

      scanner.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Method to view all projects
  private static void viewAllProjects(Connection conn) {
    String query = "SELECT * FROM Project";
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        System.out.println("\nProject Number: " + rs.getString("project_number"));
        System.out.println("Project Name: " + rs.getString("name"));
        System.out.println("Building Type: " + rs.getString("building_type"));
        System.out.println("Address: " + rs.getString("address"));
        System.out.println("Total Fee: " + rs.getDouble("total_fee"));
        System.out.println("Amount Paid: " + rs.getDouble("amount_paid"));
        System.out.println("Deadline: " + rs.getDate("deadline"));
        System.out.println("Finalised: " + rs.getBoolean("finalised"));
        System.out.println("Completion Date: " + rs.getDate("completion_date"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Method to add a new project
  private static void addNewProject(Connection conn, Scanner scanner) {
    try {
      System.out.print("Enter Project Number: ");
      String projectNumber = scanner.nextLine();
      System.out.print("Enter Project Name: ");
      String name = scanner.nextLine();
      System.out.print("Enter Building Type: ");
      String buildingType = scanner.nextLine();
      System.out.print("Enter Address: ");
      String address = scanner.nextLine();
      System.out.print("Enter ERF Number: ");
      String erfNumber = scanner.nextLine();
      System.out.print("Enter Total Fee: ");
      double totalFee = scanner.nextDouble();
      System.out.print("Enter Amount Paid: ");
      double amountPaid = scanner.nextDouble();
      scanner.nextLine(); // consume newline
      System.out.print("Enter Deadline (YYYY-MM-DD): ");
      String deadline = scanner.nextLine();

      String insertQuery =
          """
INSERT INTO Project (project_number, name, building_type, address, erf_number, total_fee, amount_paid, deadline, finalised)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE);
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

        int rowsAffected = pstmt.executeUpdate();
        System.out.println(rowsAffected + " project(s) added successfully.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Method to update an existing project
  private static void updateProject(Connection conn, Scanner scanner) {
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

  // Method to delete a project
  private static void deleteProject(Connection conn, Scanner scanner) {
    System.out.print("Enter Project Number to delete: ");
    String projectNumber = scanner.nextLine();

    String deleteQuery = "DELETE FROM Project WHERE project_number = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
      pstmt.setString(1, projectNumber);

      int rowsAffected = pstmt.executeUpdate();
      System.out.println(rowsAffected + " project(s) deleted successfully.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Method to finalize a project
  private static void finaliseProject(Connection conn, Scanner scanner) {
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

  // Method to view all incomplete projects
  private static void viewIncompleteProjects(Connection conn) {
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

  // Method to view all overdue projects
  private static void viewOverdueProjects(Connection conn) {
    String query = "SELECT * FROM Project WHERE finalised = FALSE AND deadline < CURDATE()";
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

  // Method to search for a project by number or name
  private static void searchProject(Connection conn, Scanner scanner) {
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
}

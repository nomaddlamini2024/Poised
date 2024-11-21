import java.sql.Connection;
import java.util.Scanner;

/** Main menu for the PoisePMS application. */
public class MainMenu {
  public static void main(String[] args) {
    try (Connection conn = DatabaseConnectionManager.connect();
        Scanner scanner = new Scanner(System.in)) {

      int choice;
      do {
        System.out.println("\n=== PoisePMS Menu ===");
        System.out.println("1. View All Projects");
        System.out.println("2. Add New Project");
        System.out.println("3. Update Existing Project");
        System.out.println("4. Delete Project");
        System.out.println("5. Finalise Project");
        System.out.println("6. View Incomplete Projects");
        System.out.println("7. View Overdue Projects");
        System.out.println("8. Search Project by Number or Name");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
        choice = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        switch (choice) {
          case 1 -> ProjectManager.viewAllProjects(conn);
          case 2 -> ProjectManager.addNewProject(conn, scanner);
          case 3 -> ProjectManager.updateProject(conn, scanner);
          case 4 -> ProjectManager.deleteProject(conn, scanner);
          case 5 -> ProjectManager.finaliseProject(conn, scanner);
          case 6 -> ProjectManager.viewIncompleteProjects(conn);
          case 7 -> ProjectManager.viewOverdueProjects(conn);
          case 8 -> ProjectManager.searchProject(conn, scanner);
          case 0 -> System.out.println("Exiting application.");
          default -> System.out.println("Invalid choice. Please try again.");
        }
      } while (choice != 0);

    } catch (Exception e) {
      System.out.println("An unexpected error occurred: " + e.getMessage());
    }
  }
}

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Validates user inputs for the PoisePMS application. */
public class InputValidator {
  private static final Scanner scanner = new Scanner(System.in);

  /**
   * Reads a valid string input.
   *
   * @param prompt The prompt message.
   * @param allowBlank Whether to allow blank input.
   * @return A valid string (or blank if allowed).
   */
  public static String getValidString(String prompt, boolean allowBlank) {
    while (true) {
      System.out.print(prompt + ": ");
      String input = scanner.nextLine().trim();
      if (!input.isEmpty() || allowBlank) {
        return input;
      }
      System.out.println("Input cannot be blank. Please try again.");
    }
  }

  /**
   * Reads a valid string input.
   *
   * @param prompt The prompt message.
   * @return A valid string.
   */
  public static String getValidString(String prompt) {
    while (true) {
      System.out.print(prompt + ": ");
      String input = scanner.nextLine().trim();
      if (!input.isEmpty()) {
        return input;
      }
      System.out.println("Input cannot be blank. Please try again.");
    }
  }

  /**
   * Reads a valid decimal number.
   *
   * @param prompt The prompt message.
   * @return A valid decimal number.
   */
  public static double getValidDouble(String prompt) {
    while (true) {
      System.out.print(prompt + ": ");
      try {
        return Double.parseDouble(scanner.nextLine());
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a valid number (e.g., 150000.00).");
      }
    }
  }

  /**
   * Validates and returns a date in YYYY-MM-DD format.
   *
   * @param prompt The prompt message.
   * @return A valid date in YYYY-MM-DD format.
   */
  public static String getValidDate(String prompt) {
    while (true) {
      System.out.print(prompt + " (YYYY-MM-DD): ");
      String input = scanner.nextLine();
      try {
        LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
        return input;
      } catch (DateTimeParseException e) {
        System.out.println("Invalid date format. Please use YYYY-MM-DD.");
      }
    }
  }

  /**
   * Reads a valid date input. Optionally allows blank input.
   *
   * @param prompt The prompt message to display.
   * @param allowBlank Whether to allow blank input.
   * @return A valid date in the format YYYY-MM-DD (or an empty string if blank input is allowed and
   *     provided).
   */
  public static String getValidDate(String prompt, boolean allowBlank) {
    while (true) {
      System.out.print(prompt + ": ");
      String input = scanner.nextLine().trim();

      // Allow blank input if specified
      if (allowBlank && input.isEmpty()) {
        return "";
      }

      // Validate date format
      if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
        return input;
      }

      // Invalid input, show an error message
      System.out.println(
          "Invalid date format. Please enter in YYYY-MM-DD format or leave blank if allowed.");
    }
  }
}

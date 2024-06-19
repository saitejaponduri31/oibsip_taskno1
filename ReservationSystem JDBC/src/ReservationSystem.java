import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class ReservationSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/reservationsystem";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "new_password";

    private Connection connection;
    private Scanner scanner;
    private int userId;

    public ReservationSystem() {
        this.connection = getConnection();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        ReservationSystem system = new ReservationSystem();
        system.run();
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void run() {
        if (connection == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        if (authenticate()) {
            System.out.println("Login successful!");

            while (true) {
                System.out.println("1. Add Reservation");
                System.out.println("2. Cancel Reservation");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); 

                switch (choice) {
                    case 1:
                        addReservation();
                        break;
                    case 2:
                        cancelReservation();
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private boolean authenticate() {
        System.out.println("1. Login");
        System.out.println("2. Sign-up");
        System.out.print("Do you want to login or signup: ");
        int wish = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        if (wish == 1) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            String query = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (password.equals(storedPassword)) {
                        userId = rs.getInt("user_id");
                        return true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (wish == 2) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            String registerQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement registerStmt = connection.prepareStatement(registerQuery)) {
                registerStmt.setString(1, username);
                registerStmt.setString(2, password);
                int rowsAffected = registerStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("New user registered successfully!");
                    return authenticate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void addReservation() {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline character
        System.out.print("Enter train name: ");
        String trainName = scanner.nextLine();
        System.out.print("Enter class type (AC/GENERAL): ");
        String classType = scanner.nextLine();
        System.out.print("Enter journey date (YYYY-MM-DD): ");
        String journeyDate = scanner.nextLine();
        System.out.print("Enter from place: ");
        String fromPlace = scanner.nextLine();
        System.out.print("Enter to destination: ");
        String toDestination = scanner.nextLine();

        String pnr = generatePNR();

        String query = "INSERT INTO reservations (user_id, name, age, train_name, class_type, journey_date, from_place, to_destination, pnr) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setInt(3, age);
            stmt.setString(4, trainName);
            stmt.setString(5, classType);
            stmt.setString(6, journeyDate);
            stmt.setString(7, fromPlace);
            stmt.setString(8, toDestination);
            stmt.setString(9, pnr);
            stmt.executeUpdate();
            System.out.println("Reservation added successfully!");
            System.out.println("Your PNR number is: " + pnr);
            System.out.println("Don't forget to save your PNR number in case of any cancellations!!");
            System.out.println("Thank you for choosing Saiponduri Railways, we wish you a safe and happy journey :) ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cancelReservation() {
        System.out.print("Enter PNR number to cancel: ");
        String pnr = scanner.nextLine();

        String query = "SELECT * FROM reservations WHERE pnr = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, pnr);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Reservation found: ");
                System.out.println("Train Name: " + rs.getString("train_name"));
                System.out.println("From: " + rs.getString("from_place") + " To: " + rs.getString("to_destination"));
                System.out.println("Journey Date: " + rs.getDate("journey_date"));
                System.out.print("Do you want to cancel this reservation? (yes/no): ");
                String response = scanner.nextLine();
                if (response.equalsIgnoreCase("yes")) {
                    String cancelQuery = "DELETE FROM reservations WHERE pnr = ?";
                    try (PreparedStatement cancelStmt = connection.prepareStatement(cancelQuery)) {
                        cancelStmt.setString(1, pnr);
                        cancelStmt.executeUpdate();
                        System.out.println("Reservation cancelled successfully!");
                    }
                }
            } else {
                System.out.println("Reservation not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

      private String generatePNR() {
        Random random = new Random();
        StringBuilder pnr = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            pnr.append(random.nextInt(10));
        }
        return pnr.toString();
    }
}

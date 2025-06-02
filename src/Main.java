import services.RentalService;
import Models.Customer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RentalService rentalService = new RentalService();
        Scanner scanner = new Scanner(System.in);
        Customer loggedInCustomer = null;

        while (true) {
            System.out.println("\n--- Car Rental System ---");

            if (loggedInCustomer == null) {
                System.out.println("1. Register");
                System.out.println("2. Login");
            } else {
                System.out.println("1. View Available Cars");
                System.out.println("2. Logout");
            }

            System.out.println("0. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // clear newline

            switch (choice) {
                case 1:
                    if (loggedInCustomer == null) {
                        // Register
                        System.out.print("ID: ");
                        String id = scanner.nextLine();
                        System.out.print("Name: ");
                        String name = scanner.nextLine();
                        System.out.print("License: ");
                        String license = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();
                        rentalService.registerCustomer(id, name, license, password);
                    } else {
                        // View Available Cars
                        rentalService.showAvailableCars();
                    }
                    break;

                case 2:
                    if (loggedInCustomer == null) {
                        // Login
                        System.out.print("ID: ");
                        String loginId = scanner.nextLine();
                        System.out.print("Password: ");
                        String loginPassword = scanner.nextLine();
                        if (rentalService.login(loginId, loginPassword)) {
                            loggedInCustomer = rentalService.getCustomer(loginId);
                        }
                    } else {
                        // Logout
                        loggedInCustomer = null;
                        System.out.println("🔒 Logged out.");
                    }
                    break;

                case 0:
                    System.out.println("👋 Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }
}

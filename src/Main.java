import services.RentalService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RentalService rentalService = new RentalService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Car Rental System ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. List Customers");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt(); scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    System.out.print("License Number: ");
                    String license = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    rentalService.registerCustomer(id, name, license, password);
                    break;
                case 2:
                    System.out.print("ID: ");
                    String loginId = scanner.nextLine();
                    System.out.print("Password: ");
                    String loginPassword = scanner.nextLine();
                    rentalService.login(loginId, loginPassword);
                    break;
                case 3:
                    rentalService.listAllCustomers();
                    break;
                case 4:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }
}

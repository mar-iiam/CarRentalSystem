import services.RentalService;
import Models.Customer;
import utils.DateUtils;

import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RentalService rentalService = new RentalService();
        Scanner scanner = new Scanner(System.in);
        Customer loggedInCustomer = null;
        boolean carsListShowed = false;

        while (true) {
            System.out.println("\n--- Car Rental System ---");

            if (loggedInCustomer == null) {
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("0. Exit");

                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // clear newline

                switch (choice) {
                    case 1:
                        System.out.print("ID: ");
                        String id = scanner.nextLine();

                        boolean isAdmin = id.matches("200\\d{2}");

                        System.out.print("Name: ");
                        String name = scanner.nextLine();

                        String license;
                        if (isAdmin) {
                            license = "N/A";
                        } else {
                            System.out.print("License: ");
                            license = scanner.nextLine();
                        }

                        System.out.print("Password: ");
                        String password = scanner.nextLine();

                        rentalService.registerCustomer(id, name, license, password);
                        break;

                    case 2:
                        System.out.print("ID: ");
                        String loginId = scanner.nextLine();
                        System.out.print("Password: ");
                        String loginPassword = scanner.nextLine();
                        if (rentalService.login(loginId, loginPassword)) {
                            loggedInCustomer = rentalService.getCustomer(loginId);
                        }
                        break;

                    case 0:
                        System.out.println("👋 Goodbye!");
                        scanner.close();
                        return;

                    default:
                        System.out.println("❌ Invalid choice.");
                }
            } else if (loggedInCustomer.getType().equals("admin")) {
                // Admin Menu
                System.out.println("1. View All Customers");
                System.out.println("2. View All Cars");
                System.out.println("3. Add New Car");
                System.out.println("4. Logout");

                System.out.print("Enter choice: ");
                int adminChoice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (adminChoice) {
                    case 1:
                        rentalService.showAllCustomers();
                        break;

                    case 2:
                        rentalService.showAvailableCars();
                        break;

                    case 3:
                        System.out.print("Enter Car ID: ");
                        String carId = scanner.nextLine();
                        System.out.print("Enter Brand: ");
                        String brand = scanner.nextLine();
                        System.out.print("Enter Model: ");
                        String model = scanner.nextLine();
                        System.out.print("Enter Price Per Day: ");
                        double price = scanner.nextDouble();
                        scanner.nextLine(); // consume newline
                        rentalService.addCar(carId,brand,model,price);
                        break;

                    case 4:
                        loggedInCustomer = null;
                        System.out.println("🔒 Logged out.");
                        break;

                    default:
                        System.out.println("❌ Invalid admin choice.");
                }

            } else {
                // Customer Menu
                if (carsListShowed) {
                    System.out.println("1. Rent a Car");
                    System.out.println("2. Return Rented Car");
                    System.out.println("3. Logout");
                } else {
                    System.out.println("1. View Available Cars");
                    System.out.println("2. Rent a Car");
                    System.out.println("3. Return Rented Car");
                    System.out.println("4. Logout");
                }

                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // clear newline

                switch (choice) {
                    case 1:
                        if (carsListShowed) {
                            if (loggedInCustomer.getRentedCarId() != null) {
                                System.out.println("⚠️ You already rented a car. Return it first.");
                            } else {
                                rentalService.showAvailableCars();
                                handleRentalProcess(scanner, rentalService, loggedInCustomer);
                            }
                        } else {
                            carsListShowed = true;
                            rentalService.showAvailableCars();
                        }
                        break;

                    case 2:
                        if (carsListShowed) {
                            rentalService.returnCar(loggedInCustomer);
                        } else {
                            if (loggedInCustomer.getRentedCarId() != null) {
                                System.out.println("⚠️ You already rented a car. Return it first.");
                            } else {
                                rentalService.showAvailableCars();
                                handleRentalProcess(scanner, rentalService, loggedInCustomer);
                            }
                        }
                        break;

                    case 3:
                        if (carsListShowed) {
                            loggedInCustomer = null;
                            System.out.println("🔒 Logged out.");
                        } else {
                            rentalService.returnCar(loggedInCustomer);
                        }
                        break;

                    case 4:
                        loggedInCustomer = null;
                        System.out.println("🔒 Logged out.");
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

    private static void handleRentalProcess(Scanner scanner, RentalService rentalService, Customer customer) {
        System.out.print("Enter Car ID to rent: ");
        String carId = scanner.nextLine();

        System.out.print("Enter Start Date (yyyy-MM-dd): ");
        String startDateStr = scanner.nextLine();
        System.out.print("Enter End Date (yyyy-MM-dd): ");
        String endDateStr = scanner.nextLine();

        LocalDate startDate = DateUtils.parseDate(startDateStr);
        LocalDate endDate = DateUtils.parseDate(endDateStr);

        if (startDate == null || endDate == null || !DateUtils.isValidDateRange(startDate, endDate)) {
            System.out.println("❌ Invalid date input. Please try again.");
            return;
        }

        long totalDays = DateUtils.daysBetween(startDate, endDate);
        double dailyRate = rentalService.getCarDailyRate(carId);
        if (dailyRate < 0) {
            System.out.println("❌ Invalid car ID.");
            return;
        }

        double totalCost = totalDays * dailyRate;
        System.out.printf("Total cost for %d days: $%.2f\n", totalDays, totalCost);
        System.out.print("Do you want to proceed with the payment? (yes/no): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            rentalService.rentCar(customer, carId, startDate, endDate);
        } else {
            System.out.println("❌ Rental cancelled.");
        }
    }
}

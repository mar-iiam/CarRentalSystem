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
            } else {
                if(carsListShowed){
                    System.out.println("1. Rent a Car");
                    System.out.println("2. Return Rented Car");
                    System.out.println("3. Logout");
                }else{
                    System.out.println("1. View Available Cars");
                    System.out.println("2. Rent a Car");
                    System.out.println("3. Return Rented Car");
                    System.out.println("4. Logout");
                }

            }


            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // clear newline

            if (loggedInCustomer == null) {
                switch (choice) {
                    case 1:
                        System.out.print("ID: ");
                        String id = scanner.nextLine();
                        System.out.print("Name: ");
                        String name = scanner.nextLine();
                        System.out.print("License: ");
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
            } else {
                switch (choice) {
                    case 1:
                        if(carsListShowed){
                            if (loggedInCustomer.getRentedCarId() != null) {
                                System.out.println("⚠️ You already rented a car. Return it first.");
                            } else {
                                rentalService.showAvailableCars();

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
                                    break;
                                }

                                long totalDays = DateUtils.daysBetween(startDate, endDate);
                                double dailyRate = rentalService.getCarDailyRate(carId);
                                if (dailyRate < 0) {
                                    System.out.println("❌ Invalid car ID.");
                                    break;
                                }

                                double totalCost = totalDays * dailyRate;
                                System.out.printf("Total cost for %d days: $%.2f\n", totalDays, totalCost);
                                System.out.print("Do you want to proceed with the payment? (yes/no): ");
                                String confirm = scanner.nextLine();

                                if (confirm.equalsIgnoreCase("yes")) {
                                    rentalService.rentCar(loggedInCustomer, carId, startDate, endDate);
                                } else {
                                    System.out.println("❌ Rental cancelled.");
                                }
                            }
                            break;

                        }else{
                            carsListShowed = true ;
                            rentalService.showAvailableCars();
                            break;
                        }


                    case 2:
                        if(carsListShowed){
                            rentalService.returnCar(loggedInCustomer);
                            break;
                        }else {
                            if (loggedInCustomer.getRentedCarId() != null) {
                                System.out.println("⚠️ You already rented a car. Return it first.");
                            } else {
                                rentalService.showAvailableCars();

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
                                    break;
                                }

                                long totalDays = DateUtils.daysBetween(startDate, endDate);
                                double dailyRate = rentalService.getCarDailyRate(carId);
                                if (dailyRate < 0) {
                                    System.out.println("❌ Invalid car ID.");
                                    break;
                                }

                                double totalCost = totalDays * dailyRate;
                                System.out.printf("Total cost for %d days: $%.2f\n", totalDays, totalCost);
                                System.out.print("Do you want to proceed with the payment? (yes/no): ");
                                String confirm = scanner.nextLine();

                                if (confirm.equalsIgnoreCase("yes")) {
                                    rentalService.rentCar(loggedInCustomer, carId, startDate, endDate);
                                } else {
                                    System.out.println("❌ Rental cancelled.");
                                }
                            }
                            break;

                        }

                    case 3:
                        if(carsListShowed){
                            loggedInCustomer = null;
                            System.out.println("🔒 Logged out.");
                            break;
                        }else {
                            rentalService.returnCar(loggedInCustomer);
                            break;
                        }


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
}

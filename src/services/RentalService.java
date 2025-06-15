package services;

import Models.Car;
import Models.CarStatus;
import Models.Customer;
import utils.DateUtils;
import utils.passwordUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RentalService {
    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String CAR_FILE = "Cars.txt";
    private static final String RENTAL_FILE = "rentals.txt";
    private Map<String, Integer> loginAttempts;
    private Map<String, Customer> customers;
    private List<Car> cars;

    public RentalService() {
        customers = new HashMap<>();
        cars = new ArrayList<>();
        loadCustomersFromFile();
        loadCarsFromFile();
        this.loginAttempts = new HashMap<>();
    }

    public Customer getCustomer(String id) {
        return customers.get(id);
    }

    public void showAvailableCars() {
        System.out.println("\n🚗 Available Cars.txt:");
        boolean found = false;
        for (Car car : cars) {
            if (car.getStatus() == CarStatus.AVAILABLE) {
                System.out.println(car);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No cars are available at the moment.");
        }
    }

    public boolean registerCustomer(String id, String name, String licenseNumber, String password) {
        if (customers.containsKey(id)) {
            System.out.println("❌ Customer ID already exists.");
            return false;
        }

        String hashedPassword = passwordUtils.hashPassword(password);
        Customer newCustomer = new Customer(id, name, licenseNumber, hashedPassword);
        customers.put(id, newCustomer);
        saveCustomersToFile();
        System.out.println("✅ Registration successful.");
        return true;
    }

    public boolean login(String id, String password) {
        final int MAX_LOGIN_ATTEMPTS = 3;

        // Initialize login attempts for the customer if they haven't tried before
        loginAttempts.putIfAbsent(id, 0);

        // Check if the customer has exceeded the maximum login attempts
        if (loginAttempts.size() >= MAX_LOGIN_ATTEMPTS) {
            System.out.println("❌ Login failed: You have exceeded the maximum login attempts.");
            System.out.println("Please call customer services at 1999.");
            System.exit(0); // Exit the program
            return false; // This line won't be reached due to System.exit(0)
        }

        Customer customer = customers.get(id);

        if (customer == null) {
            System.out.println("❌ Login failed: Customer ID not found.");
            // Increment attempts even for non-existent IDs to prevent enumeration attacks
            loginAttempts.compute(id, (k, v) -> v + 1);
            return false;
        }

        String hashedInput = passwordUtils.hashPassword(password);
        if (!customer.getPassword().equals(hashedInput)) {
            System.out.println("❌ Login failed: Incorrect password.");
            // Increment attempts on incorrect password
            loginAttempts.compute(id, (k, v) -> v + 1);
            return false;
        }

        // If login is successful, reset login attempts for this customer
        loginAttempts.put(id, 0);
        System.out.println("✅ Login successful. Welcome, " + customer.getName() + "!");
        return true;
    }

    private void saveCustomersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CUSTOMER_FILE))) {
            for (Customer c : customers.values()) {
                writer.println(c.getId() + "," + c.getName() + "," + c.getLicenseNumber() + "," + c.getPassword());
            }
        } catch (IOException e) {
            System.err.println("Error saving customers: " + e.getMessage());
        }
    }

    private void loadCustomersFromFile() {
        File file = new File(CUSTOMER_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    Customer customer = new Customer(parts[0], parts[1], parts[2], parts[3]);
                    customers.put(parts[0], customer);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
    }

    public Car getCarById(String id) {
        for (Car car : cars) {
            if (car.getId().equalsIgnoreCase(id)) return car;
        }
        return null;
    }
    private boolean isCustomerRentingNow(String customerId) {
        File file = new File("rentals.txt");
        if (!file.exists()) return false;

        LocalDate today = LocalDate.now();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                String id = parts[0];
                LocalDate endDate = LocalDate.parse(parts[3]);

                if (id.equals(customerId) && !endDate.isBefore(today)) {
                    return true; // User has an ongoing or upcoming rental
                }
            }
        } catch (IOException | DateTimeParseException e) {
            System.err.println("Error reading rentals file: " + e.getMessage());
        }

        return false;
    }
    public static boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;  // or n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
    public static boolean processVisaPayment(String cardNumber, String cvv, double amount) {
        System.out.println("🔄 Connecting to Visa payment gateway...");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if card number is valid VISA format and passes Luhn
        if (!cardNumber.startsWith("4") || cardNumber.length() != 16 || !luhnCheck(cardNumber)) {
            System.out.println("❌ Payment declined: Invalid or fake card number.");
            return false;
        }

        if (!cvv.matches("\\d{3}")) {
            System.out.println("❌ Payment declined: Invalid CVV.");
            return false;
        }

        System.out.printf("✅ Payment of $%.2f approved.\n", amount);
        return true;
    }
    public boolean rentCar(Customer customer, String carId, LocalDate startDate, LocalDate endDate) {
        if (isCustomerRentingNow(customer.getId())) {
            System.out.println("⚠️ You already rented a car (according to rentals file). Return it first.");
            return false;
        }

        Car car = getCarById(carId);
        if (car == null) {
            System.out.println("❌ Car ID not found.");
            return false;
        }

        if (car.getStatus() != CarStatus.AVAILABLE) {
            System.out.println("❌ Car is not available.");
            return false;
        }

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            System.out.println("❌ Invalid dates provided.");
            return false;
        }

        long rentalDays = DateUtils.daysBetween(startDate, endDate);
        if (rentalDays <= 0) rentalDays = 1;

        double totalPrice = rentalDays * car.getPricePerDay();

        System.out.printf("\n--- Rental Summary ---\nCar: %s %s\nFrom: %s\nTo: %s\nDays: %d\nTotal Price: $%.2f\n",
                car.getBrand(), car.getModel(), startDate, endDate, rentalDays, totalPrice);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Do you want to confirm and proceed to payment? (yes/no): ");
        String confirm = scanner.nextLine();

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("❌ Rental cancelled.");
            return false;
        }

        // Payment menu
        System.out.print("Choose payment method (cash/visa): ");
        String paymentMethod = scanner.nextLine().toLowerCase();

        if (paymentMethod.equals("visa")) {
            System.out.print("Enter card number (16 digits): ");
            String cardNumber = scanner.nextLine();

            System.out.print("Enter CVV (3 digits): ");
            String cvv = scanner.nextLine();

            boolean paymentSuccess = processVisaPayment(cardNumber, cvv, totalPrice);
            if (!paymentSuccess) {
                System.out.println("❌ Rental cancelled due to failed payment.");
                return false;
            }

        } else if (paymentMethod.equals("cash")) {
            System.out.println("💵 Please pay at the counter when picking up the car.");
        } else {
            System.out.println("❌ Invalid payment method.");
            return false;
        }

        // Finalize rental
        car.setStatus(CarStatus.RENTED);
        customer.setRentedCarId(car.getId());

        System.out.println("✅ Car rented successfully!");

        saveRentalToFile(customer.getId(), car.getId(), startDate, endDate);
        saveCarsToFile();

        return true;
    }


    public boolean returnCar(Customer customer) {
        String carId = customer.getRentedCarId();
        if (carId == null) {
            System.out.println("⚠️ You have not rented any car.");
            return false;
        }

        Car car = getCarById(carId);
        if (car != null) {
            car.setStatus(CarStatus.AVAILABLE);
        }
        customer.setRentedCarId(null);

        // Load rental end date from file
        LocalDate actualReturnDate = LocalDate.now();
        LocalDate rentalEndDate = null;

        try (BufferedReader reader = new BufferedReader(new FileReader("rentals.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4 &&
                        parts[0].equals(customer.getId()) &&
                        parts[1].equals(carId)) {
                    rentalEndDate = LocalDate.parse(parts[3]);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Failed to read rentals file.");
        }

        if (rentalEndDate != null) {
            long daysLate = ChronoUnit.DAYS.between(rentalEndDate, actualReturnDate);
            if (daysLate > 2) {
                double penalty = (daysLate - 2) * 50; // $50 per day after 2-day grace
                System.out.printf("⚠️ Car is returned %d days late.\n", daysLate);
                System.out.printf("💸 Late return penalty: $%.2f\n", penalty);
            }
        }

        saveCarsToFile();
        System.out.println("✅ Car returned successfully.");
        return true;
    }


    public double getCarDailyRate(String carId) {
        for (Car car : cars) {
            if (car.getId().equalsIgnoreCase(carId) && car.getStatus() == CarStatus.AVAILABLE) {
                return car.getPricePerDay();
            }
        }
        return -1;
    }

    private void loadCarsFromFile() {
        File file = new File(CAR_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 5);
                if (parts.length == 5) {
                    Car car = new Car(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]));
                    car.setStatus(CarStatus.valueOf(parts[4]));
                    cars.add(car);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading cars: " + e.getMessage());
        }
    }

    private void saveCarsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CAR_FILE))) {
            for (Car car : cars) {
                writer.println(car.getId() + "," + car.getBrand() + "," + car.getModel() + "," +
                        car.getPricePerDay() + "," + car.getStatus());
            }
        } catch (IOException e) {
            System.err.println("Error saving cars: " + e.getMessage());
        }
    }

    private void saveRentalToFile(String customerId, String carId, LocalDate startDate, LocalDate endDate) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RENTAL_FILE, true))) {
            writer.printf("%s,%s,%s,%s%n", customerId, carId, startDate, endDate);
        } catch (IOException e) {
            System.err.println("Error saving rental: " + e.getMessage());
        }
    }
}

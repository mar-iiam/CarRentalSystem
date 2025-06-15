package services;

import Models.Car;
import Models.CarStatus;
import Models.Customer;
import utils.DateUtils;
import utils.passwordUtils;

import java.io.*;
import java.time.LocalDate;
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

    public boolean rentCar(Customer customer, String carId, LocalDate startDate, LocalDate endDate) {
        if (customer.getRentedCarId() != null) {
            System.out.println("⚠️ You already rented a car. Return it first.");
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

        System.out.print("Do you want to confirm and proceed to payment? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            car.setStatus(CarStatus.RENTED);
            customer.setRentedCarId(car.getId());

            System.out.println("💳 Processing payment...");
            System.out.println("✅ Payment successful. Car rented!");

            saveRentalToFile(customer.getId(), car.getId(), startDate, endDate);
            saveCarsToFile();

            return true;
        } else {
            System.out.println("❌ Rental cancelled.");
            return false;
        }
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

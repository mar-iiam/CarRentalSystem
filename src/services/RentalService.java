package services;

import Models.Car;
import Models.CarStatus;
import Models.Customer;
import utils.passwordUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RentalService {
    private static final String CUSTOMER_FILE = "customers.txt";
    private Map<String, Customer> customers;
    private List<Car> cars;

    public Customer getCustomer(String id) {
        return customers.get(id);
    }

    public RentalService() {
        customers = new HashMap<>();
        cars = new ArrayList<>();
        loadCustomersFromFile();
        loadCars(); // initialize cars
    }
    private void loadCars() {
        cars.add(new Car("CAR001", "Toyota", "Corolla", 40.0));
        cars.add(new Car("CAR002", "Honda", "Civic", 45.0));
        cars.add(new Car("CAR003", "Ford", "Focus", 38.0));
        cars.add(new Car("CAR004", "BMW", "3 Series", 70.0));
        cars.add(new Car("CAR005", "Hyundai", "Elantra", 42.0));
    }
    public void showAvailableCars() {
        System.out.println("\n🚗 Available Cars:");
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
        Customer customer = customers.get(id);

        if (customer == null) {
            System.out.println("❌ Login failed: Customer ID not found.");
            return false;
        }

        String hashedInput = passwordUtils.hashPassword(password);
        if (!customer.getPassword().equals(hashedInput)) {
            System.out.println("❌ Login failed: Incorrect password.");
            return false;
        }

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

    public void listAllCustomers() {
        customers.values().forEach(System.out::println);
    }
}

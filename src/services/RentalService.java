package services;

import Models.Customer;
import utils.passwordUtils;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RentalService {
    private static final String CUSTOMER_FILE = "customers.txt";
    private Map<String, Customer> customers;

    public RentalService() {
        this.customers = new HashMap<>();
        loadCustomersFromFile();
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
        String hashedInput = passwordUtils.hashPassword(password);
        if (customer != null && customer.getPassword().equals(hashedInput)) {
            System.out.println("✅ Login successful. Welcome, " + customer.getName() + "!");
            return true;
        } else {
            System.out.println("❌ Invalid ID or password.");
            return false;
        }
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

package Models;

import java.util.Objects;

public class Customer {
    private final String id;
    private String name;
    private final String licenseNumber;
    private String password;

    public Customer(String id, String name, String licenseNumber, String password) {
        this.id = id;
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.password = password;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getPassword() { return password; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return String.format("Customer{id='%s', name='%s', license='%s'}", id, name, licenseNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return id.equals(customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

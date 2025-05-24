package Models;

public class Truck extends Car{
    private double loadCapacity; // in tons

    public Truck(String id, String brand, String model, int year, double loadCapacity) {
        super(id, brand, model, year);
        this.loadCapacity = loadCapacity;
    }

    public double getLoadCapacity() {
        return loadCapacity;
    }

    public void setLoadCapacity(double loadCapacity) {
        this.loadCapacity = loadCapacity;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", loadCapacity=%.1f tons", loadCapacity);
    }
}

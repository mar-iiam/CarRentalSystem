package Models;

public class ElectricCar extends Car{
    private int batteryCapacity; // in kWh

    public ElectricCar(String id, String brand, String model, int year, int batteryCapacity) {
        super(id, brand, model, year);
        this.batteryCapacity = batteryCapacity;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(int batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    @Override
    public String toString() {
        return super.toString()+String.format("ElectricCar{" +
                "batteryCapacity=" + batteryCapacity +
                '}');
    }
}

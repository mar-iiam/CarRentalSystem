package Models;

public class SportsCar extends Car{
    private int topSpeed;

    public SportsCar(String id, String brand, String model, int year, int topSpeed) {
        super(id, brand, model, year);
        this.topSpeed = topSpeed;
    }

    public int getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", topSpeed=%dkm/h", topSpeed);
    }
}

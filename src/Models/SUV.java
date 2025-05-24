package Models;

public class SUV extends Car{
    private boolean fourWheelDrive;

    public SUV(String id, String brand, String model, int year, boolean fourWheelDrive) {
        super(id, brand, model, year);
        this.fourWheelDrive = fourWheelDrive;
    }

    public boolean isFourWheelDrive() {
        return fourWheelDrive;
    }

    public void setFourWheelDrive(boolean fourWheelDrive) {
        this.fourWheelDrive = fourWheelDrive;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", 4WD=%s", fourWheelDrive);
    }

}

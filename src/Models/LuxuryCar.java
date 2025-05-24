package Models;

public class LuxuryCar extends Car{
    private boolean hasMassageSeats;

    public LuxuryCar(String id, String brand, String model, int year, boolean hasMassageSeats) {
        super(id, brand, model, year);
        this.hasMassageSeats = hasMassageSeats;
    }

    public boolean hasMassageSeats() {
        return hasMassageSeats;
    }

    public void setHasMassageSeats(boolean hasMassageSeats) {
        this.hasMassageSeats = hasMassageSeats;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", massageSeats=%s", hasMassageSeats);
    }
}

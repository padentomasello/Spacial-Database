package tracker;

import static org.junit.Assert.*;

import org.junit.Test;

public class VehicleTest {

    @Test
    public void test() {
        Vehicle car = new Vehicle(20, 20);
        assertEquals("Vehicle working incorrectly", 0, car.x(), 0.00001);
        assertEquals("Vehicle working incorrectly", 20, car.y(), 0.00001);
        car.advance(1);
        assertEquals("Vehicle working incorrectly", 20, car.x(), 0.00001);
        assertEquals("Vehicle working incorrectly", 20, car.y(), 0.00001);
        Vehicle car2 = new Vehicle(20, 20);
        Chooser chooser = new Chooser(20);
        car2.detected(chooser);
        car2.advance(1);
        assertEquals("Vehicle detection faulty", false, car2.x() == car.x());
        assertEquals("Vehicle detection faulty", false, car2.y() == car.y());
    }

}

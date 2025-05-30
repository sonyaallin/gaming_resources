import org.firmata4j.firmata.*;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;

public class VibrationMotor {

    public static void main(String []args) {
        String myPort = "/dev/cu.SLAB_USBtoUART"; // MODIFY THIS for your own computer & setup.

        try {
            IODevice device = new FirmataDevice(myPort); // using the name of a port
            device.start();
            device.ensureInitializationIsDone();

            Pin vibrationMotorPin1 = device.getPin(7);
            Pin vibrationMotorPin2 = device.getPin(6);
            Pin vibrationMotorPin3 = device.getPin(4);
            Pin vibrationMotorPin4 = device.getPin(5);

            vibrationMotorPin1.setMode(Pin.Mode.OUTPUT);
            vibrationMotorPin2.setMode(Pin.Mode.OUTPUT);
            vibrationMotorPin3.setMode(Pin.Mode.OUTPUT);
            vibrationMotorPin4.setMode(Pin.Mode.OUTPUT);

            // Activate the vibration motor
            for (int i = 0; i < 10; i++) {
                System.out.println("Activating vibration motor 1:" + i);
                vibrationMotorPin1.setValue(1);
                vibrationMotorPin2.setValue(0);
                vibrationMotorPin3.setValue(1);
                vibrationMotorPin4.setValue(0);
                Thread.sleep(1000);
                vibrationMotorPin1.setValue(0);
                vibrationMotorPin2.setValue(0);
                vibrationMotorPin3.setValue(0);
                vibrationMotorPin4.setValue(0);
                Thread.sleep(1000);
            }

            device.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

import java.io.IOException;

public class VibrationModule {

    Pin [] vibrationMotorPins =new Pin[4];

    long maxInterval = 1000;
    long minInterval = 100;
    long currInterval;

    public VibrationModule(IODevice myGroveBoard) {

        vibrationMotorPins[0] = myGroveBoard.getPin(4);
        vibrationMotorPins[1] = myGroveBoard.getPin(5);
        vibrationMotorPins[2] = myGroveBoard.getPin(2);
        vibrationMotorPins[3] = myGroveBoard.getPin(3);
        currInterval = maxInterval;

        try {
            for (int i = 0; i < 4; i++) vibrationMotorPins[i].setMode(Pin.Mode.OUTPUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public void vibrate() {

        int pin = 0;
        if (currInterval < 0) {
            pin = 2;
        }

        try {
            vibrationMotorPins[pin].setValue(0);
            vibrationMotorPins[pin + 1].setValue(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Thread.sleep(Math.abs(currInterval));
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        try {
            vibrationMotorPins[pin].setValue(0);
            vibrationMotorPins[pin + 1].setValue(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Thread.sleep(Math.abs(currInterval));
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

    }

    public void setvibrationInterval(long i) {
        currInterval = i;
    }

    public static void initGroveBoardV(IODevice myGroveBoard, String myPort) {
        // try to communicate with the board
        try {
            myGroveBoard.start(); // start communication with board;
            myGroveBoard.ensureInitializationIsDone();
            System.out.println("Board started."); //hopefully we make it here.
        } catch (Exception ex) { // if not, detail the error.
            System.out.println("couldn't connect to board.");
            return; //no point continuing at this point.
        }

    }

    public static void main(String args[]) {

        final String myPort = "/dev/cu.SLAB_USBtoUART"; // MODIFY THIS for your own computer & setup.
        final IODevice myGroveBoard = new FirmataDevice(myPort); // using the name of a port

        initGroveBoardV(myGroveBoard, myPort); //init the grove

        VibrationModule v = new VibrationModule(myGroveBoard);

        for (int j = 0; j < 3; j++) {
            System.out.println(j);
            v.setvibrationInterval(-800);
            v.vibrate();
            v.setvibrationInterval(800);
            v.vibrate();
        }

        try {
            myGroveBoard.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

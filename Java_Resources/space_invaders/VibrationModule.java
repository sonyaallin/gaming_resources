import org.firmata4j.IODevice;
import org.firmata4j.Pin;

import java.io.IOException;

public class VibrationModule {

    Pin [] vibrationMotorPins =new Pin[4];

    long maxInterval = 1000;
    long minInterval = 100;
    long currInterval;

    public VibrationModule(IODevice myGroveBoard) {
        vibrationMotorPins[0] = myGroveBoard.getPin(2);
        vibrationMotorPins[1] = myGroveBoard.getPin(3);
        vibrationMotorPins[2] = myGroveBoard.getPin(4);
        vibrationMotorPins[3] = myGroveBoard.getPin(5);
        currInterval = maxInterval;

        try {
            for (int i = 0; i < 4; i++) vibrationMotorPins[i].setMode(Pin.Mode.OUTPUT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void vibrate() {
        int start, stop;
        if (currInterval > 0) {
            start = 0;
            stop = 2;
        } else {
            start = 2;
            stop = 4;
        }
        for (int j = 0; j < 2; j++) {
            for (int i = start; i < stop; i++) {
                try {
                    if (j == 0 && i % 2 == 0) vibrationMotorPins[i].setValue(1);
                    else vibrationMotorPins[i].setValue(0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Thread.sleep(Math.abs(currInterval));
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    public void setvibrationInterval(long i) {
        currInterval = i;
    }
}

package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.Shift;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtendedGpioProvider extends GpioProviderBase {
    public static final String NAME = "org.dropco.smarthome.gpioextension.ExtendedGpioProvider";
    private static Logger logger = Logger.getLogger(ExtendedGpioProvider.class.getName());
    private GpioController gpio;
    private GpioPinDigitalOutput dataOutPin;
    private GpioPinDigitalOutput clockPin;
    private GpioPinDigitalOutput gatePin;
    private static Sender sender = new DefaultSender();

    private byte value = 0;

    public ExtendedGpioProvider(GpioController gpio, GpioPinDigitalOutput dataOutPin, GpioPinDigitalOutput clockPin, GpioPinDigitalOutput gatePin) {
        this.gpio = gpio;
        this.dataOutPin = dataOutPin;
        this.clockPin = clockPin;
        this.gatePin = gatePin;
    }

    @Override
    public void setState(Pin pin, PinState state) {
        super.setState(pin, state);
        if (state.isHigh()) {
            value |= 1 << (pin.getAddress() - 101);
            sent();
        } else {
            value &= ~(1 << (pin.getAddress() - 101));
            sent();
        }
    }

    @Override
    public PinState getState(Pin pin) {
        if ((value & (1 << (pin.getAddress() - 101))) != 0) return PinState.HIGH;
        return PinState.LOW;
    }

    void sent() {
        sender.sent(this);
    }

    public static void simulate() {
        sender = new SimulatedSender();
    }

    public void close() {
        gpio.unprovisionPin(dataOutPin, clockPin, gatePin);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setValue(Pin pin, double value) {
        super.setValue(pin, value);
    }

    private interface Sender {

        void sent(ExtendedGpioProvider provider);
    }

    private static class DefaultSender implements Sender {
        @Override
        public void sent(ExtendedGpioProvider provider) {
            provider.gatePin.low();
            logger.log(Level.FINE,"AND nastaveny na 0");
            Shift.shiftOut((byte) provider.dataOutPin.getPin().getAddress(), (byte) provider.clockPin.getPin().getAddress(), (byte) Shift.MSBFIRST, provider.value);
            logger.log(Level.FINE,"Hodnota "+provider.value+" poslana na posuvny register");
            provider.gatePin.high();
            logger.log(Level.FINE,"AND nastaveny na 1");
        }
    }

    private static class SimulatedSender implements Sender {

        @Override
        public void sent(ExtendedGpioProvider provider) {
        }
    }
}

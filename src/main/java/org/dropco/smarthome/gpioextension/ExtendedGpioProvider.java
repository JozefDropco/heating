package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.Shift;

public class ExtendedGpioProvider extends GpioProviderBase {
    public static final String NAME = "org.dropco.smarthome.gpioextension.ExtendedGpioProvider";

    private GpioController gpio;
    private GpioPinDigitalOutput dataOutPin;
    private GpioPinDigitalOutput clockPin;
    private GpioPinDigitalOutput gatePin;

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
        gatePin.low();
        Shift.shiftOut((byte) dataOutPin.getPin().getAddress(), (byte) clockPin.getPin().getAddress(), (byte) Shift.MSBFIRST, value);
        gatePin.high();
    }

    public void close(){
        gpio.unprovisionPin(dataOutPin,clockPin,gatePin);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setValue(Pin pin, double value) {
        super.setValue(pin, value);
    }
}

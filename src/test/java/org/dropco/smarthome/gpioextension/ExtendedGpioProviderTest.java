package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.*;
import org.junit.Test;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class ExtendedGpioProviderTest {
    private static final GpioController gpio = GpioFactory.getInstance();
    @Test
    public void test() throws InterruptedException {
        GpioPinDigitalOutput dataOutPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO 0"), "GPIO 0", PinState.LOW);
        GpioPinDigitalOutput clockPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO 2"), "GPIO 2", PinState.LOW);
        GpioPinDigitalOutput gatePin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO 3"), "GPIO 3", PinState.LOW);
        ExtendedGpioProvider extendedGpioProvider = new ExtendedGpioProvider(gpio, dataOutPin, clockPin, gatePin);
        extendedGpioProvider.setState(ExtendedPin.GPIO_101,PinState.HIGH);
        sleep(2*60*1000);
    }

}
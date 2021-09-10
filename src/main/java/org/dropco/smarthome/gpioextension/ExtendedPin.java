package org.dropco.smarthome.gpioextension;

import com.pi4j.io.gpio.*;

import java.util.EnumSet;

public class ExtendedPin extends PinProvider {
    public static final Pin GPIO_101 = createPin(ExtendedGpioProvider.NAME,101, "GPIO 101",EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));
    public static final Pin GPIO_102 = createPin(ExtendedGpioProvider.NAME,102, "GPIO 102",EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));
    public static final Pin GPIO_103 = createPin(ExtendedGpioProvider.NAME,103, "GPIO 103",EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));
    public static final Pin GPIO_104 = createPin(ExtendedGpioProvider.NAME,104, "GPIO 104",EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));
    public static final Pin GPIO_105 = createPin(ExtendedGpioProvider.NAME,105, "GPIO 105",EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));
    public static final Pin GPIO_106 = createPin(ExtendedGpioProvider.NAME,106, "GPIO 106",EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));
    public static final Pin GPIO_107 = createPin(ExtendedGpioProvider.NAME,107, "GPIO 107",EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));
    public static final Pin GPIO_108 = createPin(ExtendedGpioProvider.NAME,108, "GPIO 108", EnumSet.of(PinMode.DIGITAL_OUTPUT),EnumSet.allOf(PinPullResistance.class),EnumSet.allOf(PinEdge.class));


    public static Pin getPinByName(String name) {
        return pins.get(name);
    }
}

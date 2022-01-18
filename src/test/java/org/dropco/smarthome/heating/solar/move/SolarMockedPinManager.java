package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.TickerPin;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;

class SolarMockedPinManager extends MockedPinManager {

    private final TickerPin verticalTickPin;
    private final TickerPin horizontalTickPin;

    public SolarMockedPinManager(TickerPin verticalTickPin, TickerPin horizontalTickPin) {
        this.verticalTickPin = verticalTickPin;
        this.horizontalTickPin = horizontalTickPin;
    }

    @Override
    public void setState(String key, boolean value) {
        System.out.println(key+" "+value);
        switch (key) {
            case SolarSystemRefCode.NORTH_PIN_REF_CD:
            case SolarSystemRefCode.SOUTH_PIN_REF_CD:
                if (value) verticalTickPin.startTicking();
                else verticalTickPin.stopTicking();
                break;
            case SolarSystemRefCode.WEST_PIN_REF_CD:
            case SolarSystemRefCode.EAST_PIN_REF_CD:
                if (value) horizontalTickPin.startTicking();
                else horizontalTickPin.stopTicking();
                break;
            default:
        }
        super.setState(key, value);
    }

}

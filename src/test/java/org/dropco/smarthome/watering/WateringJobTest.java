package org.dropco.smarthome.watering;

import org.junit.Test;

import java.util.Calendar;

public class WateringJobTest {

    @Test
    public void test() {
        System.out.println("INSERT INTO STRING_SETTING(REF_CD,VALUE,MODIFIED_TS) VALUES( 'WATER_PUMP','GPIO 28',NOW());");
        Calendar calendar = Calendar.getInstance();
        Calendar till = Calendar.getInstance();
        till.add(Calendar.MONTH, 1);
        int cnt = 1;
        while (calendar.before(till)) {
            System.out.println("INSERT INTO WATERING_SCHEDULE(MONTH,DAY,HOUR,MINUTE,ZONE_REF_CD,TIME_IN_SEC) VALUES( "+(calendar.get(Calendar.MONTH)+1)+","+calendar.get(Calendar.DAY_OF_MONTH)+","+calendar.get(Calendar.HOUR_OF_DAY)+","+calendar.get(Calendar.MINUTE)+",'WATERING"+cnt+"',180);");
            cnt++;
            if (cnt == 5) {
                cnt = 1;
            }
            calendar.add(Calendar.MINUTE, 5);
        }

    }

}
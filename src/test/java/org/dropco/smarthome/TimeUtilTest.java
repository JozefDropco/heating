package org.dropco.smarthome;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class TimeUtilTest {

    @Test
    public void testAfter(){
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 6);
        instance.set(Calendar.MINUTE, 30);
        instance.set(Calendar.SECOND, 32);
        boolean after = TimeUtil.isAfter(instance, 6, 30);
        Assert.assertTrue(after);
    }

}

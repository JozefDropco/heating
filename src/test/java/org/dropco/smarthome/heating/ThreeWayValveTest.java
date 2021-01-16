package org.dropco.smarthome.heating;

import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.temp.TempService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.BiConsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class ThreeWayValveTest {


    @Before
    public void setup() {
    }

    @Test
    public void test() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        ThreeWayValve valve= spy(new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(valve).getStartThreshold();
        doAnswer(mock -> 5.0d).when(valve).getStopThreshold();
        doAnswer(mock -> "Ke").when(valve);
        doAnswer(mock -> (mock.getArgument(0).equals(ThreeWayValve.T31_TEMP_KEY)) ? "D1" : "D2").when(valve).getDeviceId(any());
        doAnswer(mock->false).when(valve).isWeekend(anyInt());

        TempService.setTemperature("D1",10.0);
        TempService.setTemperature("D2",10.0);
        Thread thread = new Thread(valve);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(0)).accept(any(), any());

    }

    @Test
    public void test2() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        ThreeWayValve valve= spy(new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(valve).getStartThreshold();
        doAnswer(mock -> 5.0d).when(valve).getStopThreshold();
        doAnswer(mock -> "Ke").when(valve);
        doAnswer(mock -> (mock.getArgument(0).equals(ThreeWayValve.T31_TEMP_KEY)) ? "D1" : "D2").when(valve).getDeviceId(any());
        doAnswer(mock->false).when(valve).isWeekend(anyInt());
        TempService.setTemperature("D1",70.0);
        TempService.setTemperature("D2",65.0);
        Thread thread = new Thread(valve);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
        Mockito.verify(COMMAND_EXECUTOR, times(0)).accept(any(), any());


    }

    @Test
    public void test3() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        ThreeWayValve valve= spy(new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(valve).getStartThreshold();
        doAnswer(mock -> 5.0d).when(valve).getStopThreshold();
        doAnswer(mock -> "Ke").when(valve);
        doAnswer(mock -> (mock.getArgument(0).equals(ThreeWayValve.T31_TEMP_KEY)) ? "D1" : "D2").when(valve).getDeviceId(any());
        doAnswer(mock->false).when(valve).isWeekend(anyInt());
        TempService.setTemperature("D1",77.0);
        TempService.setTemperature("D2",65.0);
        Thread thread = new Thread(valve);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(any(), any());

    }

    @Test
    public void test4() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        ThreeWayValve valve= spy(new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(valve).getStartThreshold();
        doAnswer(mock -> 5.0d).when(valve).getStopThreshold();
        doAnswer(mock -> "Ke").when(valve);
        doAnswer(mock -> (mock.getArgument(0).equals(ThreeWayValve.T31_TEMP_KEY)) ? "D1" : "D2").when(valve).getDeviceId(any());
        doAnswer(mock->false).when(valve).isWeekend(anyInt());
        TempService.setTemperature("D1",77.0);
        TempService.setTemperature("D2",65.0);
        Thread thread = new Thread(valve);
        thread.start();
        Thread.sleep(5000);
        TempService.setTemperature("D1",67.0);
        Thread.sleep(5000);
        TempService.setTemperature("D1",77.0);
        Thread.sleep(5000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(2)).accept(any(),eq(true));
        verify(COMMAND_EXECUTOR, times(1)).accept(any(),eq(false));

    }
}

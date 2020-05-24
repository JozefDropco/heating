package org.dropco.smarthome.heating;

import org.dropco.smarthome.temp.TempService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

public class CircularPumpTest {



    @Before
    public void setup() {
        }

    @Test
    public void test() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        CircularPump pump= spy(new CircularPump(COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(pump).getStartThreshold();
        doAnswer(mock -> 5.0d).when(pump).getStopThreshold();
        doAnswer(mock -> "Ke").when(pump).getCircularPumpPort();
        doAnswer(mock -> (mock.getArgument(0).equals(CircularPump.T1_TEMP_KEY)) ? "D1" : "D2").when(pump).getDeviceId(any());

        TempService.setTemperature("D1",10.0);
        TempService.setTemperature("D2",10.0);
        Thread thread = new Thread(pump);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(0)).accept(any(), any());

    }

    @Test
    public void test2() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        CircularPump pump= spy(new CircularPump(COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(pump).getStartThreshold();
        doAnswer(mock -> 5.0d).when(pump).getStopThreshold();
        doAnswer(mock -> "Ke").when(pump).getCircularPumpPort();
        doAnswer(mock -> (mock.getArgument(0).equals(CircularPump.T1_TEMP_KEY)) ? "D1" : "D2").when(pump).getDeviceId(any());

        TempService.setTemperature("D1",70.0);
        TempService.setTemperature("D2",65.0);
        Thread thread = new Thread(pump);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
        Mockito.verify(COMMAND_EXECUTOR, times(0)).accept(any(), any());


    }

    @Test
    public void test3() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        CircularPump pump= spy(new CircularPump(COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(pump).getStartThreshold();
        doAnswer(mock -> 5.0d).when(pump).getStopThreshold();
        doAnswer(mock -> "Ke").when(pump).getCircularPumpPort();
        doAnswer(mock -> (mock.getArgument(0).equals(CircularPump.T1_TEMP_KEY)) ? "D1" : "D2").when(pump).getDeviceId(any());

        TempService.setTemperature("D1",77.0);
        TempService.setTemperature("D2",65.0);
        Thread thread = new Thread(pump);
        thread.start();
        Thread.sleep(5000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(any(), any());

    }

    @Test
    public void test4() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        CircularPump pump= spy(new CircularPump(COMMAND_EXECUTOR));
        doAnswer(mock -> 10.0d).when(pump).getStartThreshold();
        doAnswer(mock -> 5.0d).when(pump).getStopThreshold();
        doAnswer(mock -> "Ke").when(pump).getCircularPumpPort();
        doAnswer(mock -> (mock.getArgument(0).equals(CircularPump.T1_TEMP_KEY)) ? "D1" : "D2").when(pump).getDeviceId(any());

        TempService.setTemperature("D1",77.0);
        TempService.setTemperature("D2",65.0);
        Thread thread = new Thread(pump);
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

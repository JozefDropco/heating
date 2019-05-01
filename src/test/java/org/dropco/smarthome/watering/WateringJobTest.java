package org.dropco.smarthome.watering;

import com.google.common.collect.Sets;
import org.dropco.smarthome.watering.db.WateringRecord;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;


public class WateringJobTest {

    @Test
    public void testUnderZero() {
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setContinuous(true);
        WateringJob job = new WateringJob(record);
        WateringJob.setTemperature(() -> -5.0);
        WateringJob.setTemperatureThreshold(() -> 5.0);
        WateringJob.setRaining(() -> false);
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        verify(commandExecutor, times(0)).accept(anyString(), anyBoolean());
    }

    @Test
    public void testRaining() {
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setContinuous(true);
        WateringJob job = new WateringJob(record);
        WateringJob.setTemperature(() -> 15.0);
        WateringJob.setRaining(() -> true);
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        verify(commandExecutor, times(0)).accept(anyString(), anyBoolean());
    }

    @Test
    public void testSuccessContinuos() {
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setTimeInSeconds(5);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setZoneRefCode("zone1");
        record.setContinuous(true);
        WateringJob job = new WateringJob(record);
        WateringJob.setTemperature(() -> 15.0);
        WateringJob.setTemperatureThreshold(() -> 5.0);
        WateringJob.setRaining(() -> false);
        WateringJob.setZones(() -> Sets.newHashSet("zone1", "zone2", "zone3"));
        WateringJob.setWatchPumpSupplier(thread -> {
        });
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        verify(commandExecutor, times(4)).accept(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
    }

    @Test
    public void testSuccessHardBreak() {
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setTimeInSeconds(5);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setZoneRefCode("zone1");
        record.setContinuous(false);
        WateringJob job = new WateringJob(record);
        WateringJob.setTemperature(() -> 15.0);
        WateringJob.setTemperatureThreshold(() -> 5.0);
        WateringJob.setRaining(() -> false);
        WateringJob.setZones(() -> Sets.newHashSet("zone1", "zone2", "zone3"));
        WateringJob.setWatchPumpSupplier(thread -> {
        });
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        verify(commandExecutor, times(6)).accept(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
    }


    @Test
    public void testRetry() throws Exception {
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setTimeInSeconds(5);
        record.setRetryHour(10);
        record.setRetryMinute(11);
        record.setZoneRefCode("zone1");
        record.setContinuous(false);
        WateringJob job = new WateringJob(record);
        WateringJob.setTemperature(() -> 15.0);
        WateringJob.setTemperatureThreshold(() -> 5.0);
        WateringJob.setRaining(() -> false);
        WateringJob.setZones(() -> Sets.newHashSet("zone1", "zone2", "zone3"));
        WateringJob.setWatchPumpSupplier(thread -> {
            WateringJob.setWatchPumpSupplier(thread1 -> {
            });
            thread.interrupt();
        });
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
    }

    public class CmdExecutor implements BiConsumer<String, Boolean> {
        @Override
        public void accept(String s, Boolean aBoolean) {

        }
    }
}
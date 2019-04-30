package org.dropco.smarthome.watering2;

import com.google.common.collect.Sets;
import org.dropco.smarthome.watering2.db.WateringRecord;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.function.BiConsumer;

public class WateringJobTest {

    @Test
    public void testUnderZero(){
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setContinuous(true);
        WateringJob job =new WateringJob(record);
        WateringJob.setTemperature(()->-5.0);
        WateringJob.setTemperatureThreshold(()->5.0);
        WateringJob.setRaining(()->false);
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = Mockito.spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        Mockito.verify(commandExecutor,Mockito.times(0));
    }

    @Test
    public void testRaining(){
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setContinuous(true);
        WateringJob job =new WateringJob(record);
        WateringJob.setTemperature(()->15.0);
        WateringJob.setRaining(()->true);
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = Mockito.spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        Mockito.verify(commandExecutor,Mockito.times(0));
    }

    @Test
    public void testSuccessContinuos(){
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setTimeInSeconds(5);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setZoneRefCode("zone1");
        record.setContinuous(true);
        WateringJob job =new WateringJob(record);
        WateringJob.setTemperature(()->15.0);
        WateringJob.setTemperatureThreshold(()->5.0);
        WateringJob.setRaining(()->false);
        WateringJob.setZones(()-> Sets.newHashSet("zone1","zone2","zone3"));
        WateringJob.setWatchPumpSupplier(thread -> {});
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = Mockito.spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        Mockito.verify(commandExecutor,Mockito.times(4)).accept(ArgumentMatchers.anyString(),ArgumentMatchers.anyBoolean());
    }
    @Test
    public void testSuccessHardBreak(){
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setTimeInSeconds(5);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setZoneRefCode("zone1");
        record.setContinuous(false);
        WateringJob job =new WateringJob(record);
        WateringJob.setTemperature(()->15.0);
        WateringJob.setTemperatureThreshold(()->5.0);
        WateringJob.setRaining(()->false);
        WateringJob.setZones(()-> Sets.newHashSet("zone1","zone2","zone3"));
        WateringJob.setWatchPumpSupplier(thread -> {});
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = Mockito.spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        Mockito.verify(commandExecutor,Mockito.times(6)).accept(ArgumentMatchers.anyString(),ArgumentMatchers.anyBoolean());
    }

    public class CmdExecutor implements BiConsumer<String, Boolean>{
        @Override
        public void accept(String s, Boolean aBoolean) {

        }
    }
}
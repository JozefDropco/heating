package org.dropco.smarthome.watering;

import com.google.common.collect.Sets;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.exception.InvalidPinException;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.watering.db.WateringRecord;
import org.junit.*;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;


public class WateringJobTest {
    public static final String WATERING_PUMP = "WATERING_PUMP";
    GpioPinDigitalInput input1,input2;

    private static final GpioProviderBase PROVIDER = new GpioProviderBase() {
        public void setState(Pin pin, PinState state) {
            if (!hasPin(pin)) {
                throw new InvalidPinException(pin);
            }

            GpioProviderPinCache pinCache = getPinCache(pin);

            // for digital output pins, we will echo the event feedback
            dispatchPinDigitalStateChangeEvent(pin, state);

            // cache pin state
            pinCache.setState(state);
        }

        @Override
        public String getName() {
            return "RaspberryPi GPIO Provider";
        }
    };

    @BeforeClass
    public static void init(){
        GpioFactory.setDefaultProvider(PROVIDER);
    }
    @Before
    public void before(){
        input1= GpioFactory.getInstance().provisionDigitalInputPin(RaspiPin.GPIO_06, "dsa");
        input2 = GpioFactory.getInstance().provisionDigitalInputPin(RaspiPin.GPIO_03, "wpf");

    }

    @After
    public void after(){
        GpioFactory.getInstance().unprovisionPin(input1,input2);

    }
    @Test
    public void testUnderZero() throws InterruptedException, IOException {
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setZoneRefCode("1");
        record.setTimeInSeconds(10);
        WateringThreadManager.stop();
        WateringJob.setWaterPumpPort(()-> "dd");
        WateringJob.setZones(()->Sets.newHashSet(new NamedPort("1","1"),new NamedPort("2","2"),new NamedPort("3","3")));
        RainSensor.start(input1);
        PROVIDER.setState(input1.getPin(), PinState.LOW);
        WaterPumpFeedback.start(input2);
        PROVIDER.setState(input2.getPin(), PinState.HIGH);
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        WateringThreadManager.water(record);

        Thread.sleep(5*1000);
        verify(commandExecutor, times(  4)).accept(anyString(), anyBoolean());
    }

    @Test
    public void testRaining() throws InterruptedException {
        WateringRecord record =
                new WateringRecord();
        record.setMinute(10);
        record.setHour(10);
        record.setRetryHour(11);
        record.setRetryMinute(10);
        record.setZoneRefCode("1");
        record.setTimeInSeconds(180);
        WateringJob.setWaterPumpPort(()-> "dd");
        WateringJob.setZones(()->Sets.newHashSet(new NamedPort("1","1"),new NamedPort("2","2"),new NamedPort("3","3")));
        RainSensor.start(input1);
        PROVIDER.setState(input1.getPin(), PinState.LOW);
        WaterPumpFeedback.start(input2);
        PROVIDER.setState(input2.getPin(), PinState.HIGH);
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);

        WateringJob.setCommandExecutor(commandExecutor);
        WateringThreadManager.water(record);
        Thread.sleep(5000);
        verify(commandExecutor, times(1)).accept(anyString(), ArgumentMatchers.eq(true));
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
        WateringJob job = new WateringJob(record);
        WateringJob.setWaterPumpPort(()-> "dd");
        WateringJob.setZones(()->Sets.newHashSet(new NamedPort("zone1","1"),new NamedPort("zone2","2"),new NamedPort("zone3","3")));
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        job.run();
        verify(commandExecutor, times(6)).accept(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean());
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
        WateringJob job = new WateringJob(record);
        WateringJob.setWaterPumpPort(()-> "dd");
        WateringJob.setZones(()->Sets.newHashSet(new NamedPort("zone1","1"),new NamedPort("zone2","2"),new NamedPort("zone3","3")));
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
        record.setTimeInSeconds(20);
        record.setRetryHour(10);
        record.setRetryMinute(11);
        record.setZoneRefCode("zone1");
        RainSensor.start(input1);
        PROVIDER.setState(input1.getPin(), PinState.LOW);
        WaterPumpFeedback.start(input2);
        PROVIDER.setState(input2.getPin(), PinState.HIGH);
        WateringJob.setWaterPumpPort(()-> "dd");
        WateringJob.setZones(()->Sets.newHashSet(new NamedPort("zone1","1"),new NamedPort("zone2","2"),new NamedPort("zone3","3")));
        BiConsumer<String, Boolean> cmdExecutor = new CmdExecutor();
        BiConsumer<String, Boolean> commandExecutor = spy(cmdExecutor);
        WateringJob.setCommandExecutor(commandExecutor);
        WateringThreadManager .water(record);
        Thread.sleep(5000);
        PROVIDER.setState(input2.getPin(), PinState.LOW);
        Thread.sleep(2000);
        Assert.assertTrue(20>record.getTimeInSeconds());
    }

    public class CmdExecutor implements BiConsumer<String, Boolean> {
        @Override
        public void accept(String s, Boolean aBoolean) {

        }
    }
}

package org.dropco.smarthome.heating;

import org.dropco.smarthome.database.SettingsDao;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

public class BoilerTest {
    @Test
    public void testWorkingDay() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        new CircularPump(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        BoilerBlocker boiler = spy(new BoilerBlocker(COMMAND_EXECUTOR));
        when(BoilerBlocker.BOILER_PORT_KEY).thenReturn("D1");
        when(boiler.getCurrentDate()).thenAnswer(mock -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_WEEK, -4);
            }
            return calendar;
        });

        when(boiler.getTime(anyInt(),anyInt())).thenAnswer(mock->{
            Date realMethod = (Date) mock.callRealMethod();
            Calendar instance = Calendar.getInstance();
            instance.setTime(realMethod);
            int day = instance.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                instance.add(Calendar.DAY_OF_WEEK, -4);
            }
            return instance.getTime();
        });
        Thread thread = new Thread(boiler);
        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(eq("D1"), eq(true));

    }

    @Test
    public void testWorkingDayWithout3Valve() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        new CircularPump(new SettingsDao(),COMMAND_EXECUTOR).raiseChange(false);
        new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        BoilerBlocker boiler = spy(new BoilerBlocker(COMMAND_EXECUTOR));
        when(BoilerBlocker.BOILER_PORT_KEY).thenReturn("D1");
        when(boiler.getCurrentDate()).thenAnswer(mock -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_WEEK, -4);
            }
            return calendar;
        });
        when(boiler.getTime(anyInt(),anyInt())).thenAnswer(mock->{
            Date realMethod = (Date) mock.callRealMethod();
            Calendar instance = Calendar.getInstance();
            instance.setTime(realMethod);
            int day = instance.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                instance.add(Calendar.DAY_OF_WEEK, -4);
            }
            return instance.getTime();
        });
        Thread thread = new Thread(boiler);
        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(eq("D1"), eq(true));

    }

    @Test
    public void testWorkingNight() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        new CircularPump(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        BoilerBlocker boiler = spy(new BoilerBlocker(COMMAND_EXECUTOR));
        when(BoilerBlocker.BOILER_PORT_KEY).thenReturn("D1");
        when(boiler.getCurrentDate()).thenAnswer(mock -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_WEEK, -4);
            }
            return calendar;
        });
        when(boiler.getTime(anyInt(),anyInt())).thenAnswer(mock->{
            Date realMethod = (Date) mock.callRealMethod();
            Calendar instance = Calendar.getInstance();
            instance.setTime(realMethod);
            int day = instance.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                instance.add(Calendar.DAY_OF_WEEK, -4);
            }
            return instance.getTime();
        });
        Thread thread = new Thread(boiler);
        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(eq("D1"), eq(true));

    }
    @Test
    public void testWeekendNight() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        new CircularPump(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        BoilerBlocker boiler = spy(new BoilerBlocker(COMMAND_EXECUTOR));
        when(BoilerBlocker.BOILER_PORT_KEY).thenReturn("D1");
        when(boiler.getCurrentDate()).thenAnswer(mock -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_WEEK, 7-day);
            }
            return calendar;
        });
        when(boiler.getTime(anyInt(),anyInt())).thenAnswer(mock->{
            Date realMethod = (Date) mock.callRealMethod();
            Calendar instance = Calendar.getInstance();
            instance.setTime(realMethod);
            int day = instance.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                instance.add(Calendar.DAY_OF_WEEK, 7-day);
            }
            return instance.getTime();
        });
        Thread thread = new Thread(boiler);
        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(eq("D1"), eq(true));
    }

    @Test
    public void testWeekendDay() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        new CircularPump(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        BoilerBlocker boiler = spy(new BoilerBlocker(COMMAND_EXECUTOR));
        when(BoilerBlocker.BOILER_PORT_KEY).thenReturn("D1");
        when(boiler.getCurrentDate()).thenAnswer(mock -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_WEEK, 7-day);
            }
            return calendar;
        });
        when(boiler.getTime(anyInt(),anyInt())).thenAnswer(mock->{
            Date realMethod = (Date) mock.callRealMethod();
            Calendar instance = Calendar.getInstance();
            instance.setTime(realMethod);
            int day = instance.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                instance.add(Calendar.DAY_OF_WEEK, 7-day);
            }
            return instance.getTime();
        });
        Thread thread = new Thread(boiler);
        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(eq("D1"), eq(true));
    }
    @Test
    public void testWeekendDayNotSunny() throws InterruptedException {
        BiConsumer COMMAND_EXECUTOR = mock(BiConsumer.class);
        new CircularPump(new SettingsDao(),COMMAND_EXECUTOR).setState(false);
        new ThreeWayValve(new SettingsDao(),COMMAND_EXECUTOR).setState(true);
        BoilerBlocker boiler = spy(new BoilerBlocker(COMMAND_EXECUTOR));
        BoilerBlocker.state.set(true);
        when(BoilerBlocker.BOILER_PORT_KEY).thenReturn("D1");
        when(boiler.getCurrentDate()).thenAnswer(mock -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                calendar.add(Calendar.DAY_OF_WEEK, 7-day);
            }
            return calendar;
        });
        when(boiler.getTime(anyInt(),anyInt())).thenAnswer(mock->{
            Date realMethod = (Date) mock.callRealMethod();
            Calendar instance = Calendar.getInstance();
            instance.setTime(realMethod);
            int day = instance.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                instance.add(Calendar.DAY_OF_WEEK, 7-day);
            }
            return instance.getTime();
        });
        Thread thread = new Thread(boiler);
        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
        verify(COMMAND_EXECUTOR, times(1)).accept(eq("D1"), eq(false));
    }
}

package org.dropco.smarthome;

import org.dropco.smarthome.database.LogDao;

import java.io.*;
import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {

    LogDao logDao = new LogDao();
    private Object writeLock = new Object();

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) return;
        Formatter formatter = getFormatter();
        String message = (formatter!=null)? formatter.formatMessage(record): record.getMessage();
            synchronized (writeLock) {
                logDao.addLogMessage(record.getSequenceNumber(),new Date(record.getMillis()),record.getLevel().getName(),message.substring(0,255));
            }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }


}


package org.dropco.smarthome;

import org.dropco.smarthome.database.LogDao;

import java.util.Date;
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
        String message = (formatter != null) ? formatter.formatMessage(record) : record.getMessage();
        if (message.length() > 256) {
            message = message.substring(0, 255);
        }
        synchronized (writeLock) {
            logDao.addLogMessage(record.getSequenceNumber(), new Date(record.getMillis()), record.getLevel().getName(), message);
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }


}


package com.a.eye.skywalking.api.logging;


import com.a.eye.skywalking.conf.Config;
import com.a.eye.skywalking.util.LoggingUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SyncFileWriter implements IWriter {

    private static SyncFileWriter   writer;
    private        FileOutputStream os;
    private        int              bufferSize;
    private static final Object lock = new Object();

    private SyncFileWriter() {
        try {
            File logFilePath = new File(Config.SkyWalking.AGENT_BASE_PATH, Config.Logging.LOG_DIR_NAME);
            if (!logFilePath.exists()) {
                logFilePath.mkdirs();
            }
            os = new FileOutputStream(new File(logFilePath, Config.Logging.LOG_FILE_NAME), true);
            bufferSize = Long.valueOf(new File(logFilePath, Config.Logging.LOG_FILE_NAME).length()).intValue();
        } catch (IOException e) {
            writeErrorLog(e);
        }
    }


    public static IWriter instance() {
        if (writer == null) {
            writer = new SyncFileWriter();
        }
        return writer;
    }


    public void write(String message) {
        writeLogRecord(message);
        switchLogFileIfNecessary();
    }

    @Override
    public void writeError(String message) {
        this.write(message);
    }

    private void writeLogRecord(String message) {
        try {
            os.write(message.getBytes());
            os.write("\n".getBytes());
            bufferSize += message.length();
        } catch (IOException e) {
            writeErrorLog(e);
        }
    }

    private void switchLogFileIfNecessary() {
        if (bufferSize > Config.Logging.MAX_LOG_FILE_LENGTH) {
            synchronized (lock) {
                if (bufferSize > Config.Logging.MAX_LOG_FILE_LENGTH) {
                    try {
                        closeInputStream();
                        renameLogFile();
                        revertInputStream();
                    } catch (IOException e) {
                        writeErrorLog(e);
                    }
                    bufferSize = 0;
                }
            }
        }
    }

    private void revertInputStream() throws FileNotFoundException {
        os = new FileOutputStream(new File(Config.Logging.LOG_DIR_NAME, Config.Logging.LOG_FILE_NAME), true);
    }

    private void renameLogFile() {
        new File(Config.Logging.LOG_DIR_NAME, Config.Logging.LOG_FILE_NAME)
                .renameTo(new File(Config.Logging.LOG_DIR_NAME, Config.Logging.LOG_FILE_NAME + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
    }

    private void closeInputStream() throws IOException {
        this.os.flush();
        this.os.close();
    }


    private void writeErrorLog(Throwable e) {
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(Config.Logging.LOG_DIR_NAME, Config.Logging.SYSTEM_ERROR_LOG_FILE_NAME);
            fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(("Failed to init sync File Writer.\n" + LoggingUtil.fetchThrowableStack(e)).getBytes());
        } catch (Exception e1) {
            System.err.print(LoggingUtil.fetchThrowableStack(e1));
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e1) {
                    System.err.print(LoggingUtil.fetchThrowableStack(e1));
                }
            }
        }
    }

}

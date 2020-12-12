package utility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationLogger {
    private final static Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static ApplicationLogger applicationLoggerObj;

    private ApplicationLogger() {}

    public static ApplicationLogger getInstance() {
        if (applicationLoggerObj == null) {
            applicationLoggerObj = new ApplicationLogger();
        }
        return applicationLoggerObj;
    }

    public void insterLog(String logMessage, Level levelObj) {
        LOGGER.log(levelObj, logMessage);
    }
}


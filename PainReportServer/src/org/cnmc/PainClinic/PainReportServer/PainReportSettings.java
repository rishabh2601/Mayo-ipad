package org.cnmc.PainClinic.PainReportServer;

import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class PainReportSettings {
    private static final String PROPERTY_FILENAME = "properties/painreport.properties";
    public static final Calendar PAINREPORT_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private static final long MS_ONE_WEEK_FROM_NOW = 1000L * 60L * 60L * 24L * 7L;
    private static final long MS_ONE_MINUTE = 1000L * 60L;

    private Logger PAINREPORT_LOGGER  = null;
    private Level PAINREPORT_LOGLEVEL = null;
    private Properties __globalProperties;
    private long __timeBetweenSurveys = MS_ONE_WEEK_FROM_NOW;

    // Singleton pattern. because sometimes we don't go through main
    private static PainReportSettings __appSettings;
    
    // We could get real clever and make sure our patient and devices are in the DB.
    // Now accepting on faith
    private PainReportSettings() throws Exception {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILENAME));
                    //new InputStreamReader(new FileInputStream(PROPERTY_FILENAME));
            __globalProperties = new Properties();
            __globalProperties.load(isr);
            
            // Now deal with the logger
            String globalLogLevel = __globalProperties.getProperty("log.level");
            String loggerScope = __globalProperties.getProperty("log.scope");
            if (loggerScope != null) {
                PAINREPORT_LOGGER = Logger.getLogger(loggerScope.trim());
            } else {
                PAINREPORT_LOGGER = Logger.getLogger("org.cnmc.PainClinic.PainReportServer");
            }
            if (globalLogLevel != null) {
                try {
                    PAINREPORT_LOGLEVEL = Level.parse(globalLogLevel.trim());
                } catch (IllegalArgumentException iae) {
                    // ok to swallow as we will use the default. Log it.
                    PAINREPORT_LOGLEVEL = Level.INFO;
                    PAINREPORT_LOGGER.log(Level.WARNING, "Unable to initialize loglevel " + globalLogLevel + 
                            ", using " + PAINREPORT_LOGLEVEL.toString());
                }
            }
            
            // now deal with log handlers.
            String logToConsole = __globalProperties.getProperty("log.logToConsole");
            if (logToConsole == null || logToConsole.trim().equals("") || logToConsole.equalsIgnoreCase("false")) {
                PAINREPORT_LOGGER.setUseParentHandlers(false);
            }
            
            String logFileName = __globalProperties.getProperty("painreport.log");
            if (logFileName != null && !logFileName.trim().equals("")) {
                SimpleDateFormat format = new SimpleDateFormat("E_MMddyy_HHmmss");
                logFileName = "logs" + File.separator + logFileName + "." + format.format(new Date()) + ".log";
                FileHandler fhandler = new FileHandler(logFileName);
                SimpleFormatter sformatter = new SimpleFormatter();
                fhandler.setFormatter(sformatter);
                PAINREPORT_LOGGER.addHandler(fhandler);
            }

            // custom props we know about
            // this one controls the amount of time in minutes between surveys for a given user
            String timeSurveys = __globalProperties.getProperty("painreport.time_between_surveys");
            try {
                __timeBetweenSurveys = Long.parseLong(timeSurveys) * MS_ONE_MINUTE;
            } catch (NumberFormatException nfe) {
                System.out.println("Unable to parse time between surveys properties, sticking with default 1 week");
                __timeBetweenSurveys = MS_ONE_WEEK_FROM_NOW;
            }
        } catch (Throwable t1) {            
            System.out.println("Unable to initialize PainReport Settings");
            t1.printStackTrace();
        } finally {
            try {
                isr.close();
            } catch (Throwable t) {
            }
        }
    }
    
    public static PainReportSettings getPainReportSettings() {
        if (__appSettings == null) {
            try {
                __appSettings = new PainReportSettings();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return __appSettings;
    }

    public static long getTimeBetweenSurveys() {
        return PainReportSettings.getPainReportSettings().getTimeBetweenSurveysInMinutes();
    }

    public static Calendar getPainReportCalendar() {
        return PAINREPORT_CALENDAR;
    }

    public static Logger getPainReportLogger() {
        return PainReportSettings.getPainReportSettings().getLogger();
    }

    public static Level getPainReportLogLevel() {
        return PainReportSettings.getPainReportSettings().getLogLevel();
    }

    public static String getPainReportProperty(String key) {
        return PainReportSettings.getPainReportSettings().getGlobalProperty(key);
    }

    public static void log(Level l, String s) {
        PainReportSettings.getPainReportLogger().log(l, s);
    }

    public static void log(Level l, String s, Object o) {
        PainReportSettings.getPainReportLogger().log(l, s, o);
    }

    public static void log(Level l, String s, Object[] objs) {
        PainReportSettings.getPainReportLogger().log(l, s, objs);
    }

    public static void log(Level l, String s, Throwable t) {
        PainReportSettings.getPainReportLogger().log(l, s, t);
    }

    public String getGlobalProperty(String key) {
        return __globalProperties.getProperty(key);
    }

    public Logger getLogger() {
        return PAINREPORT_LOGGER;
    }

    public Level getLogLevel() {
        return PAINREPORT_LOGLEVEL;
    }

    public long getTimeBetweenSurveysInMinutes() {
        return __timeBetweenSurveys;
    }
}

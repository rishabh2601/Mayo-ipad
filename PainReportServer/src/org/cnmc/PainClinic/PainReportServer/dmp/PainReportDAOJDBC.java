/**
 *
 */
package org.cnmc.PainClinic.PainReportServer.dmp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cnmc.PainClinic.PainReportServer.PainReportSettings;
import org.cnmc.PainClinic.PainReportServer.domain.Patient;
import org.cnmc.PainClinic.PainReportServer.domain.SurveyResultAnswer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author kevinagary
 *
 */
public class PainReportDAOJDBC implements IPainReportDAO {

    private static final Logger LOGGER = PainReportSettings.getPainReportLogger();
    private static final String CLASS  = "PainReportDAOJDBC";

    protected String _jdbcURL;
    protected Properties _sqlProperties;
    protected String _jdbcDriver;
    protected String _jdbcUser;
    protected String _jdbcPasswd;
    protected String _dbVendor;

    public PainReportDAOJDBC() {
    }

    /* (non-Javadoc)
     */
    @Override
    public void init(Properties p) throws DMPException {
        _sqlProperties = new Properties();
        _jdbcDriver = p.getProperty("jdbc.driver");
        _jdbcURL    = p.getProperty("jdbc.url");
        _jdbcUser   = p.getProperty("jdbc.user");
        _jdbcPasswd = p.getProperty("jdbc.passwd");
        _dbVendor   = p.getProperty("jdbc.db");

        if (_jdbcDriver == null || _jdbcURL == null) {
            throw new DMPException("JDBC not configured");
        }
        // load the driver, test the URL
        try {
            // In case we need to modify system properties for Derby
            Properties sysProps = System.getProperties();

            _sqlProperties.setProperty("jdbc.driver", _jdbcDriver);
            _sqlProperties.setProperty("jdbc.url", _jdbcURL);
            _sqlProperties.setProperty("jdbc.user", _jdbcUser);
            _sqlProperties.setProperty("jdbc.passwd", _jdbcPasswd);

            if (_dbVendor != null && _dbVendor.length() > 0) {
                // read in all the db-specific properties and SQL queries we need
                Enumeration<?> keys = p.keys();
                while (keys.hasMoreElements()) {
                    String key = (String)keys.nextElement();
                    if (key.startsWith("sql")) {
                        LOGGER.log(Level.FINEST, CLASS, "found property with key, value\t" + key + ", " + p.getProperty(key));
                        _sqlProperties.setProperty(key, p.getProperty(key));
                    } else if (key.startsWith(_dbVendor)) {
                        System.out.println("found " + _dbVendor + " system property with key, value\t" + key + ", " + p.getProperty(key));
                        LOGGER.log(Level.FINEST, CLASS, "found " + _dbVendor + " system property with key, value\t" + key + ", " + p.getProperty(key));
                        sysProps.setProperty(key, p.getProperty(key));
                    }
                }
            }

            // Now test the connection
            Class.forName(_jdbcDriver);
            // test the connection
            if (!__testConnection(_jdbcURL, p.getProperty(_dbVendor + ".checkConnectionQuery"))) {
                LOGGER.log(Level.SEVERE, "TEST CONNECTION FAILED " + p.getProperty(_dbVendor + ".checkConnectionQuery"));
                throw new DMPException("Unable to connect to database");
            } else {
                LOGGER.log(Level.FINEST, "Testing DAO Connection -- OK");
            }
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, CLASS, "Initializing DAO", "Throwable");
            throw new DMPException(t);
        }
    }

    public boolean validatePatient(String pid) throws DMPException {
        return validatePatient(pid, new Date());
    }
    public boolean validatePatient(String pid, Date d) throws DMPException {
        Connection c = null;
        try {
            c = _getConnection();
            return __validatePatient(c, pid, d);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, CLASS, "Validating Patient", "Exception");
            throw new DMPException(t);
        } finally {
            try {
                if (c != null)
                    c.close();
            } catch (SQLException se2) {
                LOGGER.logp(Level.FINE, CLASS, "validatePatient",
                        "SQL Close Error in finally");
            }
        }
    }

    public List<Patient> getPatients() throws DMPException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Patient> pList = new ArrayList<Patient>();
        try {
            c = _getConnection();
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.getPatients"));
            rs = ps.executeQuery();
            while (rs.next()) {
                pList.add(new Patient(rs.getString(1), rs.getInt(2), rs.getTimestamp(3), rs.getTimestamp(4),
                                      rs.getBoolean(5), rs.getString(6),rs.getTimestamp(7),rs.getBoolean(8)));
            }
            return pList;
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getPatients", se.getMessage());
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getPatients", t.getMessage());
            throw new DMPException(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null)  c.close();
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Unable to close database resources in getPatients");
            }
        }
    }
    public List<Patient> getPatients(int lb, int ub) throws DMPException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<Patient> pList = new ArrayList<Patient>();
        try {
            c = _getConnection();
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.getPatientsRange"));
	    ps.setInt(1, lb);
	    ps.setInt(2, ub);
            rs = ps.executeQuery();
            while (rs.next()) {
                pList.add(new Patient(rs.getString(1), rs.getInt(2), rs.getTimestamp(3), rs.getTimestamp(4),
                                      rs.getBoolean(5), rs.getString(6),rs.getTimestamp(7),rs.getBoolean(8)));
            }
            return pList;
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getPatients", se.getMessage());
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getPatients", t.getMessage());
            throw new DMPException(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null)  c.close();
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Unable to close database resources in getPatients");
            }
        }
    }


    public Patient getPatient(String pid) throws DMPException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Patient p = null;
        try {
            c = _getConnection();
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.getPatient"));
            ps.setString(1, pid);
            rs = ps.executeQuery();
            if (rs.next()) {
                p = new Patient(rs.getString(1), rs.getInt(2), rs.getTimestamp(3), rs.getTimestamp(4),
                                rs.getBoolean(5), rs.getString(6),rs.getTimestamp(7),rs.getBoolean(8));
            }
            return p;
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getPatient", se.getMessage());
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getPatient", t.getMessage());
            throw new DMPException(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null)  c.close();
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Unable to close database resources in getPatient");
            }
        }
    }

    public String getNewPIN() throws DMPException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = _getConnection();
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.getUniqueId"));
            rs = ps.executeQuery();
            if (rs.next()) {
                String s = rs.getString(1);
                while (s.length() < 4) {
                    s = s + s;
                }
                if (s.length() > 4) {
                    s = s.substring(0,3);
                    LOGGER.log(Level.INFO, "Generated new PIN " + s);
                }
                return s;
            }
            return null;
        } catch (SQLException se) {
            se.printStackTrace();
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getNewPIN", se.getMessage());
            throw new DMPException(se);
        } catch (Throwable t) {
            t.printStackTrace();
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getNewPIN", t.getMessage());
            throw new DMPException(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null)  c.close();
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Unable to close database resources in gewNewPIN");
            }
        }
    }

    public boolean toggleCompleted(String pid) throws DMPException {
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = _getConnection();
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.toggleCompleted"));
            ps.setString(1, pid);
            ps.executeUpdate();
            return true;
        } catch (SQLException se) {
            se.printStackTrace();
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "toggleCompleted", se.getMessage());
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "toggleCompleted", t.getMessage());
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null)  c.close();
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Unable to close database resources in toggleCompleted");
            }
        }
    }

    public List<SurveyResultAnswer> getSurveyResultAnswers(String pin) throws DMPException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<SurveyResultAnswer> answers = new ArrayList<SurveyResultAnswer>();
        try {
            c = _getConnection();
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.getSurveyAnswersForPatient"));
            ps.setString(1, pin);
            rs = ps.executeQuery();
            while (rs.next()) {
                answers.add(new SurveyResultAnswer(pin,rs.getTimestamp(3),rs.getString(2),rs.getString(1),rs.getString(4)));
            }
            return answers;
            //  SurveyResultAnswer(String pid, Date d, String acid, String qid, String v)
            //select QUESTION_ID as QID,ANSWER_COMPONENT_ID as ACID,DATE_TIME,VALUE
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getSurveyResultAnswers", se.getMessage());
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "getSurveyResultAnswers", t.getMessage());
            throw new DMPException(t);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (c != null)  c.close();
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Unable to close database resources in getSurveyResultAnswers");
            }
        }
    }

    public boolean addPatient(Patient p) throws DMPException {
        Connection c = null;
        PreparedStatement ps = null;
        Date d = null;
        try {
            c = _getConnection();
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.addPatient"));
            ps.setString(1, p.getPin());
            d = p.getNextReading();
            if (d != null)
                ps.setTimestamp(2, new Timestamp(d.getTime()));
            else
                ps.setNull(2, Types.TIMESTAMP);

            ps.setBoolean(3, p.isAndroid());
            ps.setString(4, p.getOSVersion());

            d = p.getDateStarted();
            if (d != null)
                ps.setTimestamp(5, new Timestamp(d.getTime()));
            else
                ps.setNull(5, Types.TIMESTAMP);

            ps.setBoolean(6, p.isCompleted());
            ps.executeUpdate();
            return true;
        } catch (SQLException se) {
            // presume this is a duplicate key
            LOGGER.log(Level.WARNING, "Failed to add patient, probably duplicate key, returning false");
            return false;
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, "org.cnmc.PainClinic.PainReport.dmp.PainReportDAOJDBC", "addPatient", t.getMessage());
            throw new DMPException(t);
        } finally {
            try {
                if (ps != null) ps.close();
                if (c != null)  c.close();
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Unable to close database resources in addPatient");
            }
        }
    }
    // Saves all the answers of a pain report survey as one transaction
    public boolean saveAnswers(String pid, Map<String,String> answers,
                               Date dateProcessed) throws DMPException {
        Connection c = null;
        PreparedStatement pssr  = null;
        PreparedStatement psans = null;
        Timestamp dpTime = new Timestamp(dateProcessed.getTime());
        JSONParser parser = new JSONParser();
        try {
            c = _getConnection();

            if (__validatePatient(c, pid, dateProcessed)) {
                // First, survey_result table
                pssr  = c.prepareStatement(_sqlProperties.getProperty("sql.insertSurveyResult"));
                pssr.setString(1,  pid);
                pssr.setTimestamp(2, dpTime, PainReportSettings.PAINREPORT_CALENDAR);
                pssr.executeUpdate();

                // Next, survey_result_answer
                psans = c.prepareStatement(_sqlProperties.getProperty("sql.insertSurveyResultAnswer"));
                for (String key: answers.keySet()) {
                    String jsonValue = answers.get(key);
                    LOGGER.log(Level.INFO, key + " question storing value " + jsonValue);
                    if (jsonValue != null) {
                        JSONObject obj = (JSONObject)parser.parse(jsonValue);
                        for (Object jkey : obj.keySet()) {
                            psans.setString(3, pid);
                            psans.setTimestamp(4, dpTime, PainReportSettings.PAINREPORT_CALENDAR);
                            psans.setString(1, (String)jkey);
                            psans.setString(2, key);
                            psans.setString(5, obj.get(jkey).toString());
                            psans.executeUpdate();
                            psans.clearParameters();
                        }
                    }
                }
            }
            c.commit();
            return true;
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, CLASS, "Saving Answers", "SQLException");
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, CLASS, "Saving Answers", "Throwable");
            throw new DMPException(t);
        } finally {
            try {
                if (pssr != null)
                    pssr.close();
                if (psans != null)
                    psans.close();
                if (c != null) {
                    c.rollback();
                    c.close();
                }
            } catch (SQLException se2) {
                LOGGER.logp(Level.FINE, CLASS, "saveAnswers",
                        "SQL Close Error in finally");
            }
        }
    }

    private Date __getNextReading(Connection c, String pid) throws DMPException {
        PreparedStatement ps  = null;
        ResultSet rs = null;
        Timestamp dpTime = null;
        try {
            ps = c.prepareStatement(_sqlProperties.getProperty("sql.getNextReading"));
            ps.setString(1, pid);
            rs = ps.executeQuery();
            if (rs.next()) {
                dpTime = rs.getTimestamp(1);
                if (dpTime != null) {
                    return new Date(dpTime.getTime());
                }
            }
            // no next reading found, return now plus value from settings
            return new Date((new Date()).getTime() + PainReportSettings.getTimeBetweenSurveys());
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, CLASS, "__getNextReading", "SQLException");
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, CLASS, "__getNextReading", "Throwable");
            throw new DMPException(t);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
            } catch (SQLException se2) {
                LOGGER.logp(Level.FINE, CLASS, "__getNextReading",
                        "SQL Close Error in finally");
            }
        }
    }

    public Date getNextReading(String pid) throws DMPException {
        Connection c = null;

        try {
            c = _getConnection();
            return __getNextReading(c, pid);
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, CLASS, "getNextReading", "SQLException");
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, CLASS, "getNextReading", "Throwable");
            throw new DMPException(t);
        } finally {
            try {
                if (c != null) {
                    c.rollback();
                    c.close();
                }
            } catch (SQLException se2) {
                LOGGER.logp(Level.FINE, CLASS, "saveAnswers",
                        "SQL Close Error in finally");
            }
        }
    }

    public Date setNextReading(String pid) throws DMPException {
        Connection c = null;
        PreparedStatement psu = null;
        try {
            c = _getConnection();
            Date dp = new Date(); // __getNextReading(c, pid);
            dp.setTime(System.currentTimeMillis() + PainReportSettings.getTimeBetweenSurveys());
            psu = c.prepareStatement(_sqlProperties.getProperty("sql.updateNextReading"));
            psu.setString(2, pid);
            psu.setTimestamp(1, new Timestamp(dp.getTime()));
            psu.executeUpdate();
            c.commit();
            return dp;
        } catch (SQLException se) {
            LOGGER.logp(Level.SEVERE, CLASS, "setNextReading", "SQLException");
            throw new DMPException(se);
        } catch (Throwable t) {
            LOGGER.logp(Level.SEVERE, CLASS, "setNextReading", "Throwable");
            throw new DMPException(t);
        } finally {
            try {
                if (psu != null)
                    psu.close();
                if (c != null) {
                    c.rollback();
                    c.close();
                }
            } catch (SQLException se2) {
                LOGGER.logp(Level.FINE, CLASS, "saveAnswers",
                        "SQL Close Error in finally");
            }
        }
    }

    /**
     * Shadow method so we can do inside of a given transaction
     * @param c
     * @param pin
     * @return
     * @throws DMPException
     */
    private boolean __validatePatient(Connection c, String pin, Date d)
            throws DMPException {
        if (c == null || pin == null || pin.length() != 4) {
            return false;
        }
        try {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = c.prepareStatement(_sqlProperties.getProperty("sql.validatePin"));
                ps.setString(1, pin);
                ps.setTimestamp(2, new Timestamp(d.getTime()));
                rs = ps.executeQuery();
                return rs.next();
            } catch (Exception exc) {
                LOGGER.logp(Level.SEVERE, CLASS, "Validating PIN", "Exception");
                throw new DMPException(exc);
            } finally {
                try {
                    if (rs != null)
                        rs.close();
                    if (ps != null)
                        ps.close();
                } catch (SQLException se2) {
                    LOGGER.logp(Level.FINE, CLASS, "validatePatient",
                            "SQL Close Error in finally");
                }
            }
        } catch (Throwable t) {
            throw new DMPException(t);
        }
    }

    private boolean __testConnection(String url, String query) {
        Connection c = null;
        Statement s = null;
        try {
            c = _getConnection();
            System.out.println("\t__testConnection got connection");
            s = c.createStatement();
            System.out.println("\t__testConnection: Created the statement");
            return s.execute(query);
        } catch (Throwable t) {
            System.out.println("\t__testConnection: threw exception");
            t.printStackTrace();
            return false;
        } finally {
            try {
                if (s != null) s.close();
                if (c != null) c.close();
            } catch (SQLException se) {
                LOGGER.logp(Level.SEVERE, CLASS, "__testConnection", "SQL Error");
            }
        }
    }

    protected Connection _getConnection() throws SQLException {
        Connection c = null;
        if (_jdbcUser == null || _jdbcPasswd == null) {
            c = DriverManager.getConnection(_jdbcURL);
        } else {
            c = DriverManager.getConnection(_jdbcURL, _jdbcUser, _jdbcPasswd);
        }
        if (c != null) {
            c.setAutoCommit(false);
        }
        return c;
    }
}
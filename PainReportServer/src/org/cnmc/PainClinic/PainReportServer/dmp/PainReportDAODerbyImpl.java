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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
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
public class PainReportDAODerbyImpl implements IPainReportDAO {

    private static final Logger LOGGER = PainReportSettings.getPainReportLogger();
    private static final String CLASS  = "PainReportDAODerbyImpl";

    private String __jdbcURL;
    private Properties __derbyProperties;
    
    public PainReportDAODerbyImpl() {
    }

    /* (non-Javadoc)
     * @see edu.asupoly.aspira.dmp.AspiraDAOBaseImpl#init(java.util.Properties)
     */
    @Override
    public void init(Properties p) throws DMPException {
        __derbyProperties = new Properties();
        String jdbcDriver = p.getProperty("jdbc.driver");
        String jdbcURL    = p.getProperty("jdbc.url");
        // do we need user and password?
        
        if (jdbcDriver == null || jdbcURL == null) {
            throw new DMPException("JDBC not configured");
        }
        // load the driver, test the URL
        try {
            // In case we need to modify system properties for Derby
            Properties sysProps = System.getProperties();
            
            // read in all the derby properties and SQL queries we need  
            Enumeration<?> keys = p.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                if (key.startsWith("sql")) {
                    LOGGER.log(Level.FINEST, CLASS, "found property with key, value\t" + key + ", " + p.getProperty(key));                    
                    __derbyProperties.setProperty(key, p.getProperty(key));
                } else if (key.startsWith("derby")) {
                    LOGGER.log(Level.FINEST, CLASS, "found derby system property with key, value\t" + key + ", " + p.getProperty(key)); 
                    sysProps.setProperty(key, p.getProperty(key));
                }
            }
            
            Class.forName(jdbcDriver);
            // test the connection
            if (!__testConnection(jdbcURL, p.getProperty("sql.checkConnectionQuery"))) {
            		System.out.println("TEST CONNECTION FAILED " + p.getProperty("sql.checkConnectionQuery"));
                throw new DMPException("Unable to connect to database");
            } else {
                LOGGER.log(Level.FINEST, "Testing DAO Connection -- OK");
            }
            __jdbcURL = jdbcURL;
            __derbyProperties.setProperty("jdbc.driver", jdbcDriver);
            __derbyProperties.setProperty("jdbc.url", jdbcURL);
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
			c = DriverManager.getConnection(__jdbcURL);
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
	
	// Saves all the answers of a pain report survey as one transaction
	public boolean saveAnswers(String pid, Map<String,String> answers, 
							   Date dateProcessed) throws DMPException {
		Connection c = null;
		PreparedStatement pssr  = null;
		PreparedStatement psans = null;
		Timestamp dpTime = new Timestamp(dateProcessed.getTime());
		JSONParser parser = new JSONParser();
		try {
			c = DriverManager.getConnection(__jdbcURL);
			
			if (__validatePatient(c, pid, dateProcessed)) {
				// First, survey_result table
				pssr  = c.prepareStatement(__derbyProperties.getProperty("sql.insertSurveyResult"));
				pssr.setString(1,  pid);
				pssr.setTimestamp(2, dpTime, PainReportSettings.PAINREPORT_CALENDAR);
				pssr.executeUpdate();
				
				// Next, survey_result_answer
				psans = c.prepareStatement(__derbyProperties.getProperty("sql.insertSurveyResultAnswer"));
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
			ps = c.prepareStatement(__derbyProperties.getProperty("sql.getNextReading"));
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
			c = DriverManager.getConnection(__jdbcURL);
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
			c = DriverManager.getConnection(__jdbcURL);
			Date dp = __getNextReading(c, pid);
			dp.setTime(dp.getTime() + PainReportSettings.getTimeBetweenSurveys());
			psu = c.prepareStatement(__derbyProperties.getProperty("sql.updateNextReading"));
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
				ps = c.prepareStatement(__derbyProperties.getProperty("sql.validatePin"));
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
	
    private static boolean __testConnection(String url, String query) {
        Connection c = null;
        Statement s = null;
        try {
            c = DriverManager.getConnection(url);
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

	@Override
	public List<Patient> getPatients() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	    public List<Patient> getPatients(int lb, int ub) throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Patient getPatient(String pid) throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addPatient(Patient p) throws DMPException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean toggleCompleted(String pid) throws DMPException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getNewPIN() throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SurveyResultAnswer> getSurveyResultAnswers(String pin)
			throws DMPException {
		// TODO Auto-generated method stub
		return null;
	}

}
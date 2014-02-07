/**
 * 
 */
package org.cnmc.PainClinic.PainReportServer.dmp;

import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kevinagary
 *
 */
public final class PainReportDAOFactory  {
    
    private static String PROPERTY_FILENAME = "properties/dao.properties";
    private static String DAO_CLASS_PROPERTY_KEY = "daoClassName";

    private static final Logger LOGGER = Logger.getLogger(PainReportDAOFactory.class.getName());
    
    private static IPainReportDAO    __dao = null;
    private static Properties        __daoProperties = null;
    
    private PainReportDAOFactory() {
    		// We do not want this factory instantiated
    }
    
    /**
     * Singleton accessor
     * @return
     * @throws DMPException
     */
    public static IPainReportDAO getDAO() throws DMPException {
    		if (__dao != null) {
    			return __dao;
    		}
        __daoProperties = new Properties();
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(PainReportDAOFactory.class.getClassLoader().getResourceAsStream(PROPERTY_FILENAME));
            //InputStreamReader isr = new InputStreamReader(new FileInputStream(PROPERTY_FILENAME));
            __daoProperties.load(isr);
            // let's create a DAO based on a known property
            String daoClassName = __daoProperties.getProperty(DAO_CLASS_PROPERTY_KEY);
            Class<?> daoClass = Class.forName(daoClassName);
            __dao = (IPainReportDAO)daoClass.newInstance();
            __dao.init(__daoProperties);
        } catch (Throwable t1) {
            LOGGER.log(Level.SEVERE, "Throwable in constructor for PainReportDAO");
            //t1.printStackTrace();
            throw new DMPException(t1);
        } try {
            if (isr != null) isr.close();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Unable to close input dao property stream");
        }
        return __dao;
    }
}

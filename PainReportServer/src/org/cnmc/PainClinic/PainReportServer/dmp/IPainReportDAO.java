/**
 * 
 */
package org.cnmc.PainClinic.PainReportServer.dmp;

import org.cnmc.PainClinic.PainReportServer.domain.Patient;
import org.cnmc.PainClinic.PainReportServer.domain.SurveyResultAnswer;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Properties;

/**
 * @author kevinagary
 * This interface defines how we will work with persistent storage
 */
public interface IPainReportDAO {
    public void init(Properties p) throws DMPException;

    public boolean validatePatient(String pid) throws DMPException;
    public boolean validatePatient(String pid, Date d) throws DMPException;
    public boolean saveAnswers(String pid, Map<String, String> answers,
			       Date dateProcessed) throws DMPException;
    public Date getNextReading(String pid) throws DMPException;
    public Date setNextReading(String pid) throws DMPException;
    public List<Patient> getPatients() throws DMPException;
    public List<Patient> getPatients(int lb, int ub) throws DMPException;
    public Patient getPatient(String pid) throws DMPException;
    public boolean addPatient(Patient p) throws DMPException;
    public boolean toggleCompleted(String pid) throws DMPException;
    public String getNewPIN() throws DMPException;
    public List<SurveyResultAnswer> getSurveyResultAnswers(String pin) throws DMPException;
}

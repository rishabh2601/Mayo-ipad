package org.cnmc.PainClinic.PainReportServer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnmc.PainClinic.PainReportServer.domain.Patient;
import org.cnmc.PainClinic.PainReportServer.PainReportSettings;
import org.cnmc.PainClinic.PainReportServer.dmp.PainReportDAOFactory;
import org.cnmc.PainClinic.PainReportServer.dmp.DMPException;
import org.cnmc.PainClinic.PainReportServer.dmp.IPainReportDAO;

@SuppressWarnings("serial")
public class PainReportDisplayServlet extends HttpServlet {
    private static Logger LOGGER = PainReportSettings.getPainReportLogger();
    private static String __PREAMBLE = "<HTML><HEAD><TITLE>CNMC PainReport Display</TITLE></HEAD><BODY>\n";
    /**
     * doGet 
     */
    public final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = null;
        StringBuffer sb = new StringBuffer(__PREAMBLE);
        try {
	    // check for params
	    String lbStr = request.getParameter("lb");
	    String ubStr = request.getParameter("ub");
	    int lb = -1;
	    int ub = -1;
	    if (lbStr != null && ubStr != null) {
		try {
		    lb = Integer.parseInt(lbStr);
		    ub = Integer.parseInt(ubStr);
		} catch (NumberFormatException nfe) {
		    lb = -1;
		    ub = -1;
		}
	    }

            response.setContentType("text/html");
            out = response.getWriter();          
            // the logic to get the next reading time here
            IPainReportDAO theDAO = PainReportDAOFactory.getDAO();
            List<Patient> allPatients = null;
	    if (lb == -1 || ub == -1) {
		allPatients = theDAO.getPatients();
	    } else {
		allPatients = theDAO.getPatients(lb, ub);
	    }
            if (allPatients == null || allPatients.isEmpty()) {
                sb.append("<p>No patients to display</p>");
            } else {
                sb.append("<h3>Patients in Database:</h3><br/>");
                sb.append("\n<FORM METHOD=\"POST\" ACTION=\"praction\">");
                sb.append("<br/><table border=\"1\" cellpadding=\"5\">\n");
                sb.append("<tr><th></th><th>NUM</th><th>PIN</th><th>LAST SURVEY</th><th>NEXT READING</th>");
                sb.append("<th>ANDROID?</th><th>VER</th><th>STARTED</th><th>DONE?</th></tr>");

                for (Patient p : allPatients) {
                    sb.append("<tr>");
                    int c = p.getCountSurveyResults();
                    if (c > 0) {
                        sb.append("<td><input type=\"checkbox\" name=\"expin\" value=\""+p.getPin()+"\"/></td>");
                        sb.append("\n<td><a href=\"praction?action=displaysurveys&PIN="+p.getPin()+"\">"+c+"</a></td>");
                    } else {
                        sb.append("<td></td><td>"+c+"</td>");
                    }
                    sb.append("<td>"+p.getPin()+"</td><td>"+p.getLastSurveyResult()+"</td><td>");
                    sb.append(p.getNextReading()+"</td><td>"+p.isAndroid()+"</td><td>");
                    sb.append(p.getOSVersion()+"</td><td>"+p.getDateStarted()+"</td><td>");
                    sb.append("<a href=\"praction?action=toggle&PIN="+p.getPin()+"\">"+p.isCompleted()+"</a></td></tr>\n");
                }
                sb.append("</table>\n<br/>\n<input type=\"submit\" value=\"Export\">\n</FORM>");
            }
            sb.append("Complete below if you want to add a new Patient<br/>");
            sb.append("<p><FORM METHOD=\"POST\" ACTION=\"prdisplay\">\n");
            sb.append("Android? Check if Yes: <input type=\"checkbox\" name=\"android\" value=\"yes\"><br/>");
            sb.append("OS Version: <input type=\"text\" name=\"osversion\"><br/>");
            sb.append("<input type=\"submit\" value=\"Add Patient\"><br/></FORM>\n");
            out.println(sb.toString());
        } catch (DMPException dmpe) {
        		dmpe.printStackTrace();
        		LOGGER.log(Level.SEVERE, "Unable to construct next reading");
        }
        	catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Could not flush and close output stream on doGet");
            }
        }
    }

    /**
     * Handle upload of survey responses
     *
     * @param request HTTP Request object
     * @param response HTTP Response object
     *
     * @throws ServletException
     * @throws java.io.IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	IPainReportDAO theDAO = null;
        try {
        		theDAO = PainReportDAOFactory.getDAO();
        		
        		// process the params in the request
            String paramAndroid   = request.getParameter("android");
            String paramOSVersion = request.getParameter("osversion");
            boolean isAndroid = (paramAndroid != null && paramAndroid.equals("yes"));

            // Now let's get a new PIN
       		String pin = theDAO.getNewPIN();

            // Now create and save a Patient
            Patient p = new Patient(pin, isAndroid, paramOSVersion);

            // We'll try 10 times to see if we get a unique PIN
            int i = 0;
            while (i < 10 && !theDAO.addPatient(p)) {
                pin = theDAO.getNewPIN();
                p = new Patient(pin, isAndroid, paramOSVersion);
                i++;
            }
            if (i < 10) {
                LOGGER.log(Level.INFO, "Created Patient with PIN " + pin + " after " + i + " tries");
            } else {
                LOGGER.log(Level.INFO, "Could not create Patient after " + i + " tries");
            }
            doGet(request, response);
        } catch (Throwable t3) {
            LOGGER.log(Level.SEVERE, "Server pushed stacktrace on response: " + t3.getMessage());
            t3.printStackTrace();
        }
    }
}


package org.cnmc.PainClinic.PainReportServer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnmc.PainClinic.PainReportServer.PainReportSettings;
import org.cnmc.PainClinic.PainReportServer.dmp.PainReportDAOFactory;
import org.cnmc.PainClinic.PainReportServer.dmp.DMPException;
import org.cnmc.PainClinic.PainReportServer.dmp.IPainReportDAO;

@SuppressWarnings("serial")
public class PainReportImportServlet extends HttpServlet {
    private static Logger LOGGER = PainReportSettings.getPainReportLogger();

    /**
     * doGet 
     */
    public final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = null;
        try {
            response.setContentType("text/plain");
            out = response.getWriter();
            // the logic to get the next reading time here
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss z");
            //df.setTimeZone(TimeZone.getTimeZone("GMT"));
            String pin = request.getParameter("PIN");
            IPainReportDAO theDAO = PainReportDAOFactory.getDAO();
            Date theNextReading = theDAO.getNextReading(pin);
            out.println(theNextReading.getTime() + " " + df.format(theNextReading));
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
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int appReturnValue = HttpServletResponse.SC_OK;
        IPainReportDAO theDAO = null;
        Date dateProcessed = new Date();
        PrintWriter pw = null;
        try {
            String responseMsg = "Responses saved, you may exit the app";
            theDAO = PainReportDAOFactory.getDAO();

            // process the params in the request and return
            // first let's check the PIN
            String pin = request.getParameter("PIN");
            Map<String,String> answers = new HashMap<String, String>();
            if (!theDAO.validatePatient(pin)) {
                responseMsg = "Invalid PIN or next reading not due";
            } else {
                // save all the answers
                Map<String, String[]> params   = request.getParameterMap();
                for (String key: params.keySet()) {
                    if (key.endsWith("Ans")) {
                        String[] values = params.get(key);
                        if (values != null && values.length == 1) {
                            answers.put(key.substring(0, key.length()-3), values[0]);
                        }
                    }
                }
                if (theDAO.saveAnswers(pin, answers, dateProcessed)) {
                    // Move the nextReading ahead
                    theDAO.setNextReading(pin);  // what if this fails?
                } else {
                    responseMsg = "Unable to save responses, please try again";
                }
            }

            LOGGER.log(Level.INFO,"Server returning value, message: " + appReturnValue + " " + responseMsg);
            response.setStatus(appReturnValue);
            response.setContentType("text/plain");
            //responseMsg += "<p><input type=\"button\" value=\"Go Back\" onclick=\"__blhelper.goBack()\"/></p>";
            //responseMsg += "<p><input type=\"button\" value=\"Close\" onclick=\"__blhelper.finishMe()\"/></p>";
            pw = response.getWriter();
            pw.println(responseMsg.toString());
        }
        catch (IOException ie) {
            LOGGER.log(Level.SEVERE, "IOException on import: " + ie.getMessage());
            ie.printStackTrace();
        } catch (Throwable t3) {
            LOGGER.log(Level.SEVERE, "Server pushed stacktrace on response: " + t3.getMessage());
            t3.printStackTrace();
        } finally {
            try {
                if (pw != null) {
                    pw.close();
                }
            } catch (Throwable t2) {
                t2.printStackTrace();
            }
        }
    }
}


package org.cnmc.PainClinic.PainReportServer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnmc.PainClinic.PainReportServer.domain.SurveyResultAnswer;
import org.cnmc.PainClinic.PainReportServer.PainReportSettings;
import org.cnmc.PainClinic.PainReportServer.dmp.PainReportDAOFactory;
import org.cnmc.PainClinic.PainReportServer.dmp.DMPException;
import org.cnmc.PainClinic.PainReportServer.dmp.IPainReportDAO;

@SuppressWarnings("serial")
public class PainReportActionServlet extends HttpServlet {
    private static Logger LOGGER = PainReportSettings.getPainReportLogger();
    private static String __PREAMBLE = "<HTML><HEAD><TITLE>CNMC PainReport Action</TITLE></HEAD><BODY><p>\n";

    /**
     * doGet 
     */
    public final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = null;
        try {
            response.setContentType("text/html");
            out = response.getWriter();
            out.println(__PREAMBLE);
            IPainReportDAO theDAO = PainReportDAOFactory.getDAO();

            // available actions here
            String action = request.getParameter("action");
            String pin    = request.getParameter("PIN");

            // if toggle, update completed flag
            if (action.equals("toggle")) {
                if (theDAO.toggleCompleted(pin)) {
                    out.println("Update to Completed successful</br/>");
                } else {
                    out.println("Update to Completed failed</br/>");
                }
            } else if (action.equals("displaysurveys")) {
                List<SurveyResultAnswer> answers = theDAO.getSurveyResultAnswers(pin);
                if (answers == null || answers.isEmpty()) {
                    out.println("<em>No Surveys to Display for </em>" + pin);
                } else {
                    out.println("<em>Surveys to display for </em>" + pin);
                    Date d = null;
                    for (SurveyResultAnswer sra: answers) {
                        if (!sra.getWhenCompleted().equals(d)) {
                            if (d != null) {
                                out.println("</OL></p><br/>");
                            }
                            d = sra.getWhenCompleted();
                            out.println("\nSurvey taken " + d + "\n<p><OL>");
                        }
                        out.println("<LI>"+sra.getQuestionId()+"  "+sra.getAnswerComponentId()+"<br/>");
                        out.println(sra.getValue()+"</LI>");
                    }
                    out.println("</OL></p>");
                }
            } else {
                out.println("INVALID ACTION<br/>");
            }
            out.println("</p><a href=\"prdisplay\">Return to home</a>\n</body></html>");
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

        PrintWriter out = null;
        try {
            response.setContentType("text/html");
            out = response.getWriter();
            out.println(__PREAMBLE);
            out.println("EXPORT functionality not ready yet!!!<p>");
            String[] pinsToExport = request.getParameterValues("expin");
            if (pinsToExport == null || pinsToExport.length == 0) {
                out.println("No Patient Info to export!!!</p>");
            } else {
                out.println("Will export data for PINs:</p><UL>");
                for (int i = 0; i < pinsToExport.length; i++) {
                    out.println("<LI>"+pinsToExport[i]+"</LI>");
                }
                out.println("</UL>");
            }
            out.println("</body></html>");
        } catch (Throwable t3) {
            LOGGER.log(Level.SEVERE, "Server pushed stacktrace on response: " + t3.getMessage());
            t3.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Throwable t2) {
                LOGGER.log(Level.WARNING, "Could not flush and close output stream on doPost");
            }
        }
    }
}


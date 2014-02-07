package org.cnmc.PainClinic.PainReportServer.domain;

import java.util.Date;

public class Patient {
	private String  __pin;
	private Date __nextReading;
	private boolean __isAndroid;
    private String __osVersion;
	private int __countSurveyResults;
    private Date __dateStarted;
    private boolean __isCompleted;
    private Date __lastSurveyResult;

	public String getPin() {
		return __pin;
	}
    public int getCountSurveyResults() {
        return __countSurveyResults;
    }
	public Date getNextReading() {
		return __nextReading;
	}
    public Date getDateStarted() {
        return __dateStarted;
    }
    public boolean isCompleted() {
        return __isCompleted;
    }
    public Date getLastSurveyResult() {
        return __lastSurveyResult;
    }
	public boolean isAndroid() {
		return __isAndroid;
	}
    public String getOSVersion() {
        return __osVersion;
    }

	public void setNextDate(Date next) {
		__nextReading = next;
	}
    public void setIsCompleted(boolean c) {
        __isCompleted = c;
    }
    public void setDateStarted(Date c) {
        __dateStarted = c;
    }

    // This version for form entry of a new patient
    public Patient(String pin, boolean isAndroid, String os) {
        __pin = pin;
        __isAndroid = isAndroid;
        __osVersion = os;

        Date d = new Date(System.currentTimeMillis());
        __nextReading = d;
        __lastSurveyResult = null;
        __countSurveyResults = 0;
        __dateStarted = d;
        __isCompleted = false;
    }

    // This version is for data pulled out of the DB
	public Patient(String pin, int countResults, Date last, Date next,
                   boolean isAndroid, String os, Date started, boolean completed) {
		__pin = pin;
        __countSurveyResults = countResults;
        __lastSurveyResult = last;
		__nextReading = next;
		__isAndroid = isAndroid;
        __osVersion = os;
        __dateStarted = started;
        __isCompleted = completed;
	}
}

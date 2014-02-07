package org.cnmc.PainClinic.PainReportServer.domain;

import java.util.Date;

public class SurveyResult {
	private String  __patientId;
	private Date    __whenCompleted;

	public SurveyResult(String p, Date d) {
		__patientId = p;
		__whenCompleted = d;
	}

	public String getPatientId() {
		return __patientId;
	}

	public Date getWhenCompleted() {
		return __whenCompleted;
	}
}

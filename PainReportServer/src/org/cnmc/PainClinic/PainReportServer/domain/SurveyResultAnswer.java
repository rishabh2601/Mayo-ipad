package org.cnmc.PainClinic.PainReportServer.domain;

import java.util.Date;

public class SurveyResultAnswer {
	private String __patientId;
	private Date   __whenCompleted;
	private String __answerComponentId;
	private String __questionId;
	private String __value;
	
	public String getPatientId() {
		return __patientId;
	}
	public Date getWhenCompleted() {
		return __whenCompleted;
	}
	public String getAnswerComponentId() {
		return __answerComponentId;
	}
	public String getQuestionId() {
		return __questionId;
	}
	/**
	 * The actual value must be marshalled from a String to some native type
	 * @param pid
	 * @param d
	 * @param acid
	 * @param qid
	 */
	public String getValue() {
		return __value;
	}
	
	public SurveyResultAnswer(String pid, Date d, String acid, String qid, String v) {
		__patientId = pid;
		__whenCompleted = d;
		__answerComponentId = acid;
		__questionId = qid;
		__value = v;
	}
	
}

package org.cnmc.PainClinic.PainReportServer.domain;

public class Question {
	private String __questionId;
	private String __questionText;
	
	public String get__questionId() {
		return __questionId;
	}
	public String get__questionText() {
		return __questionText;
	}
	
	public Question(String id, String text) {
		__questionId = id;
		__questionText = text;
	}
}

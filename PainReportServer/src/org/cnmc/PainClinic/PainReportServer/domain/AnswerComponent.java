package org.cnmc.PainClinic.PainReportServer.domain;

public class AnswerComponent {
	private String __questionId;
	private String __answerId;
	private int __type;
	
	// need to Enum this
	public static final int TRUE_FALSE_TYPE = 1;
	public static final int RANGE_TYPE = 2;
	public static final int MCSA_TYPE = 3;
	public static final int MCMA_TYPE = 4;
	public static final int HOTSPOT_TYPE = 5;
	public static final int TEXT_TYPE = 6;
	public static final int CHOICE_TYPE = 7;
	public static final int OTHER_TYPE = 8;
	
	public String getQuestionId() {
		return __questionId;
	}
	public String getAnswerId() {
		return __answerId;
	}
	public int getType() {
		return __type;
	}
	
	public AnswerComponent(String qid, String aid, int t) {
		__questionId = qid;
		__answerId = aid;
		__type = t;
	}	
}

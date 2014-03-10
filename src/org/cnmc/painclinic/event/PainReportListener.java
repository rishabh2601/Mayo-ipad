package org.cnmc.painclinic.event;

public interface PainReportListener {

	/**
	 * 
	 */
	public void OnEventCreated(PainReportEvent event);
	
	/**
	 * 
	 */
	public void onEventDeleted(PainReportEvent event);
	
	/**
	 * 
	 */
	public void onEventModified(PainReportEvent event);
	
	/**
	 * @param event
	 */
	public void onEventNotified(PainReportEvent event);


}




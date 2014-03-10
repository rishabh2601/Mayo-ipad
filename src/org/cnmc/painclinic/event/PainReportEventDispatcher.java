/*
 * 
 */
package org.cnmc.painclinic.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
// TODO: Auto-generated Javadoc

import android.util.Log;

/**
 * The Class PainReportEventDispatcher.
 */
public class PainReportEventDispatcher {
	
	
	/** The instance. */
	private static PainReportEventDispatcher instance = new PainReportEventDispatcher();

    /**
	 * Gets the single instance of PainReportEventDispatcher.
	 * 
	 * @return single instance of PainReportEventDispatcher
	 */
    public static PainReportEventDispatcher getInstance() {
        return instance;
    }

    /** The listeners. */
    private List<PainReportListener> listeners = new CopyOnWriteArrayList<PainReportListener>();

    private PainReportEventDispatcher() {
    }

    public synchronized void addListener(PainReportListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        listeners.add(listener);
    }

    public synchronized void removeListener(PainReportListener listener) {
        listeners.remove(listener);
    }

    /**
     * Destroy dispatcher. Intended to be used primarily during testing. 
     */
    public static void destroy() {       
       instance = new PainReportEventDispatcher();
    }
    
    public void dispatchEvent(PainReportEvent event) {
        int eventType = event.getEventType();       

        for (PainReportListener listener : listeners) {
        	Log.d("PainReportEventDispatcher", "Inside dispatchEvent - listeners are:"+ listener);
            try {
                switch (eventType) {
                    case PainReportEvent.EVENT_CREATED: {
                        listener.OnEventCreated(event);
                        break;
                    }
                    case PainReportEvent.EVENT_DELETED: {
                        listener.onEventDeleted(event);
                        break;
                    }
                    case PainReportEvent.EVENT_MODIFIED: {
                        listener.onEventModified(event);
                        break;
                    }
                    case PainReportEvent.EVENT_NOTIFIED: {
                        listener.onEventNotified(event);
                        break;
                    }
                    default:
                        break;
                }
            }
            catch (Exception simpleEx) {
            	simpleEx.printStackTrace();
            }
        }
    }
}

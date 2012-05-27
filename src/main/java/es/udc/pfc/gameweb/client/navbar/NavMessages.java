package es.udc.pfc.gameweb.client.navbar;

import com.calclab.emite.core.session.SessionStatus;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface NavMessages extends Messages {
	
	public static final NavMessages msg = GWT.create(NavMessages.class);
	
	@DefaultMessage("Connecting...")
	@AlternateMessage({
		"disconnected", "Disconnected",
		"error", "Error",
		"ready", "Ready",
		"rosterReady", "Ready2"
		})
	String statusMessage(@Select SessionStatus status);
	
}

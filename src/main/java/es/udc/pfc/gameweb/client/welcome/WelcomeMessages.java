package es.udc.pfc.gameweb.client.welcome;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface WelcomeMessages extends Messages {
	
	public static final WelcomeMessages msg = GWT.create(WelcomeMessages.class);
	
	@DefaultMessage("Welcome")
	String pageTitle();
	
	@DefaultMessage("MiniChess")
	String miniChessButton();
	
}

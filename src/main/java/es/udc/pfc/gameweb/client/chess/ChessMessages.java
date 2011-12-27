package es.udc.pfc.gameweb.client.chess;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface ChessMessages extends Messages {
	
	public static final ChessMessages msg = GWT.create(ChessMessages.class);
	
	@DefaultMessage("Message")
	String labelMessage();
	
}

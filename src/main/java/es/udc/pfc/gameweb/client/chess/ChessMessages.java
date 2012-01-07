package es.udc.pfc.gameweb.client.chess;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface ChessMessages extends Messages {
	
	public static final ChessMessages msg = GWT.create(ChessMessages.class);
	
	@DefaultMessage("Message")
	String labelMessage();
	
	@DefaultMessage("Waiting for opponent...")
	@AlternateMessage({"true", "It''s your turn!"})
	String currentTurn(@Select boolean current);
	
	@DefaultMessage("You lost")
	@AlternateMessage({"true", "You won!"})
	String winner(@Select boolean current);
	
	@DefaultMessage("It''s a draw!")
	String draw();
	
	@DefaultMessage("Unknown error")
	@AlternateMessage({
		"not-started", "Game is not running",
		"invalid-turn", "Invalid turn",
		"invalid-position", "Invalid position",
		"invalid-movement", "Invalid movement"
		})
	String errorStatus(@Select String core);
	
}

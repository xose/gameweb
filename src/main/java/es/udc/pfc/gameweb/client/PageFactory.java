package es.udc.pfc.gameweb.client;

import com.calclab.emite.xep.muc.client.RoomChat;

import es.udc.pfc.gameweb.client.chess.ChessGamePage;
import es.udc.pfc.gameweb.client.welcome.WelcomePage;

public interface PageFactory {
	
	WelcomePage getWelcomePage();
	ChessGamePage getChessGamePage(RoomChat room);
	
}

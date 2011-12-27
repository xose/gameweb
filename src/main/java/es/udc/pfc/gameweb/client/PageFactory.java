package es.udc.pfc.gameweb.client;

import com.calclab.emite.xep.muc.RoomInvitation;

import es.udc.pfc.gamelib.chess.ChessGame;
import es.udc.pfc.gameweb.client.chess.ChessGamePage;
import es.udc.pfc.gameweb.client.welcome.WelcomePage;

public interface PageFactory {
	
	WelcomePage getWelcomePage();
	ChessGamePage getChessGamePage(RoomInvitation invite, ChessGame game);
	
}

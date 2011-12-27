package es.udc.pfc.gameweb.client.board;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

import es.udc.pfc.gamelib.board.Board;
import es.udc.pfc.gamelib.board.Piece;
import es.udc.pfc.gamelib.board.Position;

public interface BoardWidget<P extends Piece> extends IsWidget {
	
	void setBoard(Board<P> board);
	void drawBoard();
	
	void clearHighlights();
	void highlightPosition(Position position, String format);
	
	HandlerRegistration addPositionClickedHandler(PositionClickedEvent.Handler handler);
}

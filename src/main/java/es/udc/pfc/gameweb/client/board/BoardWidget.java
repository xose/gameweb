package es.udc.pfc.gameweb.client.board;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

import es.udc.pfc.gamelib.board.Board;
import es.udc.pfc.gamelib.board.Piece;
import es.udc.pfc.gamelib.board.Position;

public interface BoardWidget<P extends Piece> extends IsWidget {
	
	public void setBoard(final Board<P> board);
	public void drawBoard();
	
	public void clearHighlights();
	public void highlightPosition(final Position position, final String format);
	
	public HandlerRegistration addPositionClickedHandler(final PositionClickedEvent.Handler handler);
}

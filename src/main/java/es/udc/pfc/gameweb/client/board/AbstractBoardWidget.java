/*
 * Copyright 2011 José Martínez
 * 
 * This file is part of GameWeb.
 *
 * GameWeb is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * GameWeb is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GameWeb.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.udc.pfc.gameweb.client.board;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

import es.udc.pfc.gamelib.board.Board;
import es.udc.pfc.gamelib.board.Piece;
import es.udc.pfc.gamelib.board.Position;

public abstract class AbstractBoardWidget<P extends Piece> extends Composite implements BoardWidget<P>, ClickHandler {
	
	private final Canvas canvas;
	private final Context2d context;
	private final Map<Position, String> highlighted;

	@Nullable private Board<P> board;

	protected AbstractBoardWidget() {
		highlighted = new HashMap<Position, String>();
		canvas = Canvas.createIfSupported();
		if (canvas == null) {
			context = null;
			initWidget(new Label("Canvas not supported"));
			return;
		}

		canvas.addClickHandler(this);
		context = canvas.getContext2d();

		initWidget(canvas);
	}
	
	abstract protected ImageElement getPieceImage(final P piece);
	
	@Override
	public final void setBoard(final Board<P> board) {
		this.board = checkNotNull(board);

		final int width = board.getNumberOfColumns() * 100;
		final int height = board.getNumberOfRows() * 100;
		
		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		drawBoard();
	}

	private void fillSquare(final int col, final int row, final String style) {
		context.setFillStyle(style);
		context.fillRect((col - 1) * 100, (board.getNumberOfRows() - row) * 100, 100, 100);
	}

	@Override
	public final void drawBoard() {
		if (board == null)
			return;

		context.clearRect(0, 0, board.getNumberOfColumns() * 100, board.getNumberOfRows() * 100);

		for (int col = 1; col <= board.getNumberOfColumns(); col++) {
			for (int row = 1; row <= board.getNumberOfRows(); row++) {
				fillSquare(col, row, (col + row) % 2 == 0 ? "#ffce9e" : "#d18b47");
			}
		}

		for (final Entry<Position, String> hl : highlighted.entrySet()) {
			fillSquare(hl.getKey().getColumn(), hl.getKey().getRow(), hl.getValue());
		}

		for (final P piece : board.getAllPieces()) {
			final ImageElement ie = getPieceImage(piece);

			final int col = piece.getPosition().getColumn();
			final int row = piece.getPosition().getRow();

			context.drawImage(ie, (col - 1) * 100, (board.getNumberOfRows() - row) * 100, 100, 100);
		}
	}

	@Override
	public void onClick(final ClickEvent event) {
		if (board == null)
			return;
		
		final Position clicked = new Position(1 + event.getX() / 100, board.getNumberOfRows() - event.getY() / 100);

		if (board.isValidPosition(clicked)) {
			fireEvent(new PositionClickedEvent(clicked));
		}
	}
	
	@Override
	public final void clearHighlights() {
		highlighted.clear();
		drawBoard();
	}
	
	@Override
	public final void highlightPosition(final Position position, final String format) {
		if (board == null)
			return;
		
		if (board.isValidPosition(position)) {
			highlighted.put(position, format);
		}
		
		drawBoard();
	}

	@Override
	public final HandlerRegistration addPositionClickedHandler(final PositionClickedEvent.Handler handler) {
		return addHandler(handler, PositionClickedEvent.TYPE);
	}
	
}

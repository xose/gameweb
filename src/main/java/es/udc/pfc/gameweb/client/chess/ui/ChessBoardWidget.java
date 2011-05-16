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

package es.udc.pfc.gameweb.client.chess.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import es.udc.pfc.gamelib.board.Position;
import es.udc.pfc.gamelib.chess.ChessBoard;
import es.udc.pfc.gamelib.chess.ChessPiece;
import es.udc.pfc.gameweb.client.chess.pieces.ChessPieces;

public class ChessBoardWidget extends Composite implements ClickHandler {

	private static final Logger logger = Logger.getLogger(ChessBoardWidget.class.getName());

	private static final Map<String, ImageElement> pieceImages;

	static {
		pieceImages = new HashMap<String, ImageElement>();

		pieceImages.put("N", ImageElement.as(new Image(ChessPieces.INSTANCE.wn().getURL()).getElement()));
		pieceImages.put("Q", ImageElement.as(new Image(ChessPieces.INSTANCE.wq().getURL()).getElement()));
		pieceImages.put("K", ImageElement.as(new Image(ChessPieces.INSTANCE.wk().getURL()).getElement()));
		pieceImages.put("B", ImageElement.as(new Image(ChessPieces.INSTANCE.wb().getURL()).getElement()));
		pieceImages.put("R", ImageElement.as(new Image(ChessPieces.INSTANCE.wr().getURL()).getElement()));
		pieceImages.put("P", ImageElement.as(new Image(ChessPieces.INSTANCE.wp().getURL()).getElement()));

		pieceImages.put("n", ImageElement.as(new Image(ChessPieces.INSTANCE.bn().getURL()).getElement()));
		pieceImages.put("q", ImageElement.as(new Image(ChessPieces.INSTANCE.bq().getURL()).getElement()));
		pieceImages.put("k", ImageElement.as(new Image(ChessPieces.INSTANCE.bk().getURL()).getElement()));
		pieceImages.put("b", ImageElement.as(new Image(ChessPieces.INSTANCE.bb().getURL()).getElement()));
		pieceImages.put("r", ImageElement.as(new Image(ChessPieces.INSTANCE.br().getURL()).getElement()));
		pieceImages.put("p", ImageElement.as(new Image(ChessPieces.INSTANCE.bp().getURL()).getElement()));
	}

	private final Canvas canvas;
	private final Context2d context;

	private ChessBoard board;
	private Position selected;

	public ChessBoardWidget() {
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

	public void setChessBoard(final String boardFEN) {
		board = ChessBoard.fromString(boardFEN);

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
		context.fillRect((col - 1) * 100, (6 - row) * 100, 100, 100);
	}

	private void drawBoard() {
		if (board == null)
			return;

		context.clearRect(0, 0, 500, 600);

		for (int col = 1; col <= 5; col++) {
			for (int row = 1; row <= 6; row++) {
				fillSquare(col, row, (col + row) % 2 == 0 ? "#ffce9e" : "#d18b47");
			}
		}

		if (selected != null) {
			fillSquare(selected.getColumn(), selected.getRow(), "#ff0000");

			for (final Position move : board.getPieceAt(selected).getAllMoves()) {
				fillSquare(move.getColumn(), move.getRow(), "#00ff00");
			}
		}

		for (final ChessPiece piece : board.getAllPieces()) {
			final ImageElement ie = pieceImages.get(piece.toString());

			final int col = piece.getPosition().getColumn();
			final int row = piece.getPosition().getRow();

			context.drawImage(ie, (col - 1) * 100, (6 - row) * 100, 100, 100);
		}
	}

	@Override
	public void onClick(final ClickEvent event) {
		final Position clicked = new Position(1 + event.getX() / 100, 6 - event.getY() / 100);

		final ChessPiece selectedPiece = board.getPieceAt(selected);
		if (selectedPiece != null && selectedPiece.canMove(clicked)) {
			fireEvent(new PieceMovedEvent(selected, clicked));
			selected = null;
		} else if (board.isPieceAt(clicked) && !clicked.equals(selected)) {
			selected = clicked;
		} else {
			selected = null;
		}

		drawBoard();
	}

	public void movePiece(final Position from, final Position to) {
		board.movePiece(from, to);

		selected = null;
		drawBoard();
	}

	public HandlerRegistration addPieceMovedHandler(final PieceMovedEvent.Handler handler) {
		return addHandler(handler, PieceMovedEvent.TYPE);
	}

}

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

package es.udc.pfc.gameweb.client.chess;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;

import es.udc.pfc.gamelib.chess.ChessPiece;
import es.udc.pfc.gameweb.client.board.AbstractBoardWidget;
import es.udc.pfc.gameweb.client.chess.pieces.ChessPieces;

public class ChessBoardWidget extends AbstractBoardWidget<ChessPiece> {

	private static final Map<String, ImageElement> pieceImages;

	static {
		pieceImages = new HashMap<String, ImageElement>();
		
		pieceImages.put("N", ImageElement.as(new Image(ChessPieces.INSTANCE.wn().getSafeUri()).getElement()));
		pieceImages.put("Q", ImageElement.as(new Image(ChessPieces.INSTANCE.wq().getSafeUri()).getElement()));
		pieceImages.put("K", ImageElement.as(new Image(ChessPieces.INSTANCE.wk().getSafeUri()).getElement()));
		pieceImages.put("B", ImageElement.as(new Image(ChessPieces.INSTANCE.wb().getSafeUri()).getElement()));
		pieceImages.put("R", ImageElement.as(new Image(ChessPieces.INSTANCE.wr().getSafeUri()).getElement()));
		pieceImages.put("P", ImageElement.as(new Image(ChessPieces.INSTANCE.wp().getSafeUri()).getElement()));

		pieceImages.put("n", ImageElement.as(new Image(ChessPieces.INSTANCE.bn().getSafeUri()).getElement()));
		pieceImages.put("q", ImageElement.as(new Image(ChessPieces.INSTANCE.bq().getSafeUri()).getElement()));
		pieceImages.put("k", ImageElement.as(new Image(ChessPieces.INSTANCE.bk().getSafeUri()).getElement()));
		pieceImages.put("b", ImageElement.as(new Image(ChessPieces.INSTANCE.bb().getSafeUri()).getElement()));
		pieceImages.put("r", ImageElement.as(new Image(ChessPieces.INSTANCE.br().getSafeUri()).getElement()));
		pieceImages.put("p", ImageElement.as(new Image(ChessPieces.INSTANCE.bp().getSafeUri()).getElement()));
	}

	public ChessBoardWidget() {
		super();
	}

	@Override
	protected final ImageElement getPieceImage(final ChessPiece piece) {
		return pieceImages.get(piece.toString());
	}

}

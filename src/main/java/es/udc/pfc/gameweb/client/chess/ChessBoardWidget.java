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

import com.google.common.collect.ImmutableMap;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;

import es.udc.pfc.gamelib.chess.ChessPiece;
import es.udc.pfc.gameweb.client.board.AbstractBoardWidget;
import es.udc.pfc.gameweb.client.chess.pieces.ChessPieces;

public class ChessBoardWidget extends AbstractBoardWidget<ChessPiece> {

	private static final ImmutableMap<String, ImageElement> pieceImages;

	static {
		final ImmutableMap.Builder<String, ImageElement> builder = ImmutableMap.builder();
		
		builder.put("N", ImageElement.as(new Image(ChessPieces.INSTANCE.wn().getSafeUri()).getElement()));
		builder.put("Q", ImageElement.as(new Image(ChessPieces.INSTANCE.wq().getSafeUri()).getElement()));
		builder.put("K", ImageElement.as(new Image(ChessPieces.INSTANCE.wk().getSafeUri()).getElement()));
		builder.put("B", ImageElement.as(new Image(ChessPieces.INSTANCE.wb().getSafeUri()).getElement()));
		builder.put("R", ImageElement.as(new Image(ChessPieces.INSTANCE.wr().getSafeUri()).getElement()));
		builder.put("P", ImageElement.as(new Image(ChessPieces.INSTANCE.wp().getSafeUri()).getElement()));

		builder.put("n", ImageElement.as(new Image(ChessPieces.INSTANCE.bn().getSafeUri()).getElement()));
		builder.put("q", ImageElement.as(new Image(ChessPieces.INSTANCE.bq().getSafeUri()).getElement()));
		builder.put("k", ImageElement.as(new Image(ChessPieces.INSTANCE.bk().getSafeUri()).getElement()));
		builder.put("b", ImageElement.as(new Image(ChessPieces.INSTANCE.bb().getSafeUri()).getElement()));
		builder.put("r", ImageElement.as(new Image(ChessPieces.INSTANCE.br().getSafeUri()).getElement()));
		builder.put("p", ImageElement.as(new Image(ChessPieces.INSTANCE.bp().getSafeUri()).getElement()));
		
		pieceImages = builder.build();
	}

	public ChessBoardWidget() {
		super();
	}

	@Override
	protected final ImageElement getPieceImage(final ChessPiece piece) {
		return pieceImages.get(piece.toString());
	}

}

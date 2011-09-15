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

import com.google.common.collect.ImmutableSet;
import com.google.gwt.user.client.ui.IsWidget;

import es.udc.pfc.gamelib.board.Position;
import es.udc.pfc.gamelib.chess.ChessBoard;
import es.udc.pfc.gamelib.chess.ChessColor;
import es.udc.pfc.gamelib.chess.ChessMovement;

public interface ChessGameView extends IsWidget {

	public void setPresenter(final Presenter presenter);
	
	public void setPlayerColor(final ChessColor color);
	
	public void setActiveColor(final ChessColor color);

	public void setBoard(final ChessBoard board);
	
	public void updateBoard();

	public void addMovement(final ChessMovement movement);
	
	public void setStatusText(final String text);

	public void addChatLine(final String text);
	
	public interface Presenter {
		public void sendChat(final String text);

		public void sendCommand(final String text);

		public void movePiece(final Position from, final Position to);
		
		public ImmutableSet<Position> getPossibleMoves(final Position position);
	}

}

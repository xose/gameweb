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

	void setPresenter(Presenter presenter);
	
	void setPlayerColor(ChessColor color);
	
	void setActiveColor(ChessColor color);

	void setBoard(ChessBoard board);
	
	void updateBoard();

	void addMovement(ChessMovement movement);
	
	void setStatusText(String text);

	void addChatLine(String text);
	
	public interface Presenter {
		void sendChat(String text);

		void sendCommand(String... cmd);

		void movePiece(Position from, Position to);
		
		ImmutableSet<Position> getPossibleMoves(Position position);
	}

}

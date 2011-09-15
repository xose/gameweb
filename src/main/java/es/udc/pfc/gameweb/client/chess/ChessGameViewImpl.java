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

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import es.udc.pfc.gamelib.board.Position;
import es.udc.pfc.gamelib.chess.ChessBoard;
import es.udc.pfc.gamelib.chess.ChessColor;
import es.udc.pfc.gamelib.chess.ChessMovement;
import es.udc.pfc.gamelib.chess.ChessPiece;
import es.udc.pfc.gameweb.client.board.PositionClickedEvent;

public class ChessGameViewImpl extends Composite implements ChessGameView, PositionClickedEvent.Handler {
	
	@UiTemplate("ChessGameView.ui.xml")
	protected interface Binder extends UiBinder<Widget, ChessGameViewImpl> {
	}
	
	private static final Binder uiBinder = GWT.create(Binder.class);

	@Nullable private Presenter presenter;

	@UiField
	protected ChessBoardWidget boardWidget;

	@UiField
	protected HasWidgets roomMessages;

	@UiField
	protected HasText chatBox;

	@UiField
	protected HasText commandBox;

	@UiField
	protected HasText response;
	
	@Nullable private ChessBoard board;
	@Nullable private Position selected;
	@Nullable private ChessColor playerColor;
	private ChessColor activeColor = ChessColor.WHITE;
	
	public ChessGameViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));

		boardWidget.addPositionClickedHandler(this);
	}

	@Override
	public final void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

	@UiHandler("chatBox")
	protected final void onChatBoxChange(final ChangeEvent event) {
		presenter.sendChat(chatBox.getText());

		chatBox.setText("");
	}

	@UiHandler("commandBox")
	protected final void onCommandBoxChange(final ChangeEvent event) {
		presenter.sendCommand(commandBox.getText());

		commandBox.setText("");
		response.setText("");
	}
	
	@Override
	public final void setPlayerColor(final ChessColor color) {
		this.playerColor = checkNotNull(color);
	}
	
	@Override
	public final void setActiveColor(final ChessColor color) {
		this.activeColor = checkNotNull(color);
	}

	@Override
	public final void setBoard(final ChessBoard board) {
		this.board = checkNotNull(board);
		
		boardWidget.setBoard(board);
	}
	
	@Override
	public final void updateBoard() {
		boardWidget.drawBoard();
	}

	@Override
	public void addMovement(final ChessMovement movement) {
		roomMessages.add(new Label("Movement: "+movement.toString()));
	}
	
	@Override
	public final void setStatusText(final String text) {
		response.setText(text);
	}

	@Override
	public void addChatLine(final String text) {
		roomMessages.add(new Label(text));
	}
	
	@Override
	public void onPositionClicked(final PositionClickedEvent event) {
		if (!activeColor.equals(playerColor))
			return;
		
		final Position position = event.getPosition();
		final ChessPiece piece = board.getPieceAt(position);
		
		boardWidget.clearHighlights();
		
		if (selected != null && presenter.getPossibleMoves(selected).contains(position)) {
			presenter.movePiece(selected, position);
			return;
		}
		
		if (piece != null && piece.getColor().equals(playerColor)) {
			selected = position;
			
			boardWidget.highlightPosition(position, "#ff0000");
			for (final Position move : presenter.getPossibleMoves(position)) {
				boardWidget.highlightPosition(move, "#00ff00");
			}
			
			return;
		}
		
		selected = null;
	}
	
}

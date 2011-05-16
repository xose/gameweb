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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import es.udc.pfc.gamelib.board.Position;

public class ChessGameViewImpl extends Composite implements ChessGameView {

	private static ChessGameViewImplUiBinder uiBinder = GWT.create(ChessGameViewImplUiBinder.class);

	interface ChessGameViewImplUiBinder extends UiBinder<Widget, ChessGameViewImpl> {
	}

	private Presenter presenter;

	public ChessGameViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	ChessBoardWidget chessBoard;

	@UiField
	HasWidgets roomMessages;

	@UiField
	HasText chatBox;

	@UiField
	HasText commandBox;

	@UiField
	HasText response;

	@UiHandler("chatBox")
	void onChatBoxChange(final ChangeEvent event) {
		presenter.sendChat(chatBox.getText());

		chatBox.setText("");
	}

	@UiHandler("commandBox")
	final void onCommandBoxChange(final ChangeEvent event) {
		presenter.sendCommand(commandBox.getText());

		commandBox.setText("");
		response.setText("");
	}

	@Override
	public void setBoard(final String boardFEN) {
		chessBoard.setChessBoard(boardFEN);
	}

	@Override
	public void movePiece(final String from, final String to) {
		chessBoard.movePiece(Position.fromString(from), Position.fromString(to));
	}

	@Override
	public final void setCommandResponse(final String text) {
		response.setText(text);
	}

	@Override
	public void addChatLine(final String text) {
		roomMessages.add(new Label(text));
	}

	@Override
	public final void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

}

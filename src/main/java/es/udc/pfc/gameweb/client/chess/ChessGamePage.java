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

import java.util.logging.Logger;

import com.calclab.emite.core.client.events.MessageReceivedEvent;
import com.calclab.emite.core.client.events.PresenceReceivedEvent;
import com.calclab.emite.core.client.xmpp.stanzas.Message;
import com.calclab.emite.core.client.xmpp.stanzas.Presence;
import com.calclab.emite.im.client.chat.ChatStateChangedEvent;
import com.calclab.emite.im.client.chat.ChatStates;
import com.calclab.emite.xep.muc.client.RoomChat;
import com.calclab.emite.xep.muc.client.RoomChatManager;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import es.udc.pfc.gamelib.board.Position;
import es.udc.pfc.gamelib.chess.ChessBoard;
import es.udc.pfc.gamelib.chess.ChessColor;
import es.udc.pfc.gamelib.chess.ChessMovement;
import es.udc.pfc.gamelib.chess.ChessRules;
import es.udc.pfc.gamelib.chess.MiniChessRules;
import es.udc.pfc.gameweb.client.layout.AbstractPage;

public class ChessGamePage extends AbstractPage implements ChatStateChangedEvent.Handler, MessageReceivedEvent.Handler, PresenceReceivedEvent.Handler, ChessGameView.Presenter {

	private static final Logger log = Logger.getLogger(ChessGamePage.class.getName());
	private static final Splitter cmdSplitter = Splitter.on(':');
	private static final Joiner cmdJoiner = Joiner.on(' ');
	
	private ChessBoard board;
	private final ChessRules rules;
	
	private final ChessGameView view;
	private final RoomChatManager roomManager;
	private final RoomChat room;

	@Inject
	public ChessGamePage(final EventBus eventBus, final ChessGameView view, final RoomChatManager roomManager, @Assisted final RoomChat room) {
		super(eventBus);
		
		super.setPageTitle("Chess");
		
		this.rules = new MiniChessRules();
		this.view = view;
		this.roomManager = roomManager;
		this.room = room;
		
		view.setPresenter(this);
		room.addChatStateChangedHandler(true, this);
		room.addMessageReceivedHandler(this);
		room.addPresenceReceivedHandler(this);
	}

	@Override
	public boolean willClose() {
		roomManager.close(room);
		
		return true;
	}
	
	@Override
	public void onChatStateChanged(final ChatStateChangedEvent event) {
		if (ChatStates.isReady(event.getState())) {
			log.info("READY!");
			sendCommand("!board");
		} else {
			log.info("NOT READY: "+event.getState());
		}
	}

	@Override
	public void onMessageReceived(final MessageReceivedEvent event) {
		final Message m = event.getMessage();

		if (m.getBody() == null)
			return;
		
		if (m.getFrom().getResource().equals("arbiter")) {
			final ImmutableList<String> command = ImmutableList.copyOf(cmdSplitter.split(m.getBody()));
			receivedArbiter(command, Message.Type.chat.equals(m.getType()));
		} else if (m.getType().equals(Message.Type.groupchat)) {
			receivedGroupChat(m.getFrom().getResource(), m.getBody());
		}
	}
	
	@Override
	public void onPresenceReceived(final PresenceReceivedEvent event) {
		final Presence p = event.getPresence();
		view.addChatLine(p.getFrom().getResource() + " - " + (p.getType() != null ? p.getType().toString() : "available"));
	}

	private void receivedGroupChat(final String from, final String body) {
		view.addChatLine("<" + from + "> " + body);
	}

	private void receivedArbiter(final ImmutableList<String> cmd, final boolean priv) {
		//view.addChatLine("arbiter: " + cmd.toString());

		if (cmd.size() == 2 && cmd.get(0).equals("board")) {
			board = ChessBoard.fromString(cmd.get(1));
			rules.setBoard(board);
			view.setBoard(board);
		} else if (cmd.size() == 3 && cmd.get(0).equals("move")) {
			final Position from = Position.fromString(cmd.get(1));
			final Position to = Position.fromString(cmd.get(2));
			
			for (final ChessMovement movement : rules.getPossibleMovements()) {
				if (movement.getFrom().equals(from) && movement.getTo().equals(to)) {
					board.setPieceAt(movement.getTo(), board.setPieceAt(movement.getFrom(), null));
					view.addMovement(movement);
					view.setActiveColor(rules.nextTurn());
					view.updateBoard();
					break;
				}
			}
		} else if (cmd.size() == 2 && cmd.get(0).equals("color")) {
			final ChessColor color = ChessColor.valueOf(cmd.get(1));
			log.info("turn: "+color.toString());
			view.setPlayerColor(color);
		} else if (priv) {
			view.setStatusText(cmd.toString());
		}
	}

	// Presenter

	@Override
	public void sendChat(final String text) {
		room.send(new Message(text));
	}

	@Override
	public void sendCommand(final String text) {
		room.sendPrivateMessage(new Message(text), "arbiter");
	}

	@Override
	public void movePiece(final Position from, final Position to) {
		sendCommand(cmdJoiner.join("!move", from, to));
	}

	@Override
	public ImmutableSet<Position> getPossibleMoves(final Position position) {
		final ImmutableSet.Builder<Position> builder = ImmutableSet.builder();
		
		for (final ChessMovement movement : rules.getPossibleMovements()) {
			if (movement.getFrom().equals(position)) {
				builder.add(movement.getTo());
			}
		}
		
		return builder.build();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}

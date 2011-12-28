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

import java.util.logging.Logger;

import com.calclab.emite.base.util.Platform;
import com.calclab.emite.base.util.ScheduledAction;
import com.calclab.emite.base.xml.XMLPacket;
import com.calclab.emite.core.events.MessageReceivedEvent;
import com.calclab.emite.core.events.PresenceReceivedEvent;
import com.calclab.emite.core.stanzas.Message;
import com.calclab.emite.core.stanzas.Presence;
import com.calclab.emite.xep.muc.RoomChat;
import com.calclab.emite.xep.muc.RoomChatManager;
import com.calclab.emite.xep.muc.RoomInvitation;
import com.calclab.emite.xep.muc.events.RoomChatChangedEvent;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.EventBus;

import es.udc.pfc.gamelib.board.Position;
import es.udc.pfc.gamelib.chess.ChessColor;
import es.udc.pfc.gamelib.chess.ChessMovement;
import es.udc.pfc.gamelib.chess.ChessGame;
import es.udc.pfc.gameweb.client.layout.AbstractPage;

public final class ChessGamePage extends AbstractPage implements RoomChatChangedEvent.Handler, MessageReceivedEvent.Handler, PresenceReceivedEvent.Handler, ChessGameView.Presenter {

	private static final Logger log = Logger.getLogger(ChessGamePage.class.getName());
	
	private static final String XMLNS = "urn:xmpp:gamepfc:chess";
	
	private final ChessGameView view;
	private final ChessGame game;
	private final RoomChat room;

	@Inject
	protected ChessGamePage(final EventBus eventBus, final ChessGameView view, final RoomChatManager roomManager, @Assisted final RoomInvitation invite, @Assisted final ChessGame game) {
		super(eventBus);
		setPageTitle("Chess");
		this.game = checkNotNull(game);
		
		this.view = checkNotNull(view);
		view.setPresenter(this);
		view.setBoard(game.getBoard());
		
		roomManager.addRoomChatChangedHandler(this);
		room = roomManager.acceptRoomInvitation(invite, null);
		room.addMessageReceivedHandler(this);
		room.addPresenceReceivedHandler(this);
		
		// TODO: fix this
		Platform.schedule(100, new ScheduledAction() {
			@Override
			public void run() {
				final Message msg = new Message();
				msg.addExtension("x", XMLNS).addChild("ping");
				sendArbiter(msg);
			}
		});
	}

	@Override
	public final boolean willClose() {
		room.close(null);
		
		return true;
	}
	
	@Override
	public final void onRoomChatChanged(final RoomChatChangedEvent event) {
		if (room != event.getChat())
			return;
		
		if (event.isOpened()) {
			log.info("READY!");
			view.updateBoard();
		} else {
			log.info("NOT READY: "+event.getChangeType().toString());
		}
	}
	
	@Override
	public final void onMessageReceived(final MessageReceivedEvent event) {
		final Message m = event.getMessage();

		view.addChatLine("arbiter: " + m.toString());
		
		if (m.getFrom().getResource().equals("arbiter")) {
			final XMLPacket x = m.getExtension("x", XMLNS);
			if (x != null) {
				receivedArbiter(x, Message.Type.chat.equals(m.getType()));
			}
		} else if (m.getType().equals(Message.Type.groupchat)) {
			receivedGroupChat(m.getFrom().getResource(), m.getBody());
		}
	}
	
	@Override
	public final void onPresenceReceived(final PresenceReceivedEvent event) {
		final Presence p = event.getPresence();
		view.addChatLine(p.getFrom().getResource() + " - " + (p.getType() != null ? p.getType().toString() : "available"));
	}

	private final void receivedGroupChat(final String from, final String body) {
		view.addChatLine("<" + from + "> " + body);
	}

	private final void receivedArbiter(final XMLPacket x, final boolean priv) {
		if (x.hasChild("move")) {
			final XMLPacket move = x.getFirstChild("move");
			final Position from = Position.fromString(move.getAttribute("from"));
			final Position to = Position.fromString(move.getAttribute("to"));
			
			final ChessMovement movement = game.movePiece(from, to);
			view.addMovement(movement);
			view.setActiveColor(game.getCurrentTurn());
			view.updateBoard();
		} else if (x.hasChild("start")) {
			final XMLPacket start = x.getFirstChild("start");
			final ChessColor color = ChessColor.valueOf(start.getAttribute("color"));
			view.setPlayerColor(color);
		} else if (x.hasChild("error")) {
			final XMLPacket error = x.getFirstChild("error");
			view.setStatusText(error.getAttribute("status"));
		}
	}

	// Presenter

	@Override
	public final void sendChat(final String text) {
		room.send(new Message(text));
	}
	
	private final void sendArbiter(final Message message) {
		room.sendPrivateMessage(message, "arbiter");
		view.addChatLine("command: " + message.toString());
	}

	@Override
	public final void movePiece(final Position from, final Position to) {
		final Message msg = new Message();
		final XMLPacket move = msg.addExtension("x", XMLNS).addChild("move");
		move.setAttribute("from", from.toString());
		move.setAttribute("to", to.toString());
		sendArbiter(msg);
	}

	@Override
	public final ImmutableSet<Position> getPossibleMoves(final Position position) {
		final ImmutableSet.Builder<Position> builder = ImmutableSet.builder();
		
		for (final ChessMovement movement : game.getPossibleMovements()) {
			if (movement.getFrom().equals(position)) {
				builder.add(movement.getTo());
			}
		}
		
		return builder.build();
	}

	@Override
	public final Widget asWidget() {
		return view.asWidget();
	}

}

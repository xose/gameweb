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

import com.calclab.emite.core.client.events.MessageEvent;
import com.calclab.emite.core.client.events.MessageHandler;
import com.calclab.emite.core.client.events.PresenceEvent;
import com.calclab.emite.core.client.events.PresenceHandler;
import com.calclab.emite.core.client.xmpp.stanzas.Message;
import com.calclab.emite.core.client.xmpp.stanzas.Presence;
import com.calclab.emite.xep.muc.client.Room;
import com.calclab.emite.xep.muc.client.RoomChatManager;
import com.calclab.emite.xep.muc.client.RoomManager;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import es.udc.pfc.gamelib.board.Position;
import es.udc.pfc.gameweb.client.chess.ui.ChessGameView;

public class ChessGameActivity extends AbstractActivity implements ChessGameView.Presenter {

	private final ChessGameView view;
	private final RoomChatManager roomManager;
	private Room room;

	@Inject
	public ChessGameActivity(final PlaceController placeController, final ChessGameView view, final RoomManager roomManager) {
		this.view = view;
		this.roomManager = (RoomChatManager) roomManager;

		view.setPresenter(this);
	}

	public ChessGameActivity withPlace(final ChessGamePlace place) {
		room = roomManager.getChat(place.getRoomJID());

		if (room == null)
			return null;

		room.addMessageReceivedHandler(new MessageHandler() {
			@Override
			public void onMessage(final MessageEvent event) {
				final Message m = event.getMessage();

				if (m.getBody() == null)
					return;

				if (m.getType().equals(Message.Type.groupchat)) {
					if (m.getFrom().getResource().equals("arbiter")) {
						receivedArbiter(m.getBody(), false);
					} else {
						receivedGroupChat(m.getFrom().getResource(), m.getBody());
					}
				} else if (m.getType().equals(Message.Type.chat)) {
					if (m.getFrom().getResource().equals("arbiter")) {
						receivedArbiter(m.getBody(), true);
					}
				}
			}
		});

		room.addPresenceReceivedHandler(new PresenceHandler() {
			@Override
			public void onPresence(final PresenceEvent event) {
				receivedPresence(event.getPresence());
			}
		});

		return this;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		panel.setWidget(view);
	}

	@Override
	public void onStop() {
		room.close();
	}

	@Override
	public String mayStop() {
		// return "Are you sure you want to quit this game?";
		return null;
	}

	private void receivedPresence(final Presence p) {
		view.addChatLine(p.getFrom().getResource() + " - " + (p.getType() != null ? p.getType().toString() : "available"));
	}

	private void receivedGroupChat(final String from, final String body) {
		view.addChatLine("<" + from + "> " + body);
	}

	private void receivedArbiter(final String body, final boolean priv) {
		// view.addChatLine("arbiter: " + body);

		final String[] split = body.split(":");
		if (split[0].equals("board")) {
			view.setBoard(split[1]);
		} else if (split[0].equals("move")) {
			view.movePiece(split[1], split[2]);
		} else if (priv) {
			view.setCommandResponse(body);
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
		room.sendPrivateMessage(new Message("!move " + from.toString() + " " + to.toString()), "arbiter");
	}

}

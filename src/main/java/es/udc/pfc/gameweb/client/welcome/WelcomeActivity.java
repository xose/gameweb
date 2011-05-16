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

package es.udc.pfc.gameweb.client.welcome;

import java.util.logging.Logger;

import com.calclab.emite.core.client.xmpp.stanzas.Message;
import com.calclab.emite.core.client.xmpp.stanzas.XmppURI;
import com.calclab.emite.im.client.chat.Chat;
import com.calclab.emite.im.client.chat.ChatManager;
import com.calclab.emite.im.client.chat.ChatProperties;
import com.calclab.emite.xep.muc.client.Room;
import com.calclab.emite.xep.muc.client.RoomInvitation;
import com.calclab.emite.xep.muc.client.RoomManager;
import com.calclab.emite.xep.muc.client.events.RoomInvitationEvent;
import com.calclab.emite.xep.muc.client.events.RoomInvitationHandler;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import es.udc.pfc.gameweb.client.chess.ChessGamePlace;
import es.udc.pfc.gameweb.client.welcome.ui.WelcomeView;

public class WelcomeActivity extends AbstractActivity implements WelcomeView.Presenter {

	private final static Logger logger = Logger.getLogger(WelcomeActivity.class.getName());

	private final WelcomeView view;
	private final ChatManager chatManager;

	@Inject
	public WelcomeActivity(final PlaceController placeController, final WelcomeView view, final ChatManager chatManager, final RoomManager roomManager) {
		this.view = view;
		this.chatManager = chatManager;

		view.setPresenter(this);

		roomManager.addRoomInvitationReceivedHandler(new RoomInvitationHandler() {
			@Override
			public void onRoomInvitation(final RoomInvitationEvent event) {
				final RoomInvitation invitation = event.getRoomInvitation();

				logger.info("Invitation : " + invitation.getReason());

				final Room room = roomManager.acceptRoomInvitation(invitation);

				placeController.goTo(new ChessGamePlace(room.getURI()));
			}
		});
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		panel.setWidget(view);
	}

	@Override
	public void playChess() {
		final Chat chat = chatManager.openChat(new ChatProperties(XmppURI.uri("arbiter@games.localhost")), true);
		chat.send(new Message("play"));
	}

}

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

import com.calclab.emite.core.client.xmpp.stanzas.Message;
import com.calclab.emite.core.client.xmpp.stanzas.XmppURI;
import com.calclab.emite.im.client.chat.Chat;
import com.calclab.emite.im.client.chat.ChatProperties;
import com.calclab.emite.im.client.chat.pair.PairChatManagerImpl;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import es.udc.pfc.gameweb.client.layout.AbstractPage;

public class WelcomePage extends AbstractPage implements WelcomeView.Presenter {

	private final WelcomeView view;
	private final PairChatManagerImpl chatManager;

	@Inject
	public WelcomePage(EventBus eventBus, WelcomeView view, PairChatManagerImpl chatManager) {
		super(eventBus);
		
		super.setPageCanClose(false);
		super.setPageTitle("Welcome");
		
		this.view = view;
		this.chatManager = chatManager;

		view.setPresenter(this);
	}
	
	@Override
	public void playChess() {
		System.out.println("presenter playChess");
		final Chat chat = chatManager.openChat(new ChatProperties(XmppURI.uri("arbiter@games.localhost")), true);
		chat.send(new Message("play"));
		chat.close();
	}

	@Override
	public boolean willClose() {
		return false;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}

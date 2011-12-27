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

import static com.google.common.base.Preconditions.checkNotNull;

import com.calclab.emite.core.XmppURI;
import com.calclab.emite.core.session.XmppSession;
import com.calclab.emite.core.stanzas.Message;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import es.udc.pfc.gameweb.client.layout.AbstractPage;

public final class WelcomePage extends AbstractPage implements WelcomeView.Presenter {

	private final WelcomeView view;
	private final XmppSession session;

	@Inject
	public WelcomePage(final EventBus eventBus, final WelcomeView view, final XmppSession session) {
		super(eventBus);
		super.setPageCanClose(false);
		super.setPageTitle(WelcomeMessages.msg.pageTitle());

		this.view = checkNotNull(view);
		this.session = checkNotNull(session);

		view.setPresenter(this);
	}

	@Override
	public final void play(final String game) {
		final Message msg = new Message("play:" + game);
		msg.setTo(XmppURI.uri("games.localhost"));
		session.send(msg);
	}

	@Override
	public final boolean willClose() {
		return false;
	}

	@Override
	public final Widget asWidget() {
		return view.asWidget();
	}

}

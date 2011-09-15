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

package es.udc.pfc.gameweb.client;

import com.calclab.emite.core.client.xmpp.session.SessionState;
import com.calclab.emite.core.client.xmpp.session.SessionStateChangedEvent;
import com.calclab.emite.core.client.xmpp.session.XmppSession;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StatusWidget extends Composite implements SessionStateChangedEvent.Handler {

	private final Label image = new Label("?"); // TODO: make this an image
	private final Label status = new Label("Unknown");
	
	private final XmppSession session;

	@Inject
	public StatusWidget(final XmppSession session) {
		this.session = session;
		
		final HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(10);
		panel.add(image);
		panel.add(status);

		session.addSessionStateChangedHandler(true, this);

		initWidget(panel);
	}
	
	@Override
	public void onSessionStateChanged(final SessionStateChangedEvent event) {
		status.setText(event.getState().toString());

		if (event.is(SessionState.disconnected)) {
			image.setText("D");
		} else if (event.is(SessionState.loggedIn)) {
			image.setText("L");
		} else if (event.is(SessionState.error)) {
			image.setText("E");
		} else {
			image.setText("C");
		}
	}
	
	@UiHandler("image")
	public void onStatusClicked(final ClickEvent event) {
		if (!SessionState.isDisconnected(session.getSessionState())) {
			Cookies.removeCookie("emite.cookies.pause");
			session.logout();
		}
	}

}

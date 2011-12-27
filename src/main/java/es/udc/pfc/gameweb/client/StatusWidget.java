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

import com.calclab.emite.core.events.SessionStatusChangedEvent;
import com.calclab.emite.core.session.SessionStatus;
import com.calclab.emite.core.session.XmppSession;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StatusWidget extends Composite implements SessionStatusChangedEvent.Handler {

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

		session.addSessionStatusChangedHandler(this, true);

		initWidget(panel);
	}
	
	@Override
	public void onSessionStatusChanged(final SessionStatusChangedEvent event) {
		status.setText(event.getStatus().toString());

		if (SessionStatus.disconnected.equals(event.getStatus())) {
			image.setText("D");
		} else if (SessionStatus.loggedIn.equals(event.getStatus())) {
			image.setText("L");
		} else if (SessionStatus.error.equals(event.getStatus())) {
			image.setText("E");
		} else {
			image.setText("C");
		}
	}
	
	@UiHandler("image")
	public void onStatusClicked(final ClickEvent event) {
		if (!SessionStatus.isDisconnected(session.getStatus())) {
			Cookies.removeCookie("emite.cookies.pause");
			session.logout();
		}
	}

}

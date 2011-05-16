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

package es.udc.pfc.gameweb.client.ui;

import com.calclab.emite.core.client.events.StateChangedEvent;
import com.calclab.emite.core.client.events.StateChangedHandler;
import com.calclab.emite.core.client.xmpp.session.SessionStates;
import com.calclab.emite.core.client.xmpp.session.XmppSession;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StatusWidget extends Composite {

	private final Label image = new Label("?"); // TODO: make this an image
	private final Label status = new Label("Unknown");

	@Inject
	public StatusWidget(final XmppSession session) {
		final HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(10);
		panel.add(image);
		panel.add(status);

		session.addSessionStateChangedHandler(true, new StateChangedHandler() {
			@Override
			public void onStateChanged(final StateChangedEvent event) {
				status.setText(event.getState());

				if (event.is(SessionStates.disconnected)) {
					image.setText("D");
				} else if (event.is(SessionStates.loggedIn)) {
					image.setText("L");
				} else if (event.is(SessionStates.error)) {
					image.setText("E");
				} else {
					image.setText("C");
				}
			}
		});

		initWidget(panel);
	}

}

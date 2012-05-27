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

package es.udc.pfc.gameweb.client.navbar;

import static com.google.common.base.Preconditions.checkNotNull;

import com.calclab.emite.core.events.SessionStatusChangedEvent;
import com.calclab.emite.core.session.SessionStatus;
import com.calclab.emite.core.session.XmppSession;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public final class NavWidget extends Composite implements SessionStatusChangedEvent.Handler {
	
	protected interface Binder extends UiBinder<Widget, NavWidget> {
	}
	
	private static final Binder uiBinder = GWT.create(Binder.class);

	private final XmppSession session;

	@UiField
	protected Label status;
	
	@Inject
	public NavWidget(final XmppSession session) {
		this.session = checkNotNull(session);
		
		initWidget(uiBinder.createAndBindUi(this));
		
		session.addSessionStatusChangedHandler(this, true);
	}
	
	@Override
	public void onSessionStatusChanged(final SessionStatusChangedEvent event) {
		status.setText(NavMessages.msg.statusMessage(event.getStatus()));
	}
	
	@UiHandler("status")
	public void onStatusClicked(final ClickEvent event) {
		if (!SessionStatus.isDisconnected(session.getStatus())) {
			Cookies.removeCookie("emite.cookies.pause");
			session.logout();
		}
	}

}

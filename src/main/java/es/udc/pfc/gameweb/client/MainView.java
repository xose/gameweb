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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import es.udc.pfc.gameweb.client.layout.LayoutView;

@Singleton
public final class MainView extends Composite {

	protected interface Binder extends UiBinder<Widget, MainView> {
	}
	
	private static final Binder uiBinder = GWT.create(Binder.class);

	@UiField
	AcceptsOneWidget mainPanel;

	@UiField
	AcceptsOneWidget statusPanel;

	@Inject
	public MainView(final LayoutView layoutView, final StatusWidget statusWidget) {
		initWidget(uiBinder.createAndBindUi(this));
		
		mainPanel.setWidget(layoutView);
		statusPanel.setWidget(statusWidget);
	}

}

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

import com.calclab.emite.browser.client.BrowserModule;
import com.calclab.emite.core.client.CoreModule;
import com.calclab.emite.im.client.ImModule;
import com.calclab.emite.reconnect.client.ReconnectModule;
import com.calclab.emite.xep.muc.client.MucModule;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import es.udc.pfc.gameweb.client.chess.ChessGameActivity;
import es.udc.pfc.gameweb.client.chess.ui.ChessGameView;
import es.udc.pfc.gameweb.client.chess.ui.ChessGameViewImpl;
import es.udc.pfc.gameweb.client.ui.MainView;
import es.udc.pfc.gameweb.client.ui.StatusWidget;
import es.udc.pfc.gameweb.client.welcome.WelcomeActivity;
import es.udc.pfc.gameweb.client.welcome.WelcomePlace;
import es.udc.pfc.gameweb.client.welcome.ui.WelcomeView;
import es.udc.pfc.gameweb.client.welcome.ui.WelcomeViewImpl;

public class GameGinModule extends AbstractGinModule {

	@Override
	protected void configure() {
		// Emite
		install(new CoreModule());
		install(new ImModule());
		install(new MucModule());
		install(new BrowserModule());
		install(new ReconnectModule());

		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
		bind(PlaceHistoryMapper.class).to(GamePlaceHistoryMapper.class).in(Singleton.class);

		// Activities
		bind(MainActivityMapper.class).in(Singleton.class);

		bind(WelcomeActivity.class);
		bind(ChessGameActivity.class);

		// Views
		bind(MainView.class).in(Singleton.class);
		bind(WelcomeView.class).to(WelcomeViewImpl.class);
		bind(ChessGameView.class).to(ChessGameViewImpl.class);

		// Widgets
		bind(StatusWidget.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	public PlaceHistoryHandler getHistoryHandler(final PlaceController placeController, final PlaceHistoryMapper historyMapper, final EventBus eventBus) {
		final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		historyHandler.register(placeController, eventBus, new WelcomePlace());

		return historyHandler;
	}

	@Provides
	@Singleton
	public PlaceController getPlaceController(final EventBus eventBus) {
		return new PlaceController(eventBus);
	}

}

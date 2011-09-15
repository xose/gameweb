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

import com.calclab.emite.core.client.CoreModule;
import com.calclab.emite.im.client.ImModule;
import com.calclab.emite.reconnect.client.ReconnectModule;
import com.calclab.emite.xep.muc.client.MucModule;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import es.udc.pfc.gameweb.client.chess.ChessGameView;
import es.udc.pfc.gameweb.client.chess.ChessGameViewImpl;
import es.udc.pfc.gameweb.client.layout.LayoutViewTabs;
import es.udc.pfc.gameweb.client.welcome.WelcomeView;
import es.udc.pfc.gameweb.client.welcome.WelcomeViewImpl;

public class GameGinModule extends AbstractGinModule {

	@Override
	protected void configure() {
		// Emite
		install(new CoreModule());
		install(new ImModule());
		install(new MucModule());
		install(new ReconnectModule());

		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
		
		// Pages
		install(new GinFactoryModuleBuilder().build(PageFactory.class));

		// Views
		bind(MainView.class).in(Singleton.class);
		bind(WelcomeView.class).to(WelcomeViewImpl.class);
		bind(ChessGameView.class).to(ChessGameViewImpl.class);
		
		bind(LayoutViewTabs.class).in(Singleton.class);

		// Widgets
		bind(StatusWidget.class).in(Singleton.class);
		
		bind(GameWebManager.class).asEagerSingleton();
	}

}

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

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import es.udc.pfc.gameweb.client.chess.ChessGameActivity;
import es.udc.pfc.gameweb.client.chess.ChessGamePlace;
import es.udc.pfc.gameweb.client.welcome.WelcomeActivity;

@Singleton
public class MainActivityMapper implements ActivityMapper {

	private final Provider<WelcomeActivity> welcomeActivityProvider;
	private final Provider<ChessGameActivity> chessGameActivityProvider;

	@Inject
	public MainActivityMapper(final Provider<WelcomeActivity> welcomeActivityProvider, final Provider<ChessGameActivity> chessGameActivityProvider) {
		this.welcomeActivityProvider = welcomeActivityProvider;
		this.chessGameActivityProvider = chessGameActivityProvider;
	}

	@Override
	public Activity getActivity(final Place place) {
		if (place instanceof ChessGamePlace)
			return chessGameActivityProvider.get().withPlace((ChessGamePlace) place);

		return welcomeActivityProvider.get();
	}

}

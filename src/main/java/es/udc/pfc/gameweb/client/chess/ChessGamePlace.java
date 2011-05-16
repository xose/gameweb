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

package es.udc.pfc.gameweb.client.chess;

import com.calclab.emite.core.client.xmpp.stanzas.XmppURI;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ChessGamePlace extends Place {

	private final XmppURI roomJID;

	public ChessGamePlace(final XmppURI roomJID) {
		this.roomJID = roomJID;
	}

	public XmppURI getRoomJID() {
		return roomJID;
	}

	@Prefix("ChessGame")
	public static class Tokenizer implements PlaceTokenizer<ChessGamePlace> {
		@Override
		public String getToken(final ChessGamePlace place) {
			return place.getRoomJID().toString();
		}

		@Override
		public ChessGamePlace getPlace(final String token) {
			return new ChessGamePlace(XmppURI.uri(token));
		}
	}

}

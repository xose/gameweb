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

package es.udc.pfc.gameweb.client.board;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import es.udc.pfc.gamelib.board.Position;

public class PositionClickedEvent extends GwtEvent<PositionClickedEvent.Handler> {

	public interface Handler extends EventHandler {
		void onPositionClicked(PositionClickedEvent event);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final Position position;

	protected PositionClickedEvent(final Position position) {
		this.position = position;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(final Handler handler) {
		handler.onPositionClicked(this);
	}
}

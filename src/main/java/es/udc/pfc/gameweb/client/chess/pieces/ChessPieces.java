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

package es.udc.pfc.gameweb.client.chess.pieces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface ChessPieces extends ClientBundle {

	public static final ChessPieces INSTANCE = GWT.create(ChessPieces.class);

	@ImageOptions(preventInlining=true)
	ImageResource bb();

	@ImageOptions(preventInlining=true)
	ImageResource bk();

	@ImageOptions(preventInlining=true)
	ImageResource bn();

	@ImageOptions(preventInlining=true)
	ImageResource bp();

	@ImageOptions(preventInlining=true)
	ImageResource bq();

	@ImageOptions(preventInlining=true)
	ImageResource br();

	@ImageOptions(preventInlining=true)
	ImageResource wb();

	@ImageOptions(preventInlining=true)
	ImageResource wk();

	@ImageOptions(preventInlining=true)
	ImageResource wn();

	@ImageOptions(preventInlining=true)
	ImageResource wp();

	@ImageOptions(preventInlining=true)
	ImageResource wq();

	@ImageOptions(preventInlining=true)
	ImageResource wr();
}

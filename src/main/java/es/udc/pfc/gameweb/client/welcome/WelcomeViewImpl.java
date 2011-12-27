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

package es.udc.pfc.gameweb.client.welcome;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public final class WelcomeViewImpl extends Composite implements WelcomeView {

	@UiTemplate("WelcomeView.ui.xml")
	protected interface Binder extends UiBinder<Widget, WelcomeViewImpl> {
	}
	
	private static final Binder uiBinder = GWT.create(Binder.class);

	@Nullable private Presenter presenter;
	
	public WelcomeViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("playMiniChess")
	protected final void onMiniChessClick(final ClickEvent event) {
		if (presenter != null) {
			presenter.play("minichess");
		}
	}
	
	@Override
	public final void setPresenter(final Presenter presenter) {
		this.presenter = checkNotNull(presenter);
	}

}

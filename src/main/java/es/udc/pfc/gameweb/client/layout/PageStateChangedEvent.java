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

package es.udc.pfc.gameweb.client.layout;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gwt.resources.client.ImageResource;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class PageStateChangedEvent extends Event<PageStateChangedEvent.Handler> {

	public interface Handler {
		void onPageStateChanged(PageStateChangedEvent event);
	}

	private static final Type<Handler> TYPE = new Type<Handler>();
	
	public static HandlerRegistration bind(EventBus eventBus, Page source, Handler handler) {
		return eventBus.addHandlerToSource(TYPE, source, handler);
	}
	
	public static void fire(EventBus eventBus, Page page) {
		eventBus.fireEventFromSource(new PageStateChangedEvent(page), page);
	}

	private final Page page;

	protected PageStateChangedEvent(Page page) {
		this.page = checkNotNull(page);
	}

	public Page getPage() {
		return page;
	}

	public String getPageTitle() {
		return page.getPageTitle();
	}

	public ImageResource getPageIcon() {
		return page.getPageIcon();
	}

	public boolean getPageCanClose() {
		return page.getPageCanClose();
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(final Handler handler) {
		handler.onPageStateChanged(this);
	}
}

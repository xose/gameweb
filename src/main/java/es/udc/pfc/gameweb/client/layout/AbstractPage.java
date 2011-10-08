package es.udc.pfc.gameweb.client.layout;

import com.google.gwt.resources.client.ImageResource;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public abstract class AbstractPage implements Page {

	protected final EventBus eventBus;
	
	private String title = "";
	private ImageResource icon = null;
	private boolean canClose = true;
	
	protected AbstractPage(final EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public final String getPageTitle() {
		return title;
	}
	
	protected final void setPageTitle(String title) {
		this.title = title;

		eventBus.fireEventFromSource(new PageStateChangedEvent(this), this);
	}
	
	@Override
	public final ImageResource getPageIcon() {
		return icon;
	}
	
	protected final void setPageIcon(ImageResource icon) {
		this.icon = icon;

		eventBus.fireEventFromSource(new PageStateChangedEvent(this), this);
	}
	
	@Override
	public final boolean getPageCanClose() {
		return canClose;
	}

	protected final void setPageCanClose(boolean canClose) {
		this.canClose = canClose;

		eventBus.fireEventFromSource(new PageStateChangedEvent(this), this);
	}

	@Override
	public final HandlerRegistration addPageStateChangedHandler(final PageStateChangedEvent.Handler handler) {
		handler.onPageStateChanged(new PageStateChangedEvent(this));
		
		return eventBus.addHandlerToSource(PageStateChangedEvent.TYPE, this, handler);
	}

}

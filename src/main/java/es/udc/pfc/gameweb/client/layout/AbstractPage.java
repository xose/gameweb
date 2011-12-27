package es.udc.pfc.gameweb.client.layout;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gwt.resources.client.ImageResource;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public abstract class AbstractPage implements Page {

	protected final EventBus eventBus;
	
	private String title = "";
	private ImageResource icon = null;
	private boolean canClose = true;
	
	protected AbstractPage(final EventBus eventBus) {
		this.eventBus = checkNotNull(eventBus);
	}

	@Override
	public final String getPageTitle() {
		return title;
	}
	
	protected final void setPageTitle(final String title) {
		this.title = checkNotNull(title);

		PageStateChangedEvent.fire(eventBus, this);
	}
	
	@Override
	public final ImageResource getPageIcon() {
		return icon;
	}
	
	protected final void setPageIcon(final ImageResource icon) {
		this.icon = checkNotNull(icon);

		PageStateChangedEvent.fire(eventBus, this);
	}
	
	@Override
	public final boolean getPageCanClose() {
		return canClose;
	}

	protected final void setPageCanClose(boolean canClose) {
		this.canClose = canClose;

		PageStateChangedEvent.fire(eventBus, this);
	}

	@Override
	public final HandlerRegistration addPageStateChangedHandler(final PageStateChangedEvent.Handler handler) {
		handler.onPageStateChanged(new PageStateChangedEvent(this));
		
		return PageStateChangedEvent.bind(eventBus, this, handler);
	}

}

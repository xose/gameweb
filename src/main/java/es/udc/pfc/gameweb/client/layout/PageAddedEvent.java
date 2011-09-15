package es.udc.pfc.gameweb.client.layout;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class PageAddedEvent extends Event<PageAddedEvent.Handler> {

	public interface Handler {
		public void onPageAdded(PageAddedEvent event);
	}
	
	private static final Type<Handler> TYPE = new Type<Handler>();
	
	public static HandlerRegistration bind(EventBus eventBus, Handler handler) {
		return eventBus.addHandler(TYPE, handler);
	}
	
	public static void fire(EventBus eventBus, Page page) {
		eventBus.fireEvent(new PageAddedEvent(page));
	}
	
	private final Page page;

	private PageAddedEvent(Page page) {
		this.page = page;
	}

	public Page getPage() {
		return page;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onPageAdded(this);
	}
	
}

package es.udc.pfc.gameweb.client.layout;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
public class LayoutViewStack extends Composite implements LayoutView {

	private final EventBus eventBus;

	private final LinkedList<Page> pages;
	private final SimplePanel current;
	
	@Inject
	public LayoutViewStack(EventBus eventBus) {
		this.eventBus = checkNotNull(eventBus);
		this.pages = Lists.newLinkedList();
		this.current = new SimplePanel();
		
		PageAddedEvent.bind(eventBus, this);
		PageClosedEvent.bind(eventBus, this);
		
		initWidget(current);
	}
	
	@Override
	public void onPageAdded(PageAddedEvent event) {
		final Page page = event.getPage();
		
		GWT.log("adding page "+page.getPageTitle());
		
		pages.add(page);
		current.setWidget(page);
	}
	
	@Override
	public void onPageClosed(PageClosedEvent event) {
		final Page page = event.getPage();
		
		if (page.willClose()) {
			pages.remove(page);
			current.setWidget(pages.peek());
		}
	}

}

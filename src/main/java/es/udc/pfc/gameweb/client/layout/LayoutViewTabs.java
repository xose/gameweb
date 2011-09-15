package es.udc.pfc.gameweb.client.layout;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
public class LayoutViewTabs extends Composite implements LayoutView, CloseHandler<Page> {

	private final TabLayoutPanel tabPanel;
	
	@Inject
	public LayoutViewTabs(EventBus eventBus) {
		PageAddedEvent.bind(eventBus, this);
		
		tabPanel = new TabLayoutPanel(3, Unit.EM);
		tabPanel.setHeight("100%");
		tabPanel.setAnimationDuration(200);
		
		initWidget(tabPanel);
	}
	
	@Override
	public void onPageAdded(PageAddedEvent event) {
		final Page page = event.getPage();
		
		final TabTitleWidget tabTitle = new TabTitleWidget(page);
		tabTitle.addCloseHandler(this);
		
		tabPanel.add(page, tabTitle);
		tabPanel.selectTab(page);
	}
	
	@Override
	public void onClose(CloseEvent<Page> event) {
		final Page page = event.getTarget();
		
		if (page.willClose()) {
			tabPanel.remove(page.asWidget());
		}
	}

}

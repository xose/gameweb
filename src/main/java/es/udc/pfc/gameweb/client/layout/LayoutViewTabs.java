package es.udc.pfc.gameweb.client.layout;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

@Singleton
public class LayoutViewTabs extends Composite implements LayoutView {

	private final EventBus eventBus;
	private final TabLayoutPanel tabPanel;
	
	@Inject
	public LayoutViewTabs(EventBus eventBus) {
		this.eventBus = checkNotNull(eventBus);
		
		PageAddedEvent.bind(eventBus, this);
		PageClosedEvent.bind(eventBus, this);
		
		tabPanel = new TabLayoutPanel(3, Unit.EM);
		tabPanel.setAnimationDuration(200);
		
		initWidget(tabPanel);
	}
	
	@Override
	public void onPageAdded(PageAddedEvent event) {
		final Page page = event.getPage();
		
		final TabTitleWidget tabTitle = new TabTitleWidget(eventBus, page);
		
		tabPanel.add(page, tabTitle);
		tabPanel.selectTab(page);
	}
	
	@Override
	public void onPageClosed(PageClosedEvent event) {
		final Page page = event.getPage();
		
		if (page.willClose()) {
			tabPanel.remove(page.asWidget());
		}
	}

}

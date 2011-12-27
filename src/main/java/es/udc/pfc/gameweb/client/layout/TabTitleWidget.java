package es.udc.pfc.gameweb.client.layout;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

class TabTitleWidget extends Composite implements PageStateChangedEvent.Handler {

	private static final Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, TabTitleWidget> {
	}

	@UiField
	protected Image iconImage;

	@UiField
	protected HasText titleLabel;

	@UiField
	protected Widget closeButton;

	private final EventBus eventBus;
	private final Page page;

	protected TabTitleWidget(EventBus eventBus, Page page) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.eventBus = checkNotNull(eventBus);
		this.page = checkNotNull(page);
		
		page.addPageStateChangedHandler(this);
	}

	@UiHandler("closeButton")
	protected void onClick(ClickEvent event) {
		PageClosedEvent.fire(eventBus, page);
	}

	@Override
	public void onPageStateChanged(PageStateChangedEvent event) {
		titleLabel.setText(event.getPageTitle());
		closeButton.setVisible(event.getPageCanClose());

		final ImageResource icon = event.getPageIcon();
		iconImage.setVisible(icon != null);
		if (icon != null) {
			iconImage.setResource(event.getPageIcon());
		}
	}

}

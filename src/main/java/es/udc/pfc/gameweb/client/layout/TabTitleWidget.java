package es.udc.pfc.gameweb.client.layout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class TabTitleWidget extends Composite implements PageStateChangedEvent.Handler, HasCloseHandlers<Page> {

	private static final Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, TabTitleWidget> {
	}

	@UiField
	protected Image iconImage;

	@UiField
	protected HasText titleLabel;

	@UiField
	protected Widget closeButton;

	private final Page page;

	public TabTitleWidget(Page page) {
		initWidget(uiBinder.createAndBindUi(this));

		page.addPageStateChangedHandler(this);

		this.page = page;
	}

	@UiHandler("closeButton")
	protected void onClick(ClickEvent e) {
		CloseEvent.fire(this, page);
	}

	@Override
	public final HandlerRegistration addCloseHandler(CloseHandler<Page> handler) {
		return addHandler(handler, CloseEvent.getType());
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

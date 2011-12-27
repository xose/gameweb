package es.udc.pfc.gameweb.client.layout;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

public interface Page extends IsWidget {

	String getPageTitle();
	
	ImageResource getPageIcon();
	
	boolean getPageCanClose();
	
	
	// Callbacks
	
	boolean willClose();

	HandlerRegistration addPageStateChangedHandler(PageStateChangedEvent.Handler handler);
}

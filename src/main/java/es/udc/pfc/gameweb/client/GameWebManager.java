package es.udc.pfc.gameweb.client;

import com.calclab.emite.xep.muc.client.RoomChat;
import com.calclab.emite.xep.muc.client.RoomInvitation;
import com.calclab.emite.xep.muc.client.RoomInvitationReceivedEvent;
import com.calclab.emite.xep.muc.client.RoomChatManager;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import es.udc.pfc.gameweb.client.layout.PageAddedEvent;

@Singleton
public class GameWebManager implements RoomInvitationReceivedEvent.Handler {
	
	private final EventBus eventBus;
	private final PageFactory pageFactory;
	private final RoomChatManager roomManager;
	
	@Inject
	public GameWebManager(EventBus eventBus, MainView mainView, PageFactory pageFactory, RoomChatManager roomManager) {
		this.eventBus = eventBus;
		this.pageFactory = pageFactory;
		this.roomManager = roomManager;
		
		roomManager.addRoomInvitationReceivedHandler(this);
		
		PageAddedEvent.fire(eventBus, pageFactory.getWelcomePage());
		
		RootLayoutPanel.get().add(mainView);
	}

	@Override
	public void onRoomInvitationReceived(RoomInvitationReceivedEvent event) {
		final RoomInvitation invitation = event.getRoomInvitation();

		final RoomChat room = roomManager.acceptRoomInvitation(invitation);
		PageAddedEvent.fire(eventBus, pageFactory.getChessGamePage(room));
	}
	
}

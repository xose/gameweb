package es.udc.pfc.gameweb.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.calclab.emite.xep.muc.RoomChatManager;
import com.calclab.emite.xep.muc.RoomInvitation;
import com.calclab.emite.xep.muc.events.RoomInvitationReceivedEvent;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import es.udc.pfc.gamelib.chess.MiniChessGame;
import es.udc.pfc.gameweb.client.layout.PageAddedEvent;

@Singleton
public class GameWebManager implements RoomInvitationReceivedEvent.Handler {
	
	private final EventBus eventBus;
	private final PageFactory pageFactory;
	
	@Inject
	public GameWebManager(final EventBus eventBus, final MainView mainView, final PageFactory pageFactory, final RoomChatManager roomManager) {
		this.eventBus = checkNotNull(eventBus);
		this.pageFactory = checkNotNull(pageFactory);
		
		roomManager.addRoomInvitationReceivedHandler(this);
		PageAddedEvent.fire(eventBus, pageFactory.getWelcomePage());
		RootLayoutPanel.get().add(mainView);
	}

	@Override
	public final void onRoomInvitationReceived(final RoomInvitationReceivedEvent event) {
		final RoomInvitation invitation = event.getRoomInvitation();
		
		if ("minichess".equals(invitation.getReason())) {
			PageAddedEvent.fire(eventBus, pageFactory.getChessGamePage(invitation, new MiniChessGame()));
		}
	}
	
}

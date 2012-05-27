package es.udc.pfc.gameweb.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.calclab.emite.core.XmppURI;
import com.calclab.emite.core.session.XmppSession;
import com.calclab.emite.core.stanzas.Message;
import com.calclab.emite.xep.muc.RoomChatManager;
import com.calclab.emite.xep.muc.RoomInvitation;
import com.calclab.emite.xep.muc.events.RoomInvitationReceivedEvent;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import es.udc.pfc.gamelib.chess.MiniChessGame;
import es.udc.pfc.gameweb.client.layout.PageAddedEvent;

@Singleton
public final class GameWebManager implements RoomInvitationReceivedEvent.Handler {
	
	private final EventBus eventBus;
	private final PageFactory pageFactory;
	private final XmppSession session;
	
	@Inject
	public GameWebManager(final EventBus eventBus, final MainView mainView, final PageFactory pageFactory, final XmppSession session, final RoomChatManager roomManager) {
		this.eventBus = checkNotNull(eventBus);
		this.pageFactory = checkNotNull(pageFactory);
		this.session = checkNotNull(session);
		
		roomManager.addRoomInvitationReceivedHandler(this);
		PageAddedEvent.fire(eventBus, pageFactory.getWelcomePage());
		RootPanel.get().add(mainView);
		
		if (!Canvas.isSupported()) {
			Window.alert("Your browser does not support Canvas. You can't play :(");
		} else {
			//play("minichess");
		}
	}
	
	private final void play(final String game) {
		final Message msg = new Message();
		msg.addExtension("play", "urn:xmpp:gamepfc").setAttribute("game", game);
		msg.setTo(XmppURI.uri("games.localhost"));
		session.send(msg);
	}

	@Override
	public final void onRoomInvitationReceived(final RoomInvitationReceivedEvent event) {
		final RoomInvitation invitation = event.getRoomInvitation();
		
		if ("minichess".equals(invitation.getReason())) {
			PageAddedEvent.fire(eventBus, pageFactory.getChessGamePage(invitation, new MiniChessGame()));
		}
	}
	
}

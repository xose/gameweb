/**
 * PALLADIUM v1.4
 * Description: the session janitor tool
 * Authors: Stefan Strigler, Valérian Saliou
 * License: GNU GPL
 * Last revision: 24/10/10
 */

package org.xmpp.palladium;

import java.util.Enumeration;

public class Janitor implements Runnable {
	public static final int SLEEPMILLIS = 1000;
	
	private boolean keep_running = true;
	
	public void run() {
		while (this.keep_running) {
			for (Enumeration e = Session.getSessions(); e.hasMoreElements();) {
				Session sess = (Session) e.nextElement();
				
				// Stop inactive sessions
				if (System.currentTimeMillis() - sess.getLastActive() > Session.MAX_INACTIVITY * 1000) {
					if (PalladiumServlet.DEBUG)
						System.err.println("Session timed out: " + sess.getSID());
					sess.terminate();
				}
			}
			
			try {
				Thread.sleep(SLEEPMILLIS);
			}
			
			catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	public void stop() {
		this.keep_running = false;
	}
}

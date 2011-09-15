/**
 * PALLADIUM v1.4
 * Description: the session tool
 * Authors: Stefan Strigler, Valérian Saliou
 * License: GNU GPL
 * Last revision: 24/10/10
 */

package org.xmpp.palladium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Session with HTTP Bind
public class Session {
	// Content-type header
	public static final String DEFAULT_CONTENT = "text/xml; charset=utf-8";
	
	// Maximum inactivity period
	public static final int MAX_INACTIVITY = 60;
	
	// Maximum number of simultaneous requests allowed
	public static final int MAX_REQUESTS = 5;
	
	// Maximum time to wait for XMPP server replies
	public static final int MAX_WAIT = 300;
	
	// Shortest polling period
	public static final int MIN_POLLING = 2;
	
	// Sleep time
	private static final int READ_TIMEOUT = 1;
	
	// Socket timeout
	private static final int SOCKET_TIMEOUT = 6000;
	
	// Default XMPP port to connect
	public static final int DEFAULT_XMPPPORT = 5222;
	
	// Session starting
	protected static final String SESS_START = "starting";
	
	// Session active
	protected static final String SESS_ACTIVE = "active";
	
	// Session terminate
	protected static final String SESS_TERM = "term";
	
	private static Hashtable sessions = new Hashtable();
	
	private static TransformerFactory tff = TransformerFactory.newInstance();
	
	private static String createSessionID(int len) {
		String charlist = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
		
		Random rand = new Random();
		
		String str = new String();
		
		for (int i = 0; i < len; i++)
			str += charlist.charAt(rand.nextInt(charlist.length()));
		
		return str;
	}
	
	public static Session getSession(String sid) {
		return (Session) sessions.get(sid);
	}
	
	public static Enumeration getSessions() {
		return sessions.elements();
	}
	
	public static int getNumSessions() {
		return sessions.size();
	}
	
	public static void stopSessions() {
		for (Enumeration e = sessions.elements(); e.hasMoreElements();)
			((Session) e.nextElement()).terminate();
	}
	
	private String authid;
	
	boolean authidSent = false;
	
	boolean streamFeatures = false;
	
	private String content = DEFAULT_CONTENT;
	
	private DocumentBuilder db;
	
	private int hold = MAX_REQUESTS - 1;
	
	private String inQueue = "";
	
	private BufferedReader br;
	
	private String key;
	
	private long lastActive;
	
	private long lastPoll = 0;
	
	private OutputStreamWriter osw;
	
	private TreeMap responses;
	
	private String status = SESS_START;
	
	private String sid;
	
	public Socket sock;
	
	private String to;
	
	private DNSUtil.HostAddress host = null;
	
	private int wait = MAX_WAIT;
	
	private String xmllang = null;
	
	private boolean reinit = false;
	
	private boolean secure = false;
	
	private boolean pauseForHandshake = false;
	
	private Pattern streamPattern;
	
	private Pattern stream10Test;
	
	private Pattern stream10Pattern;
	
	// Create a new session and connect to the XMPP server
	public Session(String to, String route, String xmllang) throws UnknownHostException, IOException {
		this.to = to;
		this.xmllang = xmllang;
		
		int port = DEFAULT_XMPPPORT;
		
		this.sock = new Socket();
		this.setLastActive();
		
		try {
			this.db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		
		catch (Exception e) { }
		
		// First, try connecting throught the 'route' attribute.
		if (route != null && !route.equals("")) {
			PalladiumServlet.dbg("Trying to use 'route' attribute to open a socket...", 3);
			
			if (route.startsWith("xmpp:"))
				route = route.substring("xmpp:".length());
			
			int i;
			
			// Has 'route' the optional port?
			if ((i = route.lastIndexOf(":")) != -1) {
				try {
					int p = Integer.parseInt(route.substring(i + 1));
					
					if (p >= 0 && p <= 65535) {
						port = p;
						PalladiumServlet.dbg("...route attribute holds a valid port (" + port + ").", 3);
					}
				}
				
				catch (NumberFormatException nfe) { }
				
				route = route.substring(0, i);
			}
			
			PalladiumServlet.dbg("Trying to open a socket to '" + route + "', using port " + port + ".", 3);
			
			try {
				this.sock.connect(new InetSocketAddress(route, port), SOCKET_TIMEOUT);
			}
			
			catch (Exception e) {
				PalladiumServlet.dbg("Failed to open a socket using the 'route' attribute", 3);
			}
		}
		
		// If no socket has been opened, try connecting trough the 'to' attribute
		if (this.sock == null || !this.sock.isConnected()) {
			this.sock = new Socket();
			PalladiumServlet.dbg("Trying to use 'to' attribute to open a socket...", 3);
			
			host = DNSUtil.resolveXMPPServerDomain(to, DEFAULT_XMPPPORT);
			
			try {
				PalladiumServlet.dbg("Trying to open a socket to '" + host.getHost() + "', using port " + host.getPort() + ".", 3);
				this.sock.connect(new InetSocketAddress(host.getHost(), host.getPort()), SOCKET_TIMEOUT);
			}
			
			catch (UnknownHostException uhe) {
				PalladiumServlet.dbg("Failed to open a socket using the 'to' attribute: " + uhe.toString(), 3);
				throw uhe;
			
			}
			
			catch (IOException ioe) {
				PalladiumServlet.dbg("Failed to open a socket using the 'to' attribute: " + ioe.toString(), 3);
				throw ioe;
			}
		}
		
		// At this point, we either have a socket, or an exception has already been thrown
		try {
			if (this.sock.isConnected())
				PalladiumServlet.dbg("Succesfully connected to " + to, 2);
			
			this.sock.setSoTimeout(SOCKET_TIMEOUT);
			
			this.osw = new OutputStreamWriter(this.sock.getOutputStream(),
					"UTF-8");
			
			this.osw.write("<stream:stream to='" + this.to + "'"
					+ appendXMLLang(this.xmllang)
					+ " xmlns='jabber:client'"
					+ " xmlns:stream='http://etherx.jabber.org/streams'"
					+ " version='1.0'" + ">");
			
			this.osw.flush();
			
			// Create unique session id
			while (sessions.get(this.sid = createSessionID(24)) != null);
			
			PalladiumServlet.dbg("creating session with id " + this.sid, 2);
			
			// Register session
			sessions.put(this.sid, this);
			
			// Create list of responses
			responses = new TreeMap();
			
			this.br = new BufferedReader(new InputStreamReader(this.sock.getInputStream(), "UTF-8"));
			
			this.streamPattern = Pattern.compile(".*<stream:stream[^>]*id=['|\"]([^'|^\"]+)['|\"][^>]*>.*", Pattern.DOTALL);
			
			this.stream10Pattern = Pattern.compile(".*<stream:stream[^>]*id=['|\"]([^'|^\"]+)['|\"][^>]*>.*(<stream.*)$", Pattern.DOTALL);
			
			this.stream10Test = Pattern.compile(".*<stream:stream[^>]*version=['|\"]1.0['|\"][^>]*>.*", Pattern.DOTALL);
			
			this.setStatus(SESS_ACTIVE);
		}
		
		catch (IOException ioe) {
			throw ioe;
		}
	}

	// Adds new response to list of known responses.
	public synchronized Response addResponse(Response r) {
		while (this.responses.size() > 0 && this.responses.size() >= Session.MAX_REQUESTS)
			this.responses.remove(this.responses.firstKey());
		
		return (Response) this.responses.put(new Long(r.getRID()), r);
	}

	// Checks InputStream from server for incoming packets blocks until request timeout or packets available
	private int init_retry = 0;
	
	public NodeList checkInQ(long rid) throws IOException {
		NodeList nl = null;
		
		inQueue += this.readFromSocket(rid);
		
		PalladiumServlet.dbg("inQueue: " + inQueue, 2);
		
		if (init_retry < 1000 && (this.authid == null || this.isReinit()) && inQueue.length() > 0) {
			init_retry++;
			
			if (stream10Test.matcher(inQueue).matches()) {
				Matcher m = stream10Pattern.matcher(inQueue);
				
				if (m.matches()) {
					this.authid = m.group(1);
					inQueue = m.group(2);
					PalladiumServlet.dbg("inQueue: " + inQueue, 2);
					streamFeatures = inQueue.length() > 0;
				}
				
				else {
					PalladiumServlet.dbg("failed to get stream features", 2);
					
					try {
						Thread.sleep(5);
					}
					
					catch (InterruptedException ie) { }
					
					// Retry
					return this.checkInQ(rid);
				}
			}
			
			else {
				// Legacy XMPP stream
				Matcher m = streamPattern.matcher(inQueue);
				
				if (m.matches())
					this.authid = m.group(1);
				
				else {
					PalladiumServlet.dbg("failed to get authid", 2);
					
					try {
						Thread.sleep(5);
					}
					
					catch (InterruptedException ie) { }
					
					// Retry
					return this.checkInQ(rid);
				}
			}
			
			// Reset
			init_retry = 0;
		}
		
		// Try to parse it
		if (!inQueue.equals("")) {
			try {
				Document doc = null;
				
				if (streamFeatures)
					doc = db.parse(new InputSource(new StringReader("<doc>" + inQueue + "</doc>")));
				
				else
					try {
						doc = db.parse(new InputSource(new StringReader("<doc xmlns='jabber:client'>" + inQueue + "</doc>")));
					}
					
					catch (SAXException sex) {
						try {
							// Stream closed?
							doc = db.parse(new InputSource(new StringReader("<stream:stream>" + inQueue)));
							this.terminate();
						}
						
						catch (SAXException sex2) { }
					}
				
				if (doc != null)
					nl = doc.getFirstChild().getChildNodes();
				
				// Check for StartTLS
				if (streamFeatures) {
					for (int i = 0; i < nl.item(0).getChildNodes().getLength(); i++) {
						if (nl.item(0).getChildNodes().item(i).getNodeName().equals("starttls")) {
							if (!this.isReinit()) {
								PalladiumServlet.dbg("starttls present, trying to use it", 2);
								this.osw.write("<starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>");
								this.osw.flush();
								
								String response = this.readFromSocket(rid);
								PalladiumServlet.dbg(response, 2);
								
								TrustManager[] trustAllCerts = new TrustManager[] {
									new X509TrustManager() {
										public X509Certificate[] getAcceptedIssuers() {
											return null;
										}
										
										public void checkClientTrusted(X509Certificate[] certs, String authType) { }
										
										public void checkServerTrusted(X509Certificate[] certs, String authType) { }
									}
								};
								
								try {
									SSLContext sc = SSLContext.getInstance("TLS");
									sc.init(null, trustAllCerts, null);
									
									SSLSocketFactory sslFact = sc.getSocketFactory();
									
									SSLSocket tls;
									
									tls = (SSLSocket) sslFact.createSocket(this.sock, this.sock.getInetAddress().getHostName(), this.sock.getPort(), false);
									tls.addHandshakeCompletedListener(new HandShakeFinished(this));
									
									this.pauseForHandshake = true;
									
									PalladiumServlet.dbg("initiating handshake", 2);
									
									tls.startHandshake();
									
									try {
										while (this.pauseForHandshake) {
											PalladiumServlet.dbg(".");
											Thread.sleep(5);
										}
									}
									
									catch (InterruptedException ire) { }
									
									PalladiumServlet.dbg("TLS Handshake complete", 2);
									
									this.sock = tls;
									this.sock.setSoTimeout(SOCKET_TIMEOUT);
									
									this.br = new SSLSocketReader((SSLSocket) tls);
									
									this.osw = new OutputStreamWriter(tls.getOutputStream(), "UTF-8");
									
									// Reset
									this.inQueue = "";
									this.setReinit(true);
									this.osw.write("<stream:stream to='" + this.to + "'" + appendXMLLang(this.getXMLLang()) + " xmlns='jabber:client' " + " xmlns:stream='http://etherx.jabber.org/streams'" + " version='1.0'" + ">");
									this.osw.flush();
									
									return this.checkInQ(rid);
								}
								
								catch (Exception ssle) {
									PalladiumServlet.dbg("STARTTLS failed: " + ssle.toString(), 1);
									
									this.setReinit(false);
									
									if (this.isSecure()) {
										if (!this.sock.getInetAddress().getHostName().equals("localhost") && !this.getResponse(rid).getReq().getServerName().equals(this.sock.getInetAddress().getHostName())) {
											PalladiumServlet.dbg("secure connection requested but failed", 2);
											throw new IOException();
										}
										
										else
											PalladiumServlet.dbg("secure requested and we're local", 1);
									}
									
									else
										PalladiumServlet.dbg("tls failed but we don't need to be secure", 2);
									
									if (this.sock.isClosed()) {
										PalladiumServlet.dbg("socket closed", 1);
										
										// Reconnect
										Socket s = new Socket();
										s.connect(this.sock.getRemoteSocketAddress(), SOCKET_TIMEOUT);
										
										this.sock = s;
										this.sock.setSoTimeout(SOCKET_TIMEOUT);
										this.br = new BufferedReader(new InputStreamReader(this.sock.getInputStream(), "UTF-8"));
										this.osw = new OutputStreamWriter(this.sock.getOutputStream(), "UTF-8");
										
										// Reset
										this.inQueue = "";
										this.setReinit(true);
										
										this.osw.write("<stream:stream to='" + this.to + "'" + appendXMLLang(this.getXMLLang()) + " xmlns='jabber:client' " + " xmlns:stream='http://etherx.jabber.org/streams'" + " version='1.0'" + ">");
										this.osw.flush();
										
										return this.checkInQ(rid);
									}
								}
							}
							
							else
								nl.item(0).removeChild(nl.item(0).getChildNodes().item(i));
						}
					}
				}
				
				if (doc != null) 
					inQueue = "";
			}
			
			catch (SAXException sex3) {
				this.setReinit(false);
				PalladiumServlet.dbg("failed to parse inQueue: " + inQueue + "\n" + sex3.toString(), 1);
				
				return null;
			}
		}
		
		this.setReinit(false);
		this.setLastActive();
		return nl;
	}
	
	private class HandShakeFinished implements javax.net.ssl.HandshakeCompletedListener {
		private Session sess;
		
		public HandShakeFinished(Session sess) {
			this.sess = sess;
		}
		
		public void handshakeCompleted(javax.net.ssl.HandshakeCompletedEvent event) {
			PalladiumServlet.dbg("startTLS: Handshake is complete", 2);
			
			this.sess.pauseForHandshake = false;
			return;
		}
	}
	
	// Checks whether given request ID is valid within context of this session.
	public synchronized boolean checkValidRID(long rid) {
		try {
			if (rid <= ((Long) this.responses.lastKey()).longValue() + MAX_REQUESTS && rid >= ((Long) this.responses.firstKey()).longValue())
				return true;
			
			else {
				PalladiumServlet.dbg("invalid request id: " + rid + " (last: " + ((Long) this.responses.lastKey()).longValue() + ")", 1);
				
				return false;
			}
		}
		
		catch (NoSuchElementException e) {
			return false;
		}
	}
	
	public String getAuthid() {
		return this.authid;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public int getHold() {
		return this.hold;
	}
	
	// Returns the key.
	public synchronized String getKey() {
		return key;
	}
	
	// Returns the lastActive.
	public synchronized long getLastActive() {
		return lastActive;
	}

	// Returns the lastPoll.
	public synchronized long getLastPoll() {
		return lastPoll;
	}
	
	// Lookup response for given request id
	public synchronized Response getResponse(long rid) {
		return (Response) this.responses.get(new Long(rid));
	}
	
	public String getSID() {
		return this.sid;
	}
	
	public String getTo() {
		return this.to;
	}
	
	public int getWait() {
		return this.wait;
	}
	
	public String getXMLLang() {
		return this.xmllang;
	}
	
	public String appendXMLLang(String locale) {
		if(locale != null)
			return " xml:lang='" + locale + "'";
		
		return "";
	}
	
	public synchronized int numPendingRequests() {
		int num_pending = 0;
		Iterator it = this.responses.values().iterator();
		
		while (it.hasNext()) {
			Response r = (Response) it.next();
			
			if (!r.getStatus().equals(Response.STATUS_DONE))
				num_pending++;
		}
		
		return num_pending;
	}
	
	private long lastDoneRID;
	
	public synchronized long getLastDoneRID() {
		return this.lastDoneRID;
	}
	
	// Reads from socket
	private String readFromSocket(long rid) throws IOException {
		String retval = "";
		char buf[] = new char[16];
		int c = 0;
		
		Response r = this.getResponse(rid);
		
		while (!this.sock.isClosed() && !this.isStatus(SESS_TERM)) {
			this.setLastActive();
			try {
				if (this.br.ready()) {
					while (this.br.ready() && (c = this.br.read(buf, 0, buf.length)) >= 0)
						retval += new String(buf, 0, c);
						break;
				}
				
				else {
					if ((this.hold == 0 && r != null && System.currentTimeMillis() - r.getCDate() > 200) || (this.hold > 0 && ((r != null && System.currentTimeMillis() - r.getCDate() >= this.getWait() * 1000) || this.numPendingRequests() > this.getHold() || !retval.equals(""))) || r.isAborted()) {
						PalladiumServlet.dbg("readFromSocket done for " + rid, 3);
						break;
					}
					
					try {
						// Wait for incoming packets
						Thread.sleep(READ_TIMEOUT);
					}
					
					catch (InterruptedException ie) {
						System.err.println(ie.toString());
					}
				}
			}
			
			catch (IOException e) {
				System.err.println("Can't read from socket");
				
				this.terminate();
			}
		}
		
		if (this.sock.isClosed()) {
			throw new IOException();
		}
		
		return retval;
	}

	// Sends all nodes in list to remote XMPP server make sure that nodes get
	public Session sendNodes(NodeList nl) {
		// Build a string
		String out = "";
		StreamResult strResult = new StreamResult();
		
		try {
			Transformer tf = tff.newTransformer();
			tf.setOutputProperty("omit-xml-declaration", "yes");
			
			// Loop list
			for (int i = 0; i < nl.getLength(); i++) {
				strResult.setWriter(new StringWriter());
				tf.transform(new DOMSource(nl.item(i)), strResult);
				String tStr = strResult.getWriter().toString();
				out += tStr;
			}
		}
		
		catch (Exception e) {
			PalladiumServlet.dbg("XML.toString(Document): " + e, 1);
		}
		
		try {
			if (this.isReinit()) {
				PalladiumServlet.dbg("Reinitializing Stream!", 2);
				this.osw.write("<stream:stream to='" + this.to + "'" + appendXMLLang(this.getXMLLang()) + " xmlns='jabber:client' " + " xmlns:stream='http://etherx.jabber.org/streams'" + " version='1.0'" + ">");
			}
			
			this.osw.write(out);
			this.osw.flush();
		}
		
		catch (IOException ioe) {
			PalladiumServlet.dbg(this.sid + " failed to write to stream", 1);
		}
		
		return this;
	}
	
	public Session setContent(String content) {
		this.content = content;
		return this;
	}
	
	public Session setHold(int hold) {
		if (hold < MAX_REQUESTS && hold >= 0)
			this.hold = hold;
		return this;
	}
	
	// The key to set.
	public synchronized void setKey(String key) {
		this.key = key;
	}
	
	// Set lastActive to current timestamp
	public synchronized void setLastActive() {
		this.lastActive = System.currentTimeMillis();
	}
	
	public synchronized void setLastDoneRID(long rid) {
		this.lastDoneRID = rid;
	}
	
	// Set lastPoll to current timestamp
	public synchronized void setLastPoll() {
		this.lastPoll = System.currentTimeMillis();
	}
	
	public int setWait(int wait) {
		if (wait < 0)
			wait = 0;
		if (wait > MAX_WAIT)
			wait = MAX_WAIT;
		this.wait = wait;
		return wait;
	}
	
	public Session setXMLLang(String xmllang) {
		this.xmllang = xmllang;
		return this;
	}
	
	// Returns the reinit.
	public synchronized boolean isReinit() {
		return reinit;
	}
	
	// Returns the secure
	public synchronized boolean isSecure() {
		return secure;
	}
	
	// The reinit to set.
	public synchronized void setReinit(boolean reinit) {
		this.reinit = reinit;
	}
	
	public synchronized void setStatus(String status) {
		this.status = status;
	}
	
	public synchronized boolean isStatus(String status) {
		return (this.status == status);
	}
	
	// Kill this session
	public void terminate() {
		PalladiumServlet.dbg("terminating session " + this.getSID(), 2);
		this.setStatus(SESS_TERM);
		synchronized (this.sock) {
			if (!this.sock.isClosed()) {
				try {
					this.osw.write("</stream:stream>");
					this.osw.flush();
					this.sock.close();
				}
				
				catch (IOException ie) { }
			}
			
			this.sock.notifyAll();
		}
		
		sessions.remove(this.sid);
	}

	// The secure to set
	public synchronized void setSecure(boolean secure) {
		this.secure = secure;
	}
}

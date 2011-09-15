/**
 * PALLADIUM v1.4
 * Description: the servlet itself
 * Authors: Stefan Strigler, Valérian Saliou
 * License: GNU GPL
 * Last revision: 24/10/10
 */

package org.xmpp.palladium;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// An implementation of http://xmpp.org/extensions/xep-0124.html
public final class PalladiumServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String APP_VERSION = "1.4";
	
	public static final String APP_NAME = "Palladium";
	
	public static final boolean DEBUG = false;
	
	public static final int DEBUG_LEVEL = 2;
	
	private DocumentBuilder db;
	
	private Janitor janitor;
	
	private static PalladiumServlet srv;
	
	public void init() throws ServletException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			db = dbf.newDocumentBuilder();
		}
		
		catch (ParserConfigurationException e) {
			log("failed to create DocumentBuilderFactory", e);
		}
		
		// Clean up the sessions
		janitor = new Janitor();
		new Thread(janitor).start();
		srv = this;
	}
	
	public void destroy() {
		Session.stopSessions();
		janitor.stop();
	}
	
	public static String hex(byte[] array) {
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
					.toLowerCase().substring(1, 3));
		}
		
		return sb.toString();
	}
	
	public static String sha1(String message) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			return hex(sha.digest(message.getBytes()));
		}
		
		catch (NoSuchAlgorithmException e) { }
		
		return null;
	}
	
	public static void dbg(String msg) {
		dbg(msg, 0);
	}
	
	public static void dbg(String msg, int lvl) {
		if (!DEBUG)
			return;
		if (lvl > DEBUG_LEVEL)
			return;
		
		srv.log("[" + lvl + "] " + msg);
	}

	// Reply to POST requests
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
			long rid = 0;
			try {
				// Parse the request
				Document doc;
				
				synchronized (db) {
					doc = db.parse(request.getInputStream());
				}
				
				Node rootNode = doc.getDocumentElement();
				if (rootNode == null || !rootNode.getNodeName().equals("body"))
					// Not a <body> tag
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				
				else {
					// Got a <body> tag request
					NamedNodeMap attribs = rootNode.getAttributes();
					
					if (attribs.getNamedItem("sid") != null) {
						// This session exists?
						Session sess = Session.getSession(attribs.getNamedItem(
								"sid").getNodeValue());
						
						if (sess != null) {
							dbg("incoming request for " + sess.getSID(), 3);
							
							// Check the validity of the request
							if (attribs.getNamedItem("rid") == null) {
								// RID missing
								dbg("rid missing", 1);
								response.sendError(HttpServletResponse.SC_NOT_FOUND);
								sess.terminate();
							}
							
							else {
								try {
									rid = Integer.parseInt(attribs.getNamedItem("rid").getNodeValue());
								}
								
								catch (NumberFormatException e) {
									dbg("rid not a number", 1);
									response.sendError(HttpServletResponse.SC_BAD_REQUEST);
									return;
								}
								
								Response r = sess.getResponse(rid);
								
								// Re-send
								if (r != null) {
									dbg("resend rid " + rid, 2);
									r.setAborted(true);
									r.send(response);
									return;
								}	
							
								if (!sess.checkValidRID(rid)) {
									dbg("invalid rid " + rid, 1);
									response.sendError(HttpServletResponse.SC_NOT_FOUND);
									sess.terminate();
									return;
								}
							}
							
							dbg("found valid rid " + rid, 3);
							
							// Too many simultaneous requests
							if (sess.numPendingRequests() >= Session.MAX_REQUESTS) {
								dbg("too many simultaneous requests: " + sess.numPendingRequests(), 1);
								response.sendError(HttpServletResponse.SC_FORBIDDEN);
								
								// Kick it!
								sess.terminate();
								return;
							}
							
							// Got a valid request
							Response jresp = new Response(db.newDocument());
							jresp.setRID(rid);
							jresp.setContentType(sess.getContent());
							sess.addResponse(jresp);
							
							try {
								synchronized (sess.sock) {
									// Wait until it's our turn
									long lastrid = sess.getLastDoneRID();
									while (rid != lastrid + 1) {
										if (sess.isStatus(Session.SESS_TERM)) {
											dbg("session terminated for " + rid, 1);
											
											response.sendError(HttpServletResponse.SC_NOT_FOUND);
											sess.sock.notifyAll();
											
											return;
										}
										
										try {
											dbg(rid + " waiting for " + (lastrid + 1), 2);
											
											sess.sock.wait();
											
											dbg("bell for " + rid, 2);
											
											lastrid = sess.getLastDoneRID();
										}
										
										catch (InterruptedException e) { }
									}
									
									dbg("handling response " + rid, 3);
									
									// Check the key
									String key = sess.getKey();
									
									if (key != null) {
										dbg("checking keys for " + rid, 3);
										
										if (attribs.getNamedItem("key") == null || !sha1(attribs.getNamedItem("key").getNodeValue()).equals(key)) {
											dbg("Key sequence error", 1);
											
											response.sendError(HttpServletResponse.SC_NOT_FOUND);
											
											sess.terminate();
											
											return;
										}
										
										if (attribs.getNamedItem("newkey") != null)
											sess.setKey(attribs.getNamedItem("newkey").getNodeValue());
										else
											sess.setKey(attribs.getNamedItem("key").getNodeValue());
										
										dbg("key valid for " + rid, 3);
									}
									
									if (attribs.getNamedItem("xmpp:restart") != null) {
										dbg("XMPP RESTART", 2);
										sess.setReinit(true);
									}
									
									// Check we have got to forward something to the XMPP server
									if (rootNode.hasChildNodes())
										sess.sendNodes(rootNode.getChildNodes());
									
									else {
										// Too many empty requests? (DoS?)
										long now = System.currentTimeMillis();
										
										if (sess.getHold() == 0 && 
												now - sess.getLastPoll() < Session.MIN_POLLING * 1000) {
											dbg("polling too frequently! [now:" + now + ", last:" + sess.getLastPoll() + "(" + (now - sess.getLastPoll()) + ")]", 1);
											
											response.sendError(HttpServletResponse.SC_FORBIDDEN);
											
											// Kick it!
											sess.terminate();
											
											return;
										}
										
										// Last empty reply
										sess.setLastPoll();
									}
									
									// Send the reply
									
									// Request to end the session?
									if (attribs.getNamedItem("type") != null) {
										String rType = attribs.getNamedItem("type").getNodeValue();
										
										if (rType.equals("terminate")) {
											sess.terminate();
											jresp.send(response);
											
											return;
										}
									}
									
									// Check incoming queue
									NodeList nl = sess.checkInQ(rid);
									
									// Add items to response
									if (nl != null)
										for (int i = 0; i < nl.getLength(); i++)
											jresp.addNode(nl.item(i), "jabber:client");
									
									if (sess.streamFeatures) {
										jresp.setAttribute("xmlns:stream", "http://etherx.jabber.org/streams");
										
										// Reset
										sess.streamFeatures = false;
									}
									
									// Check for stream id (DIGEST authentication)
									if (!sess.authidSent && sess.getAuthid() != null) {
										sess.authidSent = true;
										
										jresp.setAttribute("authid", sess.getAuthid());
									}
									
									if (sess.isStatus(Session.SESS_TERM)) {
										// Stream error: close the session
										jresp.setAttribute("type", "terminate");
										jresp.setAttribute("condition", "remote-stream-error");
									}
									
									// Send back reply
									jresp.send(response);
									sess.setLastDoneRID(jresp.getRID());
									sess.sock.notifyAll();
								}
							}
							
							catch (IOException ioe) {
								sess.terminate();
								
								jresp.setAttribute("type", "terminate");
								jresp.setAttribute("condition", "remote-connection-failed");
								jresp.send(response);
							}
						}
						
						else
							// Session not found!
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
					
					else {
						// Request to create a new session
						if (attribs.getNamedItem("rid") == null) {
							response.sendError(HttpServletResponse.SC_BAD_REQUEST);
							
							return;
						}
						
						else {
							try {
								rid = Integer.parseInt(attribs.getNamedItem("rid").getNodeValue());
							}
							
							catch (NumberFormatException e) {
								response.sendError(HttpServletResponse.SC_BAD_REQUEST);
								
								return;
							}
						}
						
						Response jresp = new Response(db.newDocument(), request);
						jresp.setRID(rid);
						
						// Check 'route' attribute
						String route = null;
						
						if (attribs.getNamedItem("route") != null && isValidRoute(attribs.getNamedItem("route").getNodeValue())) {
							route = attribs.getNamedItem("route").getNodeValue().substring("xmpp:".length());
						}
						
						// Check 'to' attribute
						String to = null;
						if ((attribs.getNamedItem("to") != null) && (attribs.getNamedItem("to").getNodeValue() != "")) {
							to = attribs.getNamedItem("to").getNodeValue();
						}
						
						// Check 'xml:lang' attribute
						String xmllang = null;
						if ((attribs.getNamedItem("xml:lang") != null) && (attribs.getNamedItem("xml:lang").getNodeValue() != "")) {
							xmllang = attribs.getNamedItem("xml:lang").getNodeValue();
						}
						
						if (to == null || to.equals("")) {
							// Error: 'to' attribute missing/empty
							if (attribs.getNamedItem("content") != null)
								jresp.setContentType(attribs.getNamedItem("content").getNodeValue());
							else
								jresp.setContentType(Session.DEFAULT_CONTENT);
							
							jresp.setAttribute("type", "terminate");
							jresp.setAttribute("condition", "improper-addressing");
							
							jresp.send(response);
							
							return;
						}
						
						// Really create new session
						try {
							Session sess = new Session(to, route, xmllang);
							
							if (attribs.getNamedItem("content") != null)
								sess.setContent(attribs.getNamedItem("content").getNodeValue());
							
							if (attribs.getNamedItem("wait") != null)
								sess.setWait(Integer.parseInt(attribs.getNamedItem("wait").getNodeValue()));
							
							if (attribs.getNamedItem("hold") != null)
								sess.setHold(Integer.parseInt(attribs.getNamedItem("hold").getNodeValue()));
							
							if (attribs.getNamedItem("xml:lang") != null)
								sess.setXMLLang(attribs.getNamedItem("xml:lang").getNodeValue());
							
							if (attribs.getNamedItem("newkey") != null)
								sess.setKey(attribs.getNamedItem("newkey").getNodeValue());
							
							if (attribs.getNamedItem("secure") != null && (attribs.getNamedItem("secure").getNodeValue().equals("true") || attribs.getNamedItem("secure").getNodeValue().equals("1")))
								sess.setSecure(true);
							
							sess.addResponse(jresp);
							
							// Send back response
							jresp.setContentType(sess.getContent());
							
							// Check incoming queue
							NodeList nl = sess.checkInQ(jresp.getRID());
							
							// Add items to response
							if (nl != null)
								for (int i = 0; i < nl.getLength(); i++) {
									if (!nl.item(i).getNodeName().equals("starttls"))
										jresp.addNode(nl.item(i), "");
								}
							
							if (sess.streamFeatures) {
								jresp.setAttribute("xmlns:stream", "http://etherx.jabber.org/streams");
								
								// Reset
								sess.streamFeatures = false;
							}
							
							jresp.setAttribute("sid", sess.getSID());
							jresp.setAttribute("wait", String.valueOf(sess.getWait()));
							jresp.setAttribute("inactivity", String.valueOf(Session.MAX_INACTIVITY));
							jresp.setAttribute("polling", String.valueOf(Session.MIN_POLLING));
							jresp.setAttribute("requests", String.valueOf(Session.MAX_REQUESTS));
							
							if (sess.getAuthid() != null) {
								sess.authidSent = true;
								jresp.setAttribute("authid", sess.getAuthid());
							}
							
							if (sess.isStatus(Session.SESS_TERM))
								jresp.setAttribute("type", "terminate");
							
							jresp.send(response);
							sess.setLastDoneRID(jresp.getRID());
						}
						
						catch (UnknownHostException uhe) {
							// Error: remote host unknown
							if (attribs.getNamedItem("content") != null)
								jresp.setContentType(attribs.getNamedItem("content").getNodeValue());
							else
								jresp.setContentType(Session.DEFAULT_CONTENT);
							
							jresp.setAttribute("type", "terminate");
							jresp.setAttribute("condition", "host-unknown");
							
							jresp.send(response);
						}
						
						catch (IOException ioe) {
							// Error: could not connect to remote host
							if (attribs.getNamedItem("content") != null)
								jresp.setContentType(attribs.getNamedItem("content").getNodeValue());
							else
								jresp.setContentType(Session.DEFAULT_CONTENT);
							
							jresp.setAttribute("type", "terminate");
							jresp.setAttribute("condition", "remote-connection-failed");
							
							jresp.send(response);
						}
						
						catch (NumberFormatException nfe) {
							response.sendError(HttpServletResponse.SC_BAD_REQUEST);
							
							return;
						}
					}
				}
			}
			
			catch (SAXException se) {
				// Error: Parser error
				dbg(se.toString(), 1);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
			
			catch (Exception e) {
				System.err.println(e.toString());
				e.printStackTrace();
				
				try {
					Response jresp = new Response(db.newDocument());
					jresp.setAttribute("type", "terminate");
					jresp.setAttribute("condition", "internal-server-error");
					jresp.send(response);
				}
				
				catch (Exception e2) {
					e2.printStackTrace();
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				}
			}
		}
	
	// Reply to GET requests
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
			// HTML content type header
			response.setContentType("text/html; charset=utf-8");
			
			// CORS headers (cross-domain)
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type");
			
			PrintWriter writer = response.getWriter();
			String title = APP_NAME;
			String version = APP_VERSION;
			
			writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n\n<head>\n	<meta http-equiv=\"Content-Type\" content=\"application/xhtml+xml; charset=UTF-8\" />\n	<meta name=\"robots\" content=\"noindex, nofollow, nocache\" />\n	<title>" + title + "</title>\n	<style type=\"text/css\">\n		* {\n			margin: 0;\n			padding: 0;\n			color: black;\n		}\n		\n		body {\n			background-color: #e2e9ea;\n			font: 1em \"Lucida Grande\", helvetica, sans-serif;\n			margin: 0.8em;\n		}\n		\n		h1 {\n			border-bottom: 2px dotted black;\n			padding-bottom: 0.3em;\n			margin-bottom: 0.6em;\n			font-size: 2.5em;\n		}\n		\n		h3 {\n			font-size: 1.1em;\n		}\n		\n		p.sessions {\n			font-size: 1em;\n			margin-top: 1em;\n		}\n		\n		p.infos {\n			font-size: 0.7em;\n			position: absolute;\n			right: 0.8em;\n			bottom:0.8em;\n		}\n	</style>\n</head>\n\n<body>\n	<h1>" + title + "</h1>\n	<h3><a href=\"http://xmpp.org/extensions/xep-0124.html\">XEP-0124</a>: Bidirectional-streams Over Synchronous HTTP (BOSH)</h3>\n	<p class=\"sessions\">Active sessions: " + Session.getNumSessions() + "</p>\n	<p class=\"infos\">Powered by <a href=\"http://vanaryon.eu/mes-creations/palladium/\">Palladium</a> " + version + ", based on <a href=\"http://blog.jwchat.org/jhb/\">JHB</a> v1.1.1</p>\n</body>\n\n</html>");
		}
	
	// Reply to OPTIONS requests
	public void doOptions(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
			// CORS headers (cross-domain)
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type");
			response.setHeader("Access-Control-Max-Age", "31536000");
		}
	
	// Checks for valid host value
	private static boolean isValidRoute(String route) {
		if (!route.startsWith("xmpp:"))
			return false;
		
		route = route.substring("xmpp:".length());
		
		// Check for port validity, if a port is given.
		int port;
		
		if ((port = route.lastIndexOf(":")) != -1) {
			try {
				int p = Integer.parseInt(route.substring(port + 1));
				if (p < 0 || p > 65535)
					return false;
			}
			
			catch (NumberFormatException nfe) {
				return false;
			}
			
			route = route.substring(0, port);
		}
		
		try {
			InetAddress.getByName(route);
		}
		
		catch (Exception e) {
			return false;
		}
		
		return true;
	}
}

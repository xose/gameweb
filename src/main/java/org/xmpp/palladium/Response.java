/**
 * PALLADIUM v1.4
 * Description: the response tool
 * Authors: Stefan Strigler, Valérian Saliou
 * License: GNU GPL
 * Last revision: 24/10/10
 */

package org.xmpp.palladium;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Response {
	private static TransformerFactory tff = TransformerFactory.newInstance();
	public static final String STATUS_LEAVING = "leaving"; 
	public static final String STATUS_PENDING = "pending"; 
	public static final String STATUS_DONE = "done";
	
	private long cDate;
	private Document doc;
	private Element body;
	private long rid;
	
	private String contentType = Session.DEFAULT_CONTENT;
	
	private String status;
	
	private HttpServletRequest req;
	
	private boolean aborted;
	
	// Create a response object
	public Response(Document doc) {		
		this.doc = doc;
		this.body = this.doc.createElement("body");
		this.doc.appendChild(this.body);
		this.body.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		this.cDate = System.currentTimeMillis();
		
		setStatus(STATUS_PENDING);
	}
	
	public Response(Document doc, HttpServletRequest req) {
		this(doc);
		this.req = req;
	}
	
	// Add the key attribute to the request main element
	public Response setAttribute(String key, String val) {
		this.body.setAttribute(key,val);
		
		return this;
	}
	
	// Add the type header
	public Response setContentType(String type) {
		this.contentType = type;
		return this;
	}
	
	// Add nodes to the <body> element
	public Response addNode(Node n, String ns) {
		try {
			if (!((Element) n).getAttribute("xmlns").equals(ns))
				((Element) n).setAttribute("xmlns",ns);
		}
		
		catch (ClassCastException e) { }
		
		this.body.appendChild(this.doc.importNode(n,true));
		
		return this;
	}
	
	// Send this code
	public synchronized void send(HttpServletResponse response) {
		StringWriter strWtr = new StringWriter();
		StreamResult strResult = new StreamResult(strWtr);
		
		try {
			Transformer tf = tff.newTransformer();
			tf.setOutputProperty("omit-xml-declaration", "yes");
			tf.transform(new DOMSource(this.doc.getDocumentElement()), strResult);
			
			// XML content type header
			response.setContentType(this.contentType);
			
			// CORS headers (cross-domain)
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type");
			
			PalladiumServlet.dbg("sending response ["+this.getRID()+"]: "+strResult.getWriter().toString(),2);
			response.getWriter().println(strResult.getWriter().toString());
			PalladiumServlet.dbg("sent response for "+this.getRID(),3);
		}
		
		catch (Exception e) {
			PalladiumServlet.dbg("XML.toString(Document): " + e,1);
		}
		
		setStatus(STATUS_DONE);
	}
	
	// Return the status
	public synchronized String getStatus() {
		return status;
	}
	
	// Status to set
	public synchronized void setStatus(String status) {
		PalladiumServlet.dbg("response status "+status+" for "+this.getRID(),3);
		this.status = status;
	}
	
	public long getRID() {
		return this.rid;
	} 
	
	public Response setRID(long rid) {
		this.rid = rid;
		return this;
	}
	
	// Return the cDate
	public synchronized long getCDate() {
		return cDate;
	}
	
	// Return the req
	public synchronized HttpServletRequest getReq() {
		return req;
	}
	
	// Param: req to set
	public synchronized void setReq(HttpServletRequest req) {
		this.req = req;
	}
	
	// Return the aborted
	public synchronized boolean isAborted() {
		return aborted;
	}
	
	// Param: aborted to set
	public synchronized void setAborted(boolean aborted) {
		this.aborted = aborted;
	}
}

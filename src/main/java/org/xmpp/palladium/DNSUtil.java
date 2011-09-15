/**
 * PALLADIUM v1.4
 * Description: the DNS resolution utilities (XMPP compliant)
 * Authors: Jive Software, Matt Tucker, Stefan Strigler, Valérian Saliou
 * License: Apache License
 * Last revision: 24/10/10
 */

package org.xmpp.palladium;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

public class DNSUtil {
	private static DirContext context;
	
	// Set the DNS environment variable
	static {
		try {
			Hashtable env = new Hashtable();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			context = new InitialDirContext(env);
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Returns the correct DNS entry (SRV or A)
	public static HostAddress resolveXMPPServerDomain(String domain, int defaultport) {
		try {
			Attributes dnsLookup = context.getAttributes("_xmpp-client._tcp." + domain);
			String srvRecord = (String)dnsLookup.get("SRV").get();
			String [] srvRecordEntries = srvRecord.split(" ");
			String host = srvRecordEntries[srvRecordEntries.length-1];
			int port = Integer.parseInt(srvRecordEntries[srvRecordEntries.length-2]);
			
			if (host.endsWith("."))
				host = host.substring(0, host.length()-1);
			
			return new HostAddress(host, port);
		}
		
		catch (Exception e) {
			return new HostAddress(domain, defaultport);
		}
	}
	
	// Encapsulates a hostname and port.
	public static class HostAddress {
		private String host;
		private int port;
		
		private HostAddress(String host, int port) {
			this.host = host;
			this.port = port;
		}
		
		public String getHost() {
			return host;
		}
		
		public int getPort() {
			return port;
		}
		
		public String toString() {
			return host + ":" + port;
		}
	}
}

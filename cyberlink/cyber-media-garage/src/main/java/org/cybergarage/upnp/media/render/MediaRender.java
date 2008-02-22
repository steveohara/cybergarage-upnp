/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003
*
*	File : MediaRender.java
*
*	02/22/08
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.media.render;

import java.io.*;

import org.cybergarage.net.*;
import org.cybergarage.util.*;
import org.cybergarage.http.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.UPnP;
import org.cybergarage.upnp.device.*;
import org.cybergarage.upnp.media.server.object.*;

public class MediaRender extends Device
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	
	public final static String DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaServer:1";
	
	public final static int DEFAULT_HTTP_PORT = 38520;
	
	public final static String DESCRIPTION = 
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n" +
		"   <specVersion>\n" +
		"      <major>1</major>\n" +
		"      <minor>0</minor>\n" +
		"   </specVersion>\n" +
		"   <device>\n" +
		"      <deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType>\n" +
		"      <friendlyName>Cyber Garage Media Render</friendlyName>\n" +
		"      <manufacturer>Cyber Garage</manufacturer>\n" +
		"      <manufacturerURL>http://www.cybergarage.org</manufacturerURL>\n" +
		"      <modelDescription>Provides content through UPnP ContentDirectory service</modelDescription>\n" +
		"      <modelName>Cyber Garage Media Render</modelName>\n" +
		"      <modelNumber>1.0</modelNumber>\n" +
		"      <modelURL>http://www.cybergarage.org</modelURL>\n" +
		"      <UDN>uuid:362d9414-31a0-48b6-b684-2b4bd38391d0</UDN>\n" +
		"      <serviceList>\n" +
		"         <service>\n" +
		"            <serviceType>urn:schemas-upnp-org:service:RenderingControl:1</serviceType>\n" +
		"            <serviceId>RenderingControl</serviceId>\n" +
		"         </service>\n" +
		"         <service>\n" +
		"            <serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>\n" +
		"            <serviceId>ConnectionManager</serviceId>\n" +
		"         </service>\n" +
		"         <service>\n" +
		"            <serviceType>urn:schemas-upnp-org:service:AVTransport:1</serviceType>\n" +
		"            <serviceId>AVTransport</serviceId>\n" +
		"         </service>\n" +
		"      </serviceList>\n" +
		"   </device>\n" +
		"</root>";
	
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	private final static String DESCRIPTION_FILE_NAME = "description/description.xml";
	
	public MediaRender(String descriptionFileName) throws InvalidDescriptionException
	{
		super(new File(descriptionFileName));
		initialize();
	}

	public MediaRender()
	{
		super();
		try {
			initialize(DESCRIPTION, ContentDirectory.SCPD, ConnectionManager.SCPD);
		}
		catch (InvalidDescriptionException ide) {}
	}

	public MediaRender(String description, String contentDirectorySCPD, String connectionManagerSCPD) throws InvalidDescriptionException
	{
		super();
		initialize(description, contentDirectorySCPD, connectionManagerSCPD);
	}
	
	private void initialize(String description, String contentDirectorySCPD, String connectionManagerSCPD) throws InvalidDescriptionException
	{
		loadDescription(description);
		
		Service servConDir = getService(ContentDirectory.SERVICE_TYPE);
		servConDir.loadSCPD(contentDirectorySCPD);
		
		Service servConMan = getService(ConnectionManager.SERVICE_TYPE);
		servConMan.loadSCPD(connectionManagerSCPD);
		
		initialize();
	}
	
	private void initialize()
	{
		// Netwroking initialization		
		UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
		String firstIf = HostInterface.getHostAddress(0);
		setInterfaceAddress(firstIf);
		setHTTPPort(DEFAULT_HTTP_PORT);
		
		conDir = new ContentDirectory(this);
		conMan = new ConnectionManager(this);
		
		Service servConDir = getService(ContentDirectory.SERVICE_TYPE);
		servConDir.setActionListener(getContentDirectory());
		servConDir.setQueryListener(getContentDirectory());

		Service servConMan = getService(ConnectionManager.SERVICE_TYPE);
		servConMan.setActionListener(getConnectionManager());
		servConMan.setQueryListener(getConnectionManager());
	}
	
	protected void finalize()
	{
		stop();		
	}
	
	////////////////////////////////////////////////
	// Memeber
	////////////////////////////////////////////////
	
	private ConnectionManager conMan;
	private ContentDirectory conDir;
	
	public ConnectionManager getConnectionManager()
	{
		return conMan;
	}

	public ContentDirectory getContentDirectory()
	{
		return conDir;
	}	
	
	////////////////////////////////////////////////
	//	ContentDirectory	
	////////////////////////////////////////////////

	public void addContentDirectory(Directory dir)
	{
		getContentDirectory().addDirectory(dir);
	}
	
	public void removeContentDirectory(String name)
	{
		getContentDirectory().removeDirectory(name);
	}

	public int getNContentDirectories()
	{
		return getContentDirectory().getNDirectories();
	}
	
	public Directory getContentDirectory(int n)
	{
		return getContentDirectory().getDirectory(n);
	}

	////////////////////////////////////////////////
	// PulgIn
	////////////////////////////////////////////////
	
	public boolean addPlugIn(Format format)
	{
		return getContentDirectory().addPlugIn(format);
	}
	
	////////////////////////////////////////////////
	// HostAddress
	////////////////////////////////////////////////

	public void setInterfaceAddress(String ifaddr)
	{
		HostInterface.setInterface(ifaddr);
	}			
	
	public String getInterfaceAddress()
	{
		return HostInterface.getInterface();
	}			

	////////////////////////////////////////////////
	// HttpRequestListner (Overridded)
	////////////////////////////////////////////////
	
	public void httpRequestRecieved(HTTPRequest httpReq)
	{
		String uri = httpReq.getURI();
		Debug.message("uri = " + uri);
	
		if (uri.startsWith(ContentDirectory.CONTENT_EXPORT_URI) == true) {
			getContentDirectory().contentExportRequestRecieved(httpReq);
			return;
		}
			 
		super.httpRequestRecieved(httpReq);
	}
	
	////////////////////////////////////////////////
	// start/stop (Overided)
	////////////////////////////////////////////////
	
	public boolean start()
	{
		getContentDirectory().start();
		super.start();
		return true;
	}
	
	public boolean stop()
	{
		getContentDirectory().stop();
		super.stop();
		return true;
	}
	
	////////////////////////////////////////////////
	// update
	////////////////////////////////////////////////

	public void update()
	{
	}			

}


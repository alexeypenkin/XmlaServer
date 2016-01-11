/*******************************************************************************
* Copyright (c) 2015-2016 Alexey Penkin.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Alexey Penkin - initial API and implementation
*******************************************************************************/

package xmlaserver.XmlaServer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlaServlet extends HttpServlet {
	static Logger log = Logger.getLogger(XmlaServlet.class.getName());
	
	private MessageFactory msgFactory = null;
    
    public void init(ServletConfig servletConfig) throws ServletException {
    	super.init(servletConfig);
	    try {
	        msgFactory = MessageFactory.newInstance();
	    } catch (SOAPException ex) {
	        throw new ServletException("Unable to create message factory" + ex.getMessage());
	    }
    }
    
    protected static MimeHeaders getHeaders(HttpServletRequest req) {
	    Enumeration enm = req.getHeaderNames();
	    MimeHeaders headers = new MimeHeaders();
	
	    while (enm.hasMoreElements()) {
	        String headerName = (String)enm.nextElement();
	        String headerValue = req.getHeader(headerName);
	
	        StringTokenizer values = new StringTokenizer(headerValue, ",");
	        while (values.hasMoreTokens())
	            headers.addHeader(headerName, values.nextToken().trim());
	    }
	    
	    return headers;
	}
    
    private void processHTTPrequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	MimeHeaders headers = getHeaders(request);
    	InputStream is = request.getInputStream();
    	SOAPMessage msg = msgFactory.createMessage(headers, is);
		SOAPPart sp = msg.getSOAPPart();
		SOAPEnvelope envelope = sp.getEnvelope();
		SOAPHeader header = envelope.getHeader();
		SOAPBody body = envelope.getBody();
		
		// -------------------------------------------------- BeginSession -----------------------------------------------------------
		
		Iterator<SOAPElement> it = header.getChildElements(envelope.createName("BeginSession", "", "urn:schemas-microsoft-com:xml-analysis"));
		if (it.hasNext()) {
			response.getOutputStream().print(TextConstants.getText("xmla/BeginSession.xml"));
			return;
		}
		
		it = header.getChildElements(envelope.createName("EndSession", "", "urn:schemas-microsoft-com:xml-analysis"));
		if (it.hasNext()) {
			response.getOutputStream().print(TextConstants.getText("xmla/BeginSession.xml"));  // TODO: should send something else here
			return;
		}
		
		String sessionId = null;
		it = header.getChildElements(envelope.createName("Session", "", "urn:schemas-microsoft-com:xml-analysis"));
		if (it.hasNext())
			sessionId = it.next().getAttribute("SessionId");
		
		// -------------------------------------------------- DISCOVER -----------------------------------------------------------
		
		SOAPElement discoverElement = null;
		it = body.getChildElements(envelope.createName("Discover", "", "urn:schemas-microsoft-com:xml-analysis"));
		if (it.hasNext())
			discoverElement = it.next();
		
		if (discoverElement != null) {
			String requestType = null;
			it = discoverElement.getChildElements(envelope.createName("RequestType", "", "urn:schemas-microsoft-com:xml-analysis"));
			if (it.hasNext())
				requestType = it.next().getTextContent();

			// Get RestrictionList
			Map<String,String> restictionsMap = new HashMap<String,String>();
			
			SOAPElement restrictionsElement = null;
			it = discoverElement.getChildElements(envelope.createName("Restrictions", "", "urn:schemas-microsoft-com:xml-analysis"));
			if (it.hasNext())
				restrictionsElement = it.next();
			
			if (restrictionsElement != null) {
				SOAPElement restrictionListElement = null;
				it = restrictionsElement.getChildElements(envelope.createName("RestrictionList", "", "urn:schemas-microsoft-com:xml-analysis"));
				if (it.hasNext())
					restrictionListElement = it.next();
				
				if (restrictionListElement != null) {
					Iterator restrictionListIt = restrictionListElement.getChildElements();
					while (restrictionListIt.hasNext()) {
						Object o = restrictionListIt.next();
						if (o instanceof SOAPElement) {
							SOAPElement restrictionElement = (SOAPElement)o;
							restictionsMap.put(restrictionElement.getLocalName(), restrictionElement.getTextContent());
						}
					}
				}
			}
			
			// Get PropertyList
			Map<String,String> propertiesMap = new HashMap<String,String>();
			
			SOAPElement propertiesElement = null;
			it = discoverElement.getChildElements(envelope.createName("Properties", "", "urn:schemas-microsoft-com:xml-analysis"));
			if (it.hasNext())
				propertiesElement = it.next();
			
			if (propertiesElement != null) {
				SOAPElement propertiesListElement = null;
				it = propertiesElement.getChildElements(envelope.createName("PropertyList", "", "urn:schemas-microsoft-com:xml-analysis"));
				if (it.hasNext())
					propertiesListElement = it.next();
				
				if (propertiesListElement != null) {
					Iterator propertiesListIt = propertiesListElement.getChildElements();
					while (propertiesListIt.hasNext()) {
						Object o = propertiesListIt.next();
						if (o instanceof SOAPElement) {
							SOAPElement propertyElement = (SOAPElement)o;
							propertiesMap.put(propertyElement.getLocalName(), propertyElement.getTextContent());
						}
					}
				}
			}
			
			XmlaHandler.handleDiscoverRequest(sessionId, requestType, restictionsMap, propertiesMap, response.getOutputStream());
		} // if Discover
		
		// -------------------------------------------------- EXECUTE -----------------------------------------------------------
		
		SOAPElement executeElement = null;
		it = body.getChildElements(envelope.createName("Execute", "", "urn:schemas-microsoft-com:xml-analysis"));
		if (it.hasNext())
			executeElement = it.next();
		
		if (executeElement != null) {
			String statement = null;
			
			SOAPElement commandElement = null;
			it = executeElement.getChildElements(envelope.createName("Command", "", "urn:schemas-microsoft-com:xml-analysis"));
			if (it.hasNext())
				commandElement = it.next();
			
			if (commandElement != null) {
				it = commandElement.getChildElements(envelope.createName("Statement", "", "urn:schemas-microsoft-com:xml-analysis"));
				if (it.hasNext())
					statement = it.next().getTextContent();
			}
			
			// Get PropertyList
			Map<String,String> propertiesMap = new HashMap<String,String>();
			
			SOAPElement propertiesElement = null;
			it = executeElement.getChildElements(envelope.createName("Properties", "", "urn:schemas-microsoft-com:xml-analysis"));
			if (it.hasNext())
				propertiesElement = it.next();
			
			if (propertiesElement != null) {
				SOAPElement propertiesListElement = null;
				it = propertiesElement.getChildElements(envelope.createName("PropertyList", "", "urn:schemas-microsoft-com:xml-analysis"));
				if (it.hasNext())
					propertiesListElement = it.next();
				
				if (propertiesListElement != null) {
					Iterator propertiesListIt = propertiesListElement.getChildElements();
					while (propertiesListIt.hasNext()) {
						Object o = propertiesListIt.next();
						if (o instanceof SOAPElement) {
							SOAPElement propertyElement = (SOAPElement)o;
							propertiesMap.put(propertyElement.getLocalName(), propertyElement.getTextContent());
						}
					}
				}
			}
			XmlaHandler.handleExecuteRequest(sessionId, statement, propertiesMap, response.getOutputStream());
		} // if Execute
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	response.setContentType("text/xml");   
    	// application/sx+xpress   - compression enabled
    	// application/sx          - encoding only
        response.setHeader("X-Transport-Caps-Negotiation-Flags", "0,0,0,0,0");  // REQ_SX, REQ_XPRESS, RESP_SX and RESP_XPRESS flags
        // [MS-BINXML]: SQL Server Binary XML Structure
        // https://msdn.microsoft.com/en-us/library/ee208875(v=sql.105).aspx
        // Direct Internet Message Encapsulation (DIME) compression
        //     http://sqlblog.com/blogs/mosha/archive/2005/12/02/analysis-services-2005-protocol-xmla-over-tcp-ip.aspx
        //     https://msdn.microsoft.com/en-us/library/aa480488.aspx
        response.setHeader("Server", "Microsoft-IIS/6.0");
        response.setHeader("X-Powered-By", "ASP.NET");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "private, no-cache");
        response.setHeader("Pragma", "no-cache");

        try {
        	processHTTPrequest(request, response);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			try {
				//response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.resetBuffer(); // If the response has been committed, this method throws an IllegalStateException.
				PrintStream ps = new PrintStream(response.getOutputStream());
				/*
				ps.println("<?xml version=\"1.0\"?>");
				ps.println("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">");
				ps.println("<SOAP-ENV:Header>");
				ps.println("</SOAP-ENV:Header>");
				ps.println("<SOAP-ENV:Body>");
				ps.println("<SOAP-ENV:Fault>");
				ps.println("  <faultcode>SOAP-ENV:Server.00HSBE02</faultcode>");
				ps.println("  <faultstring>be-be-be</faultstring>");
				ps.println("  <faultactor>XMLA Server</faultactor>");
				ps.println("  <detail>");
				ps.println("    <XA:error xmlns:XA=\"http://mondrian.sourceforge.net\">");
				ps.println("      <code>00HSBE02</code>");
				ps.println("      <desc>be-be-be</desc>");
				ps.println("    </XA:error>");
				ps.println("  </detail>");
				ps.println("</SOAP-ENV:Fault>");
				ps.println("</SOAP-ENV:Body>");
				ps.println("</SOAP-ENV:Envelope>");
				ps.flush();
				*/
				/*
				ps.println("<?xml version=\"1.0\"?>");
				ps.println("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
				ps.println("	<soap:Body>");
				ps.println("		<soap:Fault xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">");
				ps.println("			<faultcode>XMLAnalysisError.0xc10a0004</faultcode>");
				ps.println("			<faultstring>be-be-be</faultstring>");
				ps.println("			<detail>");
				ps.println("				<Error ErrorCode=\"3238658052\" Description=\"be-be-be\" Source=\"Server\" HelpFile=\"\" />");
				ps.println("			</detail>");
				ps.println("		</soap:Fault>");
				ps.println("	</soap:Body>");
				ps.println("</soap:Envelope>");
				ps.flush();
				*/
				/*
				ps.println("<?xml version=\"1.0\"?>");
				ps.println("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
				ps.println("  <soap:Body>");
				ps.println("    <ExecuteResponse xmlns=\"urn:schemas-microsoft-com:xml-analysis\">");
				ps.println("      <return>");
				ps.println("        <root xmlns=\"urn:schemas-microsoft-com:xml-analysis:empty\">");
				ps.println("          <Exception xmlns=\"urn:schemas-microsoft-com:xml-analysis:exception\" />");
				ps.println("          <Messages xmlns=\"urn:schemas-microsoft-com:xml-analysis:exception\">");
				ps.println("            <Error ErrorCode=\"3238199300\" Description=\"Memory error: While attempting to store a string, a string was found that was larger than the page size selected. The operation cannot be completed.\" Source=\"Microsoft SQL Server 2005 Analysis Services\" HelpFile=\"\" /> ");
				ps.println("          </Messages> ");
				ps.println("        </root> ");
				ps.println("      </return> ");
				ps.println("    </ExecuteResponse> ");
				ps.println("  </soap:Body> ");
				ps.println("</soap:Envelope>");
				ps.flush();
				*/
				
				ps.println(TextConstants.getText("xmla/Execute/Error4.xml"));
				
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
    }
    
    private static void saveRequest(HttpServletRequest request) throws IOException, TransformerException, ParserConfigurationException, SAXException {
    	PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("xmla.log", true)));
    	
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document doc = dBuilder.parse(new InputSource(request.getReader()));
    	
    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    	//initialize StreamResult with File object to save to file
    	StreamResult result = new StreamResult(out);
    	DOMSource source = new DOMSource(doc);
    	transformer.transform(source, result);
    	
    	  			
    	
    	
    	//Tools.copyCompletely(request.getReader(), out);
    	
        out.println();
        out.println("========================================================================================================");
        
        out.close();
    }
    
    
} 
	
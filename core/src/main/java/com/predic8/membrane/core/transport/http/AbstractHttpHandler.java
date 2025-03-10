/* Copyright 2009, 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.membrane.core.transport.http;

import com.fasterxml.jackson.core.*;
import com.predic8.membrane.core.exchange.*;
import com.predic8.membrane.core.http.*;
import com.predic8.membrane.core.http.Response.*;
import com.predic8.membrane.core.interceptor.*;
import com.predic8.membrane.core.transport.*;
import com.predic8.membrane.core.util.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;

import static com.predic8.membrane.core.http.MimeType.*;
import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.text.StringEscapeUtils.*;

public abstract class AbstractHttpHandler  {

	private static final Logger log = LoggerFactory.getLogger(AbstractHttpHandler.class.getName());

	protected Exchange exchange;
	protected Request srcReq;
	private static final InterceptorFlowController flowController = new InterceptorFlowController();

	private final Transport transport;

	public AbstractHttpHandler(Transport transport) {
		this.transport = transport;
	}

	public Transport getTransport() {
		return transport;
	}

	/**
	 * Only use for HTTP/1.0 requests. (see {@link HttpClient})
	 */
	public abstract void shutdownInput() throws IOException;
	public abstract InetAddress getLocalAddress();
	public abstract int getLocalPort();


	protected void invokeHandlers() throws IOException, EndOfStreamException, AbortException, NoMoreRequestsException, EOFWhileReadingFirstLineException {
		try {
			flowController.invokeHandlers(exchange, transport.getInterceptors());
			if (exchange.getResponse() == null)
				throw new AbortException("No response was generated by the interceptor chain.");
		} catch (Exception e) {
			if (exchange.getResponse() == null)
				exchange.setResponse(generateErrorResponse(e));

			if (e instanceof IOException)
				throw (IOException)e;
			if (e instanceof EndOfStreamException)
				throw (EndOfStreamException)e;
			if (e instanceof AbortException)
				throw (AbortException)e; // TODO: migrate catch logic into this method
			if (e instanceof NoMoreRequestsException)
				throw (NoMoreRequestsException)e;
			if (e instanceof NoResponseException)
				throw (NoResponseException)e;
			if (e instanceof EOFWhileReadingFirstLineException)
				throw (EOFWhileReadingFirstLineException)e;
			log.warn("An exception occured while handling a request: ", e);
		}
	}

	private Response generateErrorResponse(Exception e) {
		return generateErrorResponse(e, exchange, transport);
	}

	public static Response generateErrorResponse(Exception e, Exchange exchange, Transport transport) {

		boolean printStackTrace = transport.isPrintStackTrace();

		return switch (ContentTypeDetector.detect(exchange.getRequest()).getEffectiveContentType()) {
			case XML ->  createXMLErrorResponse( e, printStackTrace);
			case JSON -> createJSONErrorResponse( e, printStackTrace);
			case SOAP -> createSOAPErrorResponse( e, printStackTrace);
			case UNKNOWN -> HttpUtil.setHTMLErrorResponse(getResponseBuilder(e), getMessage(e, printStackTrace), getComment(printStackTrace));
		};
	}

	private static ResponseBuilder getResponseBuilder(Exception e) {
		ResponseBuilder b = null;
		if (e instanceof URISyntaxException)
			b = Response.badRequest();
		if (b == null)
			b = Response.internalServerError();
		return b;
	}

	private static String getMessage(Exception e, boolean printStackTrace) {
		String msg;
		if (printStackTrace) {
			msg = getStracktraceAsString(e);
		} else {
			msg = e.toString();
		}
		return msg;
	}

	private static String getStracktraceAsString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	private static String getComment(boolean printStackTrace) {
		return "Stack traces can be " + (printStackTrace ? "dis" : "en") + "abled by setting the " +
			   "@printStackTrace attribute on <a href=\"https://membrane-soa.org/service-proxy-doc/current/configuration/reference/transport.htm\">transport</a>. " +
			   "More details might be found in the log.";
	}

	private static Response createSOAPErrorResponse(Exception e, boolean printStackTrace) {
		return getResponseBuilder(e).
				header(HttpUtil.createHeaders(TEXT_XML_UTF8)).
				body(HttpUtil.getFaultSOAPBody("Internal Server Error", getMessage(e, printStackTrace) + " " + getComment(printStackTrace)).getBytes(UTF_8)).
				build();
	}

	private static Response createJSONErrorResponse( Exception e, boolean printStackTrace) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			JsonGenerator jg = new JsonFactory().createGenerator(baos);
			jg.writeStartObject();
			jg.writeFieldName("error");
			jg.writeString(getMessage(e, printStackTrace));
			jg.writeFieldName("comment");
			jg.writeString(getComment(printStackTrace));
			jg.close();
		} catch (Exception f) {
			log.error("Error generating JSON error response", f);
		}
		return getResponseBuilder(e).
				header(HttpUtil.createHeaders(APPLICATION_JSON_UTF8)).
				body(baos.toByteArray()).
				build();
	}

	private static Response createXMLErrorResponse(Exception e, boolean printStackTrace) {
		return getResponseBuilder(e).
				header(HttpUtil.createHeaders(TEXT_XML_UTF8)).
				body(("<error><message>" +
					  escapeXml11(getMessage(e, printStackTrace)) +
					  "</message><comment>" +
					  escapeXml11(getComment(printStackTrace)) +
					  "</comment></error>").getBytes(UTF_8)).
				build();
	}

	/**
	 * @return whether the {@link #getLocalPort()} of the handler has to match
	 *         the rule's local port for the rule to apply.
	 */
	public boolean isMatchLocalPort() {
		return true;
	}

	/**
	 * @return the context path of our web application, when running as a servlet with removeContextRoot="true"
	 * (which is the default, when running as a servlet). returns an empty string otherwise (e.g. if not running
	 * as a servlet or when removeContextRoot="false")
	 */
	public String getContextPath(Exchange exc) {
		return "";
	}

}

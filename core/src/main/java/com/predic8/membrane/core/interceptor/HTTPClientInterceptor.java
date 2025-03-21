/* Copyright 2011, 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */
package com.predic8.membrane.core.interceptor;

import java.net.ConnectException;
import java.net.UnknownHostException;

import com.predic8.membrane.core.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.predic8.membrane.annot.MCAttribute;
import com.predic8.membrane.annot.MCChildElement;
import com.predic8.membrane.annot.MCElement;
import com.predic8.membrane.core.Router;
import com.predic8.membrane.core.exchange.Exchange;
import com.predic8.membrane.core.http.Response;
import com.predic8.membrane.core.transport.http.HttpClient;
import com.predic8.membrane.core.transport.http.client.HttpClientConfiguration;

import static com.predic8.membrane.core.interceptor.Interceptor.Flow.Set.REQUEST;
import static com.predic8.membrane.core.interceptor.Outcome.ABORT;
import static com.predic8.membrane.core.interceptor.Outcome.RETURN;
import static com.predic8.membrane.core.util.ErrorUtil.createAndSetErrorResponse;
import static java.lang.String.format;

/**
 * @description The <i>httpClient</i> sends the request of an exchange to a Web
 *              Server using the HTTP protocol. Usually it will be globally used
 *              inside the transport. However, it is also possible to use it
 *              inside a proxy to give the proxy an individual configuration for
 *              its outgoing HTTP connection that is different from the global
 *              configuration in the transport.
 */
@MCElement(name="httpClient")
public class HTTPClientInterceptor extends AbstractInterceptor {

	private static Logger log = LoggerFactory.getLogger(HTTPClientInterceptor.class.getName());

	private boolean failOverOn5XX;
	private boolean adjustHostHeader = true;
	private HttpClientConfiguration httpClientConfig;

	private HttpClient hc;

	public HTTPClientInterceptor() {
		name="HTTPClient";
		setFlow(REQUEST);
	}

	@Override
	public Outcome handleRequest(Exchange exc) throws Exception {
		exc.blockRequestIfNeeded();

		try {
			hc.call(exc, adjustHostHeader, failOverOn5XX);
			return RETURN;
		} catch (ConnectException e) {
			String msg =  format("Target %s is not reachable.", getDestination(exc));
			log.warn(msg);
			log.warn("Maybe the target is only reachable over an HTTP proxy server. Please check proxy settings in conf/proxies.xml.");
			createAndSetErrorResponse(exc,502,msg);
			return ABORT;
		} catch (UnknownHostException e) {
			String msg = format("Target host %s is unknown. DNS was unable to resolve host name.", URLUtil.getHost(getDestination(exc)));
			log.warn(msg);
			log.warn("Maybe the target is only reachable over an HTTP proxy server. Please check proxy settings in conf/proxies.xml.");
			createAndSetErrorResponse(exc,500, msg);
			return ABORT;
		}
	}

	private String getDestination(Exchange exc) {
		return exc.getDestinations().get(0);
	}

	@Override
	public void init(Router router) throws Exception {
		super.init(router);

		hc = router.getHttpClientFactory().createClient(httpClientConfig);
		hc.setStreamPumpStats(getRouter().getStatistics().getStreamPumpStats());
	}


	public boolean isFailOverOn5XX() {
		return failOverOn5XX;
	}

	/**
	 * @description Whether to retry again (possibly the next node, when load
	 *              balancing is active) after a HTTP status code
	 *              500&lt;=<i>x</i>&lt;600 was received.
	 * @default false
	 */
	@MCAttribute
	public void setFailOverOn5XX(boolean failOverOn5XX) {
		this.failOverOn5XX = failOverOn5XX;
	}

	public boolean isAdjustHostHeader() {
		return adjustHostHeader;
	}

	/**
	 * @description Whether the HTTP "Host" header should be set before the response will be forwarded to its destination.
	 * @explanation Set this to <i>false</i>, if the incoming HTTP "Host" header should not be modified.
	 * @default true
	 */
	@MCAttribute
	public void setAdjustHostHeader(boolean adjustHostHeader) {
		this.adjustHostHeader = adjustHostHeader;
	}

	public HttpClientConfiguration getHttpClientConfig() {
		return httpClientConfig;
	}

	@MCChildElement
	public void setHttpClientConfig(HttpClientConfiguration httpClientConfig) {
		this.httpClientConfig = httpClientConfig;
	}
}

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
package com.predic8.membrane.core.interceptor.server;

import com.predic8.membrane.annot.*;
import com.predic8.membrane.core.exchange.*;
import com.predic8.membrane.core.http.*;
import com.predic8.membrane.core.interceptor.*;
import com.predic8.membrane.core.resolver.*;
import com.predic8.membrane.core.util.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import static com.predic8.membrane.core.http.MimeType.*;
import static com.predic8.membrane.core.util.HttpUtil.*;

/**
 * @description Serves static files based on the request's path.
 * @explanation <p>
 * Note that <i>docBase</i> any <i>location</i>: A relative or absolute directory, a
 * "classpath://com.predic8.membrane.core.interceptor.administration.docBase" expression or a URL.
 * </p>
 * <p>
 * The interceptor chain will not continue beyond this interceptor, as it either successfully returns a
 * HTTP response with the contents of a file, or a "404 Not Found." error.
 * </p>
 * @topic 4. Interceptors/Features
 */
@MCElement(name = "webServer")
public class WebServerInterceptor extends AbstractInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebServerInterceptor.class
            .getName());

    private static final String[] EMPTY = new String[0];

    String docBase = "docBase";
    boolean docBaseIsNormalized = false;
    String[] index = EMPTY;
    boolean generateIndex;

    public WebServerInterceptor() {
        name = "Web Server";
    }

    @Override
    public void init() throws Exception {
        super.init();
        normalizeDocBase();
    }

    private void normalizeDocBase() {
        // deferred init of docBase because router is needed
        docBase = docBase.replaceAll(Pattern.quote("\\"), "/");
        if (!docBaseIsNormalized) {
            if (!docBase.endsWith(File.separator))
                docBase += "/";
            try {
                this.docBase = getAbsolutePathWithSchemePrefix(docBase);
            } catch (Exception e) {
            }
            docBaseIsNormalized = true;
        }
    }

    @Override
    public Outcome handleRequest(Exchange exc) throws Exception {
        normalizeDocBase();

        String uri = router.getUriFactory().create(exc.getDestinations().get(0)).getPath();

        log.debug("request: " + uri);

        if (escapesPath(uri) || escapesPath(router.getUriFactory().create(uri).getPath())) {

            exc.setResponse(Response.badRequest().body("").build());
            return Outcome.ABORT;
        }

        if (uri.startsWith("/"))
            uri = uri.substring(1);


        try {
            exc.setTimeReqSent(System.currentTimeMillis());

            exc.setResponse(createResponse(router.getResolverMap(), ResolverMap.combine(router.getBaseLocation(), docBase, uri)));

            exc.setReceived();
            exc.setTimeResReceived(System.currentTimeMillis());
            return Outcome.RETURN;
        } catch (ResourceRetrievalException e) {
            for (String i : index) {
                try {
                    exc.setResponse(createResponse(router.getResolverMap(), ResolverMap.combine(router.getBaseLocation(), docBase, uri + i)));

                    exc.setReceived();
                    exc.setTimeResReceived(System.currentTimeMillis());
                    return Outcome.RETURN;
                } catch (ResourceRetrievalException e2) {
                }
            }
            String uri2 = uri + "/";
            for (String i : index) {
                try {
                    exc.setResponse(createResponse(router.getResolverMap(), ResolverMap.combine(router.getBaseLocation(), docBase, uri2 + i)));

                    exc.setReceived();
                    exc.setTimeResReceived(System.currentTimeMillis());
                    return Outcome.RETURN;
                } catch (ResourceRetrievalException e2) {
                }
            }
        }

        if (generateIndex) {
            List<String> children = router.getResolverMap().getChildren(ResolverMap.combine(router.getBaseLocation(), docBase, uri));
            if (children != null) {
                Collections.sort(children);
                StringBuilder sb = new StringBuilder();
                sb.append("<html><body><tt>");
                String base = uri;
                if (base.endsWith("/"))
                    base = "";
                else {
                    base = exc.getRequestURI();
                    int p = base.lastIndexOf('/');
                    if (p != -1)
                        base = base.substring(p + 1);
                    if (base.length() == 0)
                        base = ".";
                    base = base + "/";
                }
                for (String child : children)
                    sb.append("<a href=\"" + base + child + "\">" + child + "</a><br/>");
                sb.append("</tt></body></html>");
                exc.setResponse(Response.ok().contentType("text/html").body(sb.toString()).build());
                return Outcome.RETURN;
            }
        }

        exc.setResponse(Response.notFound().build());
        return Outcome.ABORT;
    }

    private boolean escapesPath(String uri) {
        return uri.endsWith("..") || uri.endsWith("../") || uri.endsWith("..\\") //
                || uri.contains("/../") || uri.contains("..\\") || uri.contains(":/") || uri.contains("file:\\") //
                || uri.startsWith("..");
    }

    public static Response createResponse(ResolverMap rr, String resPath) throws IOException {
        return Response.ok()
                .header(createHeaders(getContentType(resPath)))
                .body(rr.resolve(resPath), true)
                .build();
    }

    // @TODO Move to Util
    private static String getContentType(String uri) {
        if (uri.endsWith(".css"))
            return "text/css";
        if (uri.endsWith(".js"))
            return "application/javascript";
        if (uri.endsWith(".wsdl"))
            return "text/xml";
        if (uri.endsWith(".xml"))
            return "text/xml";
        if (uri.endsWith(".xsd"))
            return "text/xml";
        if (uri.endsWith(".html"))
            return "text/html";
        if (uri.endsWith(".jpg"))
            return "image/jpeg";
        if (uri.endsWith(".png"))
            return "image/png";
        if (uri.endsWith(".json"))
            return APPLICATION_JSON;
        if (uri.endsWith(".svg"))
            return "image/svg+xml";
        return null;
    }

    public String getDocBase() {
        return docBase;
    }

    /**
     * @description Sets path to the directory that contains the web content.
     * @default docBase
     * @example docBase
     */
    @Required
    @MCAttribute
    public void setDocBase(String docBase) {
        //if(!docBase.endsWith("/"))
        //	docBase = docBase + "/";
        this.docBase = docBase;
        docBaseIsNormalized = false;
    }

    private String getAbsolutePathWithSchemePrefix(String path) {
        try {
            Path p = Paths.get(path);
            if (p.isAbsolute())
                return p.toUri().toString();
        } catch (Exception ignored) {
        }


        String newPath = router.getResolverMap().combine(router.getBaseLocation(), path);
        if (newPath.endsWith(File.separator + File.separator))
            newPath = newPath.substring(0, newPath.length() - 1);
        if (!newPath.endsWith("/"))
            return newPath + "/";
        return newPath;
    }

    public String getIndex() {
        return StringUtils.join(index, ",");
    }

    @MCAttribute
    public void setIndex(String i) {
        if (i == null)
            index = EMPTY;
        else
            index = i.split(",");
    }

    public boolean isGenerateIndex() {
        return generateIndex;
    }

    @MCAttribute
    public void setGenerateIndex(boolean generateIndex) {
        this.generateIndex = generateIndex;
    }

    @Override
    public String getShortDescription() {
        return "Serves static files from<br/>" + TextUtil.linkURL(docBase) + " .";
    }

}

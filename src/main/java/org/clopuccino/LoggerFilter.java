package org.clopuccino;

import ch.qos.logback.classic.Logger;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Enumeration;

public class LoggerFilter implements Filter {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger("PRE-REQUEST");

    public void destroy() {
        // Nothing to do
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (LOGGER.isDebugEnabled()) {
            ResettableStreamHttpServletRequest wrappedRequest = new ResettableStreamHttpServletRequest((HttpServletRequest) request);

            // header

            LOGGER.debug("--- START HEADERS ---");

            Enumeration<String> headerNames = wrappedRequest.getHeaderNames();

            if (headerNames != null) {
                for (; headerNames.hasMoreElements(); ) {
                    String headerName = headerNames.nextElement();

                    LOGGER.debug(headerName + " : " + wrappedRequest.getHeader(headerName));
                }
            }

            LOGGER.debug("--- END   HEADERS ---");

            // attributes

            LOGGER.debug("--- START ATTRIBUTE ---");

            Enumeration<String> attributeNames = wrappedRequest.getAttributeNames();

            if (attributeNames != null) {
                for (; attributeNames.hasMoreElements(); ) {
                    String attributeName = attributeNames.nextElement();

                    LOGGER.debug(attributeName + " : " + wrappedRequest.getAttribute(attributeName));
                }
            }

            LOGGER.debug("--- END   ATTRIBUTE ---");

            // parameters

            LOGGER.debug("--- START PARAMETER ---");

            Enumeration<String> parameterNames = wrappedRequest.getParameterNames();

            if (parameterNames != null) {
                for (; parameterNames.hasMoreElements(); ) {
                    String parameterName = parameterNames.nextElement();

                    LOGGER.debug(parameterName + " : " + wrappedRequest.getParameter(parameterName));
                }
            }

            LOGGER.debug("--- END   PARAMETER ---");

            // body

            LOGGER.debug("--- START BODY ---");

            InputStream inputStream = wrappedRequest.getInputStream();

            if (inputStream != null) {
                try {
                    String body = IOUtils.toString(inputStream, "UTF-8");

                    if (body != null && body.trim().length() > 0) {
                        LOGGER.debug(body);
                    }
                } catch (Exception e) {
                    LOGGER.debug("Failed to get body.\n" + e.getMessage());
                }
            }

            LOGGER.debug("--- END   BODY ---");

            wrappedRequest.resetInputStream();

            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }

    }

    public void init(FilterConfig arg0) throws ServletException {
        // Nothing to do
    }

    private static class ResettableStreamHttpServletRequest extends HttpServletRequestWrapper {

        private byte[] rawData;

        private HttpServletRequest request;

        private ResettableServletInputStream servletStream;

        public ResettableStreamHttpServletRequest(HttpServletRequest request) {
            super(request);
            this.request = request;
            this.servletStream = new ResettableServletInputStream();
        }


        public void resetInputStream() {
            servletStream.stream = new ByteArrayInputStream(rawData);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return servletStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return new BufferedReader(new InputStreamReader(servletStream));
        }


        private class ResettableServletInputStream extends ServletInputStream {

            private InputStream stream;

            @Override
            public int read() throws IOException {
                return stream.read();
            }

            @Override
            public boolean isFinished() {
                throw new RuntimeException("Not yet implemented");
            }

            @Override
            public boolean isReady() {
                throw new RuntimeException("Not yet implemented");
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new RuntimeException("Not yet implemented");
            }
        }
    }
}
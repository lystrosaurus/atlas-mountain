package io.github.lystrosaurus.atlasmountain.web.log;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class RequestLogFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest httpRequest)
        || !(response instanceof HttpServletResponse httpResponse)) {
      chain.doFilter(request, response);
      return;
    }

    ContentCachingRequestWrapper wrappedRequest =
        new ContentCachingRequestWrapper(httpRequest, 1024);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

    long start = System.currentTimeMillis();
    try {
      chain.doFilter(wrappedRequest, wrappedResponse);
    } finally {
      long duration = System.currentTimeMillis() - start;
      log.info(
          "{} {} {} - {}ms",
          wrappedRequest.getMethod(),
          wrappedRequest.getRequestURI(),
          wrappedResponse.getStatus(),
          duration);
      wrappedResponse.copyBodyToResponse();
    }
  }
}

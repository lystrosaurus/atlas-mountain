package io.github.lystrosaurus.atlasmountain.web.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

public class RequestLogFilterTest {

  @Test
  public void passesOriginalRequestAndResponseToChain() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);

    new RequestLogFilter().doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }
}

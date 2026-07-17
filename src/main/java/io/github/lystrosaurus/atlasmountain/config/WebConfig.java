package io.github.lystrosaurus.atlasmountain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.lystrosaurus.atlasmountain.web.UserContextInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final UserContextInterceptor userContextInterceptor;

  public WebConfig(UserContextInterceptor userContextInterceptor) {
    this.userContextInterceptor = userContextInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // Run after the Sa-Token interceptor (default order=0) so the login id is already resolved.
    registry.addInterceptor(userContextInterceptor).addPathPatterns("/**").order(10);
  }
}

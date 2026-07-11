package io.github.lystrosaurus.atlasmountain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import io.github.lystrosaurus.atlasmountain.auth.service.ApiTokenService;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

  private final ApiTokenService apiTokenService;

  public SaTokenConfig(ApiTokenService apiTokenService) {
    this.apiTokenService = apiTokenService;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(
            new SaInterceptor(
                handle -> {
                  SaRouter.match(
                      "/api/open/**",
                      () -> apiTokenService.verify(SaHolder.getRequest().getHeader("X-API-Token")));
                  SaRouter.match("/**")
                      .notMatch("/api/public/**")
                      .notMatch("/api/auth/login")
                      .notMatch("/api/open/**")
                      .notMatch("/actuator/health")
                      .notMatch("/actuator/health/**")
                      .notMatch("/error")
                      .check(StpUtil::checkLogin);
                }))
        .addPathPatterns("/**");
  }
}

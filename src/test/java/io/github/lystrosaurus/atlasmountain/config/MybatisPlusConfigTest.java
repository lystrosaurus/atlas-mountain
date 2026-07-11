package io.github.lystrosaurus.atlasmountain.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

public class MybatisPlusConfigTest {

  @Test
  public void registersPaginationInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusConfig().mybatisPlusInterceptor();

    assertThat(interceptor.getInterceptors())
        .singleElement()
        .isInstanceOf(PaginationInnerInterceptor.class);
  }
}

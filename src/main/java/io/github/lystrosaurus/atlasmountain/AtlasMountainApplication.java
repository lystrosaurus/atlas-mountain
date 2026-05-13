package io.github.lystrosaurus.atlasmountain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AtlasMountainApplication {

  public static void main(String[] args) {
    SpringApplication.run(AtlasMountainApplication.class, args);
  }
}

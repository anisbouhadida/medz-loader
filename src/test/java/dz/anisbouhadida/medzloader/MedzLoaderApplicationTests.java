package dz.anisbouhadida.medzloader;

import dz.anisbouhadida.medzloader.testsupport.PostgresTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestContainerConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class MedzLoaderApplicationTests {

  @Test
  void contextLoads() {}
}

package dz.anisbouhadida.medzloader.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/// Test configuration that provides an isolated PostgreSQL database through Testcontainers.
///
/// The container uses the same PostgreSQL major image as local Docker Compose and CI,
/// and initializes the medicine business schema from the application classpath.
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestContainerConfiguration {

  /// Creates the PostgreSQL container used by Spring integration tests.
  ///
  /// @return a PostgreSQL Testcontainers instance with the medicine schema initialized
  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("medz_test")
        .withUsername("medz")
        .withPassword("medz")
        .withInitScript("sql/ddl.sql");
  }
}

package dz.anisbouhadida.medzloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/// Entry point for the **MedzLoader** Spring Boot application.
///
/// Bootstraps the Spring context and launches the Spring Batch job
/// that loads medicine data from CSV files into the target database.
///
/// @author Anis Bouhadida
/// @since 0.0.1
@SpringBootApplication
public class MedzLoaderApplication {

    /// Starts the Spring Boot application.
    void main() {
        SpringApplication.run(MedzLoaderApplication.class);
    }
}

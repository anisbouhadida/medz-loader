package dz.anisbouhadida.medzloader.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/// Typed configuration properties for the `medz.loader` namespace.
///
/// Bound automatically by Spring Boot from any property source using the
/// `medz.loader` prefix. Validated at startup so misconfigured deployments
/// fail fast.
///
/// The user only supplies the input directory path; the `file:` prefix and
/// the `/**/*.csv` glob pattern are added automatically by the application.
///
/// A default value is provided in `application.properties` and can be
/// overridden at runtime via an environment variable thanks to Spring Boot's
/// relaxed binding:
///
/// | Property                          | Environment variable              |
/// |-----------------------------------|-----------------------------------|
/// | `medz.loader.input-dir`           | `MEDZ_LOADER_INPUT_DIR`           |
/// | `medz.loader.registration-zone-id`| `MEDZ_LOADER_REGISTRATION_ZONE_ID`|
///
/// ```shell
/// export MEDZ_LOADER_INPUT_DIR=/data/medz/input
/// export MEDZ_LOADER_REGISTRATION_ZONE_ID=Africa/Algiers
/// ```
///
/// @param inputDir            path to the directory containing the input CSV files
///                            (e.g. `/data/medz/input`).
/// @param registrationZoneId  time zone used when converting [java.time.LocalDateTime]
///                            registration dates to UTC timestamps for storage
///                            (e.g. `Africa/Algiers`).
/// @author Anis Bouhadida
/// @since 0.1.0
/// @version 0.2.0
@Validated
@ConfigurationProperties(prefix = "medz.loader")
public record MedzLoaderProperties(
    @NotBlank(message = "medz.loader.input-dir must not be blank") String inputDir,
    @NotNull(message = "medz.loader.registration-zone-id must not be null")
        ZoneId registrationZoneId) {}

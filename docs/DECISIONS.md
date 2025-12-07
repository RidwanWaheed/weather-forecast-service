# Architecture Decisions

Key technical decisions made in this project and their rationale.

## Java Records for DTOs

**Decision:** Use Java Records instead of classes for DTOs (WeatherResponse, ForecastResponse).

**Why:**
- Immutable by default - prevents accidental modification
- Less boilerplate - no need for getters, equals, hashCode, toString
- Clear intent - signals this is a data carrier, not a service

## Lombok @Getter/@Setter over @Data for Entities

**Decision:** Use `@Getter`, `@Setter`, and `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` instead of `@Data` on JPA entities.

**Why:**
- `@Data` generates `equals()`/`hashCode()` using all fields including `id`
- This breaks Hibernate collections when entities are not yet persisted (`id = null`)
- Using business keys (e.g., `name` for City) or only `id` after persistence is safer

## Caffeine Cache over Redis

**Decision:** Use Caffeine (in-memory) cache instead of Redis.

**Why:**
- Simpler deployment - no additional infrastructure
- Sufficient for single-instance deployment
- Lower latency for weather data (frequently accessed, short TTL)
- Trade-off: No cache sharing across instances

## BigDecimal for Weather Values

**Decision:** Use `BigDecimal` instead of `Double` for temperature, wind speed, etc.

**Why:**
- Avoids floating-point precision issues
- Database column precision mapping is explicit
- Better for display formatting

## Instant over LocalDateTime

**Decision:** Use `java.time.Instant` for all timestamps.

**Why:**
- Timezone-agnostic storage (always UTC)
- Simpler conversion when displaying to users
- Consistent with external API timestamps

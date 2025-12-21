# ShareIt

[Русская версия](README.ru.md)

Spring Boot service for item sharing between users. Provides REST endpoints for managing users and items with in-memory storage.

## Features
- CRUD for users
- CRUD for items with ownership checks
- Text search across available items
- Centralized error handling with clear HTTP responses

## Tech Stack
- Java 17+
- Spring Boot
- Maven
- Lombok

## Getting Started
1. Build: `./mvnw clean package`
2. Run: `./mvnw spring-boot:run`
3. Default port: 8080

## Testing
- Run tests: `./mvnw test`

## Project Structure
- `src/main/java` — application code
- `src/test/java` — tests
- `src/main/resources` — configuration

---
For Russian instructions, see [README.ru.md](README.ru.md).

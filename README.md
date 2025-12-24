# ShareIt

[Русская версия](README.ru.md)

**ShareIt** is a RESTful web service for sharing items between users. Users can add items for sharing, search for available items, book them for specific periods, and leave comments after use.

## 🚀 Features

### Users
- Create, update, delete users
- Email uniqueness validation

### Items
- CRUD operations for items with ownership checks
- Full-text search across available items (name + description)
- Comments from users who completed bookings

### Bookings
- Create booking requests for available items
- Owner approval/rejection workflow
- Filter bookings by state: `ALL`, `CURRENT`, `PAST`, `FUTURE`, `WAITING`, `REJECTED`
- View bookings as booker or as item owner
- Last/next booking info for item owners

## 🛠 Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Programming language |
| Spring Boot | 3.5.x | Application framework |
| Spring Data JPA | 3.x | Database access |
| PostgreSQL | 16 | Production database |
| H2 | 2.x | Testing database |
| Hibernate | 6.x | ORM |
| Lombok | 1.18.x | Boilerplate reduction |
| Maven | 3.9.x | Build tool |
| Docker | 20+ | Containerization |
| Swagger/OpenAPI | 3.1 | API documentation |

## 📋 Prerequisites

- JDK 17 or higher
- Docker and Docker Compose (for PostgreSQL)
- Maven 3.9+ (or use included wrapper `mvnw`)

## 🏃 Quick Start

### 1. Start PostgreSQL Database

```bash
docker run -d \
  --name shareit-postgres \
  -e POSTGRES_DB=shareit \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Build the Application

```bash
./mvnw clean package -DskipTests
```

### 3. Run the Application

```bash
java -jar target/shareit-0.0.1-SNAPSHOT.jar
```

Or using Maven:
```bash
./mvnw spring-boot:run
```

### 4. Access the Application

| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

## 📡 API Endpoints

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users` | Get all users |
| GET | `/users/{id}` | Get user by ID |
| POST | `/users` | Create user |
| PATCH | `/users/{id}` | Update user |
| DELETE | `/users/{id}` | Delete user |

### Items
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/items` | Get owner's items |
| GET | `/items/{id}` | Get item by ID |
| GET | `/items/search?text=` | Search available items |
| POST | `/items` | Create item |
| PATCH | `/items/{id}` | Update item |
| POST | `/items/{id}/comment` | Add comment |

### Bookings
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/bookings` | Get user's bookings |
| GET | `/bookings/{id}` | Get booking by ID |
| GET | `/bookings/owner` | Get bookings for owner's items |
| POST | `/bookings` | Create booking request |
| PATCH | `/bookings/{id}?approved=` | Approve/reject booking |

> **Note:** All `/items` and `/bookings` endpoints require `X-Sharer-User-Id` header.

## 🧪 Testing

### Run Unit Tests
```bash
./mvnw test
```

### Run API Tests (PowerShell)
```powershell
# Ensure server is running first
powershell -ExecutionPolicy Bypass -File test-api-en.ps1
```

### Test Coverage
The project includes 81 tests covering:
- User CRUD operations
- Item CRUD operations with ownership validation
- Booking lifecycle (create → approve/reject)
- Booking state filtering
- Comment creation with booking validation
- Error handling scenarios

## 📁 Project Structure

```
shareit/
├── src/
│   ├── main/
│   │   ├── java/ru/practicum/shareit/
│   │   │   ├── booking/          # Booking feature
│   │   │   ├── config/           # OpenAPI configuration
│   │   │   ├── exception/        # Global error handling
│   │   │   ├── item/             # Item & Comment feature
│   │   │   ├── request/          # Item request feature
│   │   │   └── user/             # User feature
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-test.properties
│   │       └── schema.sql
│   └── test/
│       └── java/ru/practicum/shareit/
├── test-api-en.ps1               # API test script
├── pom.xml
├── mvnw / mvnw.cmd
└── README.md
```

## ⚙️ Configuration

### application.properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/shareit
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

# Server
server.port=8080
```

### Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_NAME` | shareit | Database name |
| `DB_USER` | postgres | Database user |
| `DB_PASS` | postgres | Database password |

## 🐳 Docker Commands

```bash
# Start PostgreSQL
docker start shareit-postgres

# Stop PostgreSQL
docker stop shareit-postgres

# View logs
docker logs shareit-postgres

# Connect to database
docker exec -it shareit-postgres psql -U postgres -d shareit

# Clear all data
docker exec shareit-postgres psql -U postgres -d shareit \
  -c "TRUNCATE TABLE comments, bookings, items, users RESTART IDENTITY CASCADE;"
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

For Russian documentation, see [README.ru.md](README.ru.md).

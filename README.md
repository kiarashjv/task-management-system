# Task Management System

A microservices-based task management system built with Spring Boot and Next.js.

## Project Overview

This project is a task management system that utilizes a microservices architecture. It's designed to demonstrate the implementation of various Spring Boot features and integration with a Next.js frontend.

## Services

- User Service: Handles user authentication and management

## Technologies Used

- Backend:
  - Spring Boot 3.3.3
  - Spring Security
  - Spring Data JPA
  - OAuth2 Resource Server
  - Eureka Discovery Client
  - Spring Boot Actuator
- Frontend:
  - Next.js (version TBD)
- Database:
  - PostgreSQL

## Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6 or later
- PostgreSQL 12 or later

### Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/task-management-system.git
   ```

2. Navigate to the user-service directory:

   ```bash
   cd task-management-system/user-service
   ```

3. Build the project:

   ```bash
   mvn clean install
   ```

4. Run the user service:
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

(To be added - consider using Swagger or Spring REST Docs)

## Testing

To run the tests, use the following command:

```
mvn test
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

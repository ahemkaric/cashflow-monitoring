# Cashflow Monitoring Application

Welcome to the **Cashflow Monitoring Application**, a system designed to help users track, manage, and analyze financial
transactions efficiently.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Todo](#todo)

## Features

- **Company Monitoring**: Retrieve detailed information about companies and their associated transactions.
- **Transaction Management**: Update and manage company-related transactions.
- **Transaction Types**: Supports both SEPA and SWIFT transactions, with extensibility for future transaction types.
- **Real-time Exchange Rates**: Fetch and apply real-time exchange rates to transactions automatically.
- **Company Info Management**: Store and manage company information for associated transactions.
- **Scalable and Reactive**: Uses reactive programming (WebFlux) to handle large-scale transaction data efficiently.

## Technologies Used

- **Backend**: Java, Spring Boot
- **Database**: MongoDB (Reactive)
- **Reactive Programming**: Webflux (Spring)
- **API Client**: WebClient (for external API calls)
- **Caching**: Redis (Reactive)
- **Testing**: JUnit, Embedded MongoDB (for integration tests)
- **Build Tool**: Maven

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven** (for building the project)
- **MongoDB** (ensure MongoDB is running locally or configure it for a remote instance)
- **External DB API**: The application relies on an external database API to fetch transaction data. You need access to
  this API.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/ahemkaric/cashflow-monitoring
   cd cashflow-monitoring

2. Build the project using Maven:
   ```bash
   mvn clean install

3. Run the application:
   ```bash
   mvn spring-boot:run

### Configuration

Edit the application properties file in `src/main/resources/application.properties` to configure the necessary
environment variables such as MongoDB URI, API keys, and any other application-specific settings.

Example configuration:

```properties
# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=company_info
# External DB API Configuration
external.db.api.base-url=http://localhost:8080
# Redis Caching
redis.host=localhost
```

## Usage

- After running the application, you can access the API to manage transactions and company information.
- The application exposes a **Swagger UI** for interactive API documentation, which can be accessed at:
  `http://localhost:8081/swagger-ui.html`

## API Documentation

Refer to the API documentation for detailed information on available endpoints, request formats, and response
structures.

## TODO

Here’s a list of upcoming features and improvements:

- [ ] **Add Unit Tests**: Increase code coverage by adding comprehensive unit tests.
- [ ] **Improve Error Handling**: Add more granular exception handling and custom error responses.
- [ ] **Dockerize Application**: Create a Dockerfile and add instructions for containerization.
- [ ] **Improve Logging**: Use structured logging for better traceability.

**Thank you for checking out the Cashflow Monitoring Application! Feel free to reach out for any questions or
contributions.**
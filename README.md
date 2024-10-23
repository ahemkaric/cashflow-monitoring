# Cashflow Monitoring Application

Welcome to the **Cashflow Monitoring Application**, an API designed to efficiently monitor cash flows and 
financial transactions for different companies in a user-friendly way


## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Roadmap](#roadmap)

## Features

- **Cashflow Monitoring**: Access comprehensive details about companies and their financial transactions.
- **Financial Health Overview**: Delivers an intuitive and comprehensive summary of a company's financial status, including:
  - The company's current cash balance.
  - A detailed history of transactions filtered by a user-defined time range.
  - A breakdown of the countries where the company has the highest and lowest levels of operation.
- **Real-time Exchange Rates**: Real-time exchange-rate integration from an external API.
- **Company Info Management**: Store and manage company information for associated transactions.
- **Scalable Architecture**: Designed to efficiently process and manage large-scale transaction data.

## Technologies Used    

- **Backend**: Java, Spring Boot
- **Database**: MongoDB
- **Reactive Programming**: Webflux
- **Caching**: Redis
- **Testing**: JUnit, Mockito, MockWebServer, Embedded MongoDB
- **API Documentation**: OpenAPI
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
  <http://localhost:8081/swagger-ui.html>

## API Documentation

Refer to the API documentation for detailed information on available endpoints, request formats, and response
structures.

## Roadmap

Here’s a list of upcoming features and improvements:

1. **Unit Testing**: Implement comprehensive unit tests.
2. **Error Handling Improvements**: Enhance exception handling and custom error responses.
3. **Performance Optimization:**: Analyze and optimize application performance for large data sets.
4. **Features Improvement**: Refine and enhance existing features for better usability and performance.
5. **Containerize Application**: Create a containerization setup with instructions.
6. **Implement CI/CD Pipeline**: Set up continuous integration and deployment for automated testing and deployment.
7. **Provide Usage Examples**: Add clear and practical examples to demonstrate how to use the application.

**Thank you for checking out the Cashflow Monitoring Application! Feel free to reach out for any questions or
contributions.**
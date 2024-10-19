# Cashflow Monitoring Application

Welcome to the Cashflow Monitoring API! This **application helps users track, manage, and analyze their financial transactions** in a user-friendly interface.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [API Documentation](#api-documentation)

## Features

- **Company Monitoring**: Easily get details about companies and their transactions
- **Transaction Management**: Easily update with new companies and transactions
- **Transaction Types**: Support for SEPA and SWIFT transactions and scalable to support new transactions.
- **Real-time Exchange Rates**: Automatically fetch and apply the latest exchange rates.
- **Company Info Management**: Store and manage information about companies associated with transactions.

## Technologies Used

- **Backend**: Java, Spring Boot
- **Database**: MongoDB
- **Reactive Programming**: Webflux
- **API Client**: WebClient
- **Caching**: Redis
- **Testing**: JUnit, embedded MongoDB

## Getting Started

### Prerequisites

- External DB API up and running
- MongoDB

### Configuration

Configure application properties in `src/main/resources/application.properties`.

## Usage
Access the API: Once the application is running, you can access the API endpoints to manage transactions and company info.
Documentation: API documentation is available via Swagger UI at `/swagger-ui.html` endpoint.

## API Documentation
Refer to the API documentation for detailed information on the available endpoints, request parameters, and response formats.


**Thank you for checking out the Cashflow Monitoring Application! We hope you find it useful for managing your financial transactions effectively.**
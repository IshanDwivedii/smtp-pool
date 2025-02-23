# SMTP Pool Library

This project is a simple Spring Boot application that provides an SMTP connection pool for sending emails efficiently.

## Features

- Configurable SMTP settings.
- REST API for sending emails.
- Uses Spring Boot for easy setup and dependency management.

## Getting Started

### Prerequisites

- Java 11 or higher.
- Maven or Gradle for building the project.

### Installation

1. Clone the repository:
git clone <repository-url>
cd smtp-pool-library

text

2. Update the `application.properties` file with your SMTP credentials.

3. Build the project:
- For Gradle:
  ```
  ./gradlew build
  ```
- For Maven:
  ```
  mvn clean install
  ```

4. Run the application

### Usage

Send a POST request to `/api/email/send` with a JSON body containing:

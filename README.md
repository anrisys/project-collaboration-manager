# Project Collab Manager

Project Collab Manager is a Command Line Interface (CLI) application designed to manage project collaboration for small teams or individual contributors. The app handles user authentication and project management logic in a cleanly structured layered architecture.

## ✨ Features

- User registration with secure password hashing (BCrypt)
- User login with credential verification
- Input validation at the CLI (view) layer
- Repository and service layer separation
- Integration testing with real database interaction
- Simple JDBC-based persistence

## 🚧 Current Development

- CLI-based application (project management)
- Integration testing for project management use cases

## 🔮 Future Development

- Add support for project/task management
- Add project collaboration for multiple user feature
- Shift to a REST API using Spring Boot
- Implement unit testing and API tests
- Build a GUI or web-based front-end (Vue)
- Add role-based authorization

## 🛠️ How to Install Dependencies

This app uses:

- JDK 17 or above
- Maven (for dependency management)

To install dependencies:

```bash
mvn clean install
```
## ▶️ How to Run the Application
In IntelliJ:
1. Open the Main.java file in your CLI runner class.
2. Right-click → Run Main.main()

Or using terminal:
```bash
mvn exec:java -Dexec.mainClass="com.anrisys.projectcollabmanager.CLIApp"
```
## 🧪 How to Run Integration Tests
You can run integration tests using Maven:
```bash
mvn test
```
Or from IntelliJ:
1. Right-click on BasicAuthServiceIT.java
2. Click Run 'BasicAuthServiceIT'

## 🗂️ Folder Structure / Architecture

```bash
project-collab-manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/anrisys/projectcollabmanager/
│   │   │       ├── application/      # Application configuration
│   │   │       ├── entity/           # Entity classes (User, etc.)
│   │   │       ├── exception/        # Custom exceptions
│   │   │       ├── repository/       # Database interface layer
│   │   │       ├── service/          # Business logic layer
│   │   │       ├── util/             # Utility classes (e.g., PasswordUtil)
│   │   │       ├── view/             # CLI user interface
│   │   │       └── CLIApp.java       # App entry point
│   └── test/
│       └── java/
│           └── com/anrisys/projectcollabmanager/
│               └── service/
│                   └── auth/
│                       └── BaseBasicAuthServiceIT  # Base class for Auth service integration test
└── pom.xml                           # Maven config
└── README.md                         # Readme file
```


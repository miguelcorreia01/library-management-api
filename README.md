# Library Management System REST API

A comprehensive REST API for managing a library system with JWT-based authentication, role-based access control (Admin and User), book management, borrowing system, and statistics.

## Tech Stack

- **Java**: 17+
- **Spring Boot**: 3.2.0
- **Spring Security**: JWT Authentication
- **Spring Data JPA**: Data persistence
- **PostgreSQL**: Database
- **Lombok**: Code generation
- **Gradle**: Build tool
- **Swagger/OpenAPI**: API documentation
- **JUnit 5 & Mockito**: Testing

## Features

### Admin Features
- Create, update, and delete books, authors, and categories
- Manage users (activate/deactivate, promote/demote, delete)
- View comprehensive statistics (popular books, authors, categories)
- View all borrow records and user borrowing history

### User Features
- Search books by title, author, category, and release year
- Borrow books (maximum 5 books at a time)
- Return borrowed books
- View personal borrowing history and active borrows
- Manage personal profile and change password

## Database Schema

The system uses the following database tables:

### `users`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR | NOT NULL |
| email | VARCHAR | UNIQUE, NOT NULL |
| password | VARCHAR | NOT NULL |
| role | VARCHAR | NOT NULL (ADMIN/USER) |
| is_active | BOOLEAN | DEFAULT true |

### `authors`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR | UNIQUE, NOT NULL |

### `categories`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR | UNIQUE, NOT NULL |

### `books`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| title | VARCHAR | UNIQUE, NOT NULL |
| release_year | INTEGER | NOT NULL |
| is_borrowed | BOOLEAN | DEFAULT false |
| author_id | BIGINT | FOREIGN KEY → authors(id) |
| category_id | BIGINT | FOREIGN KEY → categories(id) |

### `borrow_records`
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| borrow_date | DATE | NOT NULL |
| return_date | DATE | NULLABLE |
| is_returned | BOOLEAN | DEFAULT false |
| book_id | BIGINT | FOREIGN KEY → books(id) |
| user_id | BIGINT | FOREIGN KEY → users(id) |

## Authentication

The API uses JWT (JSON Web Token) for authentication. After successful login or registration, you'll receive a JWT token that must be included in the `Authorization` header for protected endpoints.

**Header Format:**
```
Authorization: Bearer <your-jwt-token>
```

**Default Admin Credentials:**
- Email: `admin@library.com`
- Password: `admin123`



## API Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and get JWT token | No |

### Books (`/api/books`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/books` | Get all books | Yes | Any |
| GET | `/api/books/{bookId}` | Get book by ID | Yes | Any |
| GET | `/api/books/title/{title}` | Get book by title | Yes | Any |
| GET | `/api/books/author/{authorId}` | Get books by author | Yes | Any |
| GET | `/api/books/category/{categoryId}` | Get books by category | Yes | Any |
| GET | `/api/books/release-year/{releaseYear}` | Get books by release year | Yes | Any |
| GET | `/api/books/search` | Search books (query params) | Yes | Any |
| GET | `/api/books/borrowed` | Get all borrowed books | Yes | Any |
| GET | `/api/books/available` | Get all available books | Yes | Any |
| POST | `/api/books` | Create a new book | Yes | ADMIN |
| PUT | `/api/books/{bookId}` | Update a book | Yes | ADMIN |
| DELETE | `/api/books/{bookId}` | Delete a book | Yes | ADMIN |

**Search Query Parameters:**
- `title` (optional): Filter by title
- `authorName` (optional): Filter by author name
- `categoryName` (optional): Filter by category name
- `releaseYear` (optional): Filter by release year

### Authors (`/api/authors`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/authors` | Get all authors | Yes | Any |
| GET | `/api/authors/{authorId}` | Get author by ID | Yes | Any |
| POST | `/api/authors` | Create a new author | Yes | ADMIN |

### Categories (`/api/categories`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/categories` | Get all categories | Yes | Any |
| GET | `/api/categories/{categoryId}` | Get category by ID | Yes | Any |
| POST | `/api/categories` | Create a new category | Yes | ADMIN |

### Borrowing (`/api/borrows`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| POST | `/api/borrows` | Borrow a book | Yes | USER |
| PUT | `/api/borrows/{borrowRecordId}/return` | Return a borrowed book | Yes | USER |
| GET | `/api/borrows/active` | Get my active borrows | Yes | USER |
| GET | `/api/borrows/history` | Get my borrow history | Yes | USER |
| GET | `/api/borrows/{borrowRecordId}` | Get borrow record by ID | Yes | ADMIN |
| GET | `/api/borrows/all/active` | Get all active borrows | Yes | ADMIN |
| GET | `/api/borrows/all/returned` | Get all returned borrows | Yes | ADMIN |
| GET | `/api/borrows/book/{bookId}` | Get borrows by book | Yes | ADMIN |
| GET | `/api/borrows/user/{userId}` | Get borrows by user | Yes | ADMIN |

### User Profile (`/api/user/profile`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/user/profile` | Get my profile | Yes | USER |
| PUT | `/api/user/profile` | Update my profile | Yes | USER |
| PUT | `/api/user/profile/change-password` | Change my password | Yes | USER |

### Admin - User Management (`/api/admin/users`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/admin/users` | Get all users | Yes | ADMIN |
| GET | `/api/admin/users/{userId}` | Get user by ID | Yes | ADMIN |
| PUT | `/api/admin/users/{userId}` | Update user | Yes | ADMIN |
| PUT | `/api/admin/users/{userId}/promote` | Promote user to admin | Yes | ADMIN |
| PUT | `/api/admin/users/{userId}/demote` | Demote admin to user | Yes | ADMIN |
| PUT | `/api/admin/users/{userId}/activate` | Activate user account | Yes | ADMIN |
| PUT | `/api/admin/users/{userId}/deactivate` | Deactivate user account | Yes | ADMIN |
| DELETE | `/api/admin/users/{userId}` | Delete user account | Yes | ADMIN |

### Admin - Statistics (`/api/admin/statistics`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/admin/statistics` | Get library statistics | Yes | ADMIN |

**Statistics Response Includes:**
- Total books, users, borrowed books, available books
- Total borrow records and active borrows
- Popular categories (sorted by borrow count)
- Popular authors (sorted by borrow count)
- Most borrowed books (sorted by borrow count)

## Setup Instructions

### Prerequisites
- Java 17 or higher
- PostgreSQL 12 or higher
- Gradle 7.x or higher

### Database Setup

1. **Create a PostgreSQL database:**
```sql
CREATE DATABASE library_db;
```

### Configure application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your_jwt_secret
jwt.expiration=86400000

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.port=8081
```

### Running the Application

1. **Clone the repository:**
```bash
git clone https://github.com/miguelcorreia01/library-management-api.git
cd library-management-api
```

2. **Build the project:**
```bash
./gradlew build
```

3. **Run the application:**
```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8081`

### Data Initialization

The application automatically creates a default admin user and sample data on startup


**Default Admin Credentials:**
- Email: `admin@library.com`
- Password: `admin123`

## API Usage Examples

### 1. Register a New User

**Request:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "USER"
}
```

---

### 2. Login

**Request:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@library.com",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "name": "Admin User",
  "email": "admin@library.com",
  "role": "ADMIN"
}
```

---

### 3. Create a Book (Admin Only)

**Request:**
```bash
curl -X POST http://localhost:8081/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "title": "The Great Gatsby",
    "authorId": 1,
    "categoryId": 1,
    "releaseYear": 1925
  }'
```

**Response:**
```json
{
  "id": 1,
  "title": "The Great Gatsby",
  "authorName": "F. Scott Fitzgerald",
  "categoryName": "Classic Literature",
  "releaseYear": 1925,
  "borrowed": false
}
```

---

### 4. Search Books

**Request:**
```bash
curl -X GET "http://localhost:8081/api/books/search?title=Harry&categoryName=Fantasy" \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Harry Potter and the Philosopher's Stone",
    "authorName": "J.K. Rowling",
    "categoryName": "Fantasy",
    "releaseYear": 1997,
    "borrowed": false
  }
]
```

---

### 5. Borrow a Book (User Only)

**Request:**
```bash
curl -X POST http://localhost:8081/api/borrows \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "bookId": 1
  }'
```

**Response:**
```json
{
  "id": 1,
  "bookId": 1,
  "bookTitle": "Harry Potter",
  "userId": 1,
  "userName": "John Doe",
  "borrowDate": "2026-01-07",
  "returnDate": null,
  "returned": false
}
```

---

### 6. Return a Book

**Request:**
```bash
curl -X PUT http://localhost:8081/api/borrows/1/return \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Response:**
```json
{
  "id": 1,
  "bookId": 1,
  "bookTitle": "Harry Potter",
  "userId": 1,
  "userName": "John Doe",
  "borrowDate": "2026-01-07",
  "returnDate": "2026-01-14",
  "returned": true
}
```

---

### 7. Get My Active Borrows

**Request:**
```bash
curl -X GET http://localhost:8081/api/borrows/active \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Response:**
```json
[
  {
    "id": 1,
    "bookId": 1,
    "bookTitle": "Harry Potter",
    "userId": 1,
    "userName": "John Doe",
    "borrowDate": "2026-01-07",
    "returnDate": null,
    "returned": false
  }
]
```

---

### 8. Get Statistics (Admin Only)

**Request:**
```bash
curl -X GET http://localhost:8081/api/admin/statistics \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Response:**
```json
{
  "totalBooks": 50,
  "totalUsers": 25,
  "totalBorrowedBooks": 20,
  "totalAvailableBooks": 30,
  "totalBorrowRecords": 100,
  "totalActiveBorrows": 15,
  "popularCategories": [
    {
      "categoryId": 1,
      "categoryName": "Fantasy",
      "bookCount": 10,
      "borrowCount": 25
    }
  ],
  "popularAuthors": [
    {
      "authorId": 1,
      "authorName": "J.K. Rowling",
      "bookCount": 7,
      "borrowCount": 30
    }
  ],
  "mostBorrowedBooks": [
    {
      "bookId": 1,
      "bookTitle": "Harry Potter",
      "authorName": "J.K. Rowling",
      "borrowCount": 15
    }
  ]
}
```

---

### 9. Promote User to Admin

**Request:**
```bash
curl -X PUT http://localhost:8081/api/admin/users/2/promote \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Response:**
```json
{
  "id": 2,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "role": "ADMIN",
  "active": true
}
```

---

### 10. Update User Profile

**Request:**
```bash
curl -X PUT http://localhost:8081/api/user/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "Updated Name"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Updated Name",
  "email": "john@example.com",
  "role": "USER",
  "active": true
}
```

---

### 11. Change Password

**Request:**
```bash
curl -X PUT http://localhost:8081/api/user/profile/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "currentPassword": "oldPassword123",
    "newPassword": "newPassword123"
  }'
```

**Response:** 
```
204 No Content
```

---

### 12. Create an Author (Admin Only)

**Request:**
```bash
curl -X POST http://localhost:8081/api/authors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "F. Scott Fitzgerald"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "F. Scott Fitzgerald"
}
```

---

### 13. Create a Category (Admin Only)

**Request:**
```bash
curl -X POST http://localhost:8081/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "name": "Science Fiction"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Science Fiction"
}
```

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON specification:
```
http://localhost:8081/v3/api-docs
```



## Testing

Run all tests:
```bash
./gradlew test
```


The project includes comprehensive unit tests for:
- Service layer
- Controller layer
- Authentication and authorization
- Business logic validation

## Security Features

- **JWT-based Authentication**: Secure token-based authentication
- **Role-Based Access Control (RBAC)**: Admin and User roles
- **Password Encryption**: BCrypt password hashing
- **Method-Level Security**: `@PreAuthorize` annotations
- **Input Validation**: Jakarta Bean Validation
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries

## Project Structure

```
src/
├── main/
│   ├── java/org/library/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entities/       # JPA entities
│   │   ├── exception/      # Custom exceptions
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # Security configuration
│   │   └── service/        # Business logic layer
│   └── resources/
│       └── application.properties
└── test/
    └── java/               # Test classes
```

## Error Handling

The API returns standard HTTP status codes:

- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `204 No Content`: Successful deletion/update
- `400 Bad Request`: Validation error or bad request
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate)

Error response format:
```json
{
  "message": "Error message",
  "timestamp": "2026-01-07T12:00:00",
  "status": 400
}
```

## Business Rules

### Borrowing Rules
- Maximum 5 active borrows per user
- A book cannot be borrowed if it's already borrowed
- Users can only return their own borrowed books
- Users cannot borrow a book that is already borrowed

### User Management Rules
- Admins cannot demote or deactivate themselves
- Admins cannot delete themselves
- Users cannot promote themselves
- Only admins can manage other users

### Book Management Rules
- Book titles must be unique
- Authors must have unique names
- Categories must have unique names
- Books cannot be deleted if they have active borrow records




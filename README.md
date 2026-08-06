# Personal Finance & Portfolio API

A REST API for personal finance and investment portfolio tracking. Manage bank accounts, log daily expenses in a diary, record stock/ETF buy and sell transactions, and get a real-time portfolio analysis.

## 🛠️ Technologies

* **Java 17**
* **Spring Boot 4.1.0**
* **Spring Web MVC**
* **Spring Data JPA / Hibernate**
* **Spring Security** with **JWT** (jjwt 0.11.5)
* **Spring Validation**
* **MySQL** (via MySQL Connector/J)
* **Lombok**
* **Maven**
* **Springdoc OpenAPI** (Swagger UI)
* **Finnhub API** (real-time market quotes)

## ✨ Features

### Authentication & Security
* User registration and login (`/api/auth/register`, `/api/auth/login`) with BCrypt password hashing.
* Stateless JWT authentication; all endpoints except auth, Swagger and errors require a `Bearer` token.
* Multi-user support: every user only sees and manages their own data.

### Investment Portfolio (`/api/transactions`)
* CRUD of stock/ETF buy and sell transactions (ticker, ISIN, broker, shares, price, commission, total).
* `GET /portfolio`: portfolio analysis with real-time prices from the Finnhub API:
  * weighted average price per position
  * current market price and market value
  * profit/loss in euros and percentage
  * weight of each position in the portfolio

### Expenses & Diary (`/api/diary`)
* CRUD of daily expense/income entries linked to a bank account and a category.
* Automatically updates the bank account balance on create, update and delete.

### Bank Accounts (`/api/accounts`)
* CRUD of bank accounts with initial balance.

### Categories (`/api/categories`)
* CRUD of expense categories (e.g., food, transport, leisure).

### API Quality
* Bean Validation with descriptive error messages.
* Global exception handling with a consistent error response format (`timestamp`, `status`, `error`, `message`, `path`).
* Swagger UI documentation at `/swagger-ui.html`.

## 🚀 Getting Started

### Prerequisites
* Java 17+
* MySQL running on port **3307** with a `finances_db` schema (tables are auto-created via `ddl-auto=update`).
* A Finnhub API key exported as `FINNHUB_API_KEY`.

### Run
```bash
export FINNHUB_API_KEY=your_key
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` by default. Overridable via environment variables: `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `APP_PORT`.

### API Docs
Once running, browse to [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

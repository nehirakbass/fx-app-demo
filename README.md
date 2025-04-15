# Currency Conversion API

A Simple API for real-time currency conversion.

---

## Features

- **Real-time Exchange Rates** (via [currencylayer.com](https://currencylayer.com))
- **Currency Conversion** — Converts amount from one currency to another
- **Conversion History** — View past conversions (with filtering & pagination)
- **Bulk Conversion** — Upload `.csv` or `.xlsx` file for batch conversion
- **Redis Caching** — Caches exchange rates for performance (5 min TTL by default)
- **Dockerized Setup** — Easy to spin up with `docker-compose`
- **Swagger UI** — Test endpoints at [`/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)
- **Unit Tests** — JUnit 5 + Mockito
   93% Method Coverage
   86% Line Coverage
- **Spotless Integration** — Enforces strict code formatting on build


## How to Run

### Run Locally

1. Make sure you have Java 17+ and Maven installed.
2. Set your CurrencyLayer API key in `.env` or as an environment variable:
    API_KEY=your_currencylayer_api_key
3. Then run the app:
```bash
mvn clean install
mvn spring-boot:run
```

Visit: http://localhost:8080/swagger-ui/index.html

### Run with Docker
Make sure Docker is installed, then:

```bash
docker-compose up --build
```
This will start:


- Application on http://localhost:8080

- Redis container for caching

Your .env file will be automatically picked up by Docker Compose.

Example .env
```bash
API_KEY=your_currencylayer_api_key
REDIS_HOST=redis
REDIS_PORT=6379
```

# API Endpoints

## POST /api/check-rate
Get exchange rate between two currencies

### Request
```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "TRY"
}
```

### Response
```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "TRY",
  "rate": 38.04481
}
```

## POST /api/currency-change
Convert currency and save the conversion history

### Request
```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "EUR",
  "amount": 100
}
```

### Response
```json
{
  "transactionId": "b3c8e763-23f1-4c64-9eaa-4f8b79ee9127",
  "convertedAmount": 88.70
}
```

## POST /api/conversion-history
Fetch conversion history (at least one filter must be provided)

### Request
```json
{
  "transactionDate": "2025-04-15T00:00:00"
}
```

Query Params
```json
?page=0&size=10
```


### Response
```json
{
  "content": [
    {
      "transactionId": "b3c8e763-23f1-4c64-9eaa-4f8b79ee9127",
      "sourceCurrency": "USD",
      "targetCurrency": "EUR",
      "amount": 100,
      "convertedAmount": 88.70,
      "rate": 0.9243,
      "timestamp": "2025-04-15T10:42:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 10
}
```

## POST /api/bulk-convert
Upload a file (.csv or .xlsx) for bulk conversion

File Format
Headers must be:

```json
SOURCE_CURRENCY,TARGET_CURRENCY,AMOUNT
```

Example .csv
```json
SOURCE_CURRENCY,TARGET_CURRENCY,AMOUNT
USD,TRY,100
EUR,USD,50
```

### Response
```json
[
  {
    "transactionId": "123e4567-e89b-12d3-a456-426614174000",
    "convertedAmount": 3242.00
  },
  {
    "transactionId": "223e4567-e89b-12d3-a456-426614174001",
    "convertedAmount": 53.10
  }
]
```

# Supported Currencies

Visit -> https://currencylayer.com/currencies

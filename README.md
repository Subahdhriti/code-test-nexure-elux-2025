# Discount-Based Product API

A Kotlin Ktor service for managing products with country-specific VAT calculations and thread-safe discount application.

## Features

- Product catalog management with country-specific pricing
- Country-based VAT calculation
- Concurrent-safe discount application with idempotency guarantees
- PostgreSQL for persistent storage with SERIALIZABLE isolation
- RESTful API with JSON responses

## Prerequisites

- JDK 22 or higher
- Docker and Docker Compose (for containerized deployment)
- OR PostgreSQL 16+ (for local development)

## Build Using Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up --build

# The API will be available at http://localhost:8080
```

The init.sql can be used to initialize the database schema if running PostgreSQL locally.


## API Endpoints

### Get Products by Country

```bash
GET /products?country={country}
```

**Example:**
```bash
curl "http://localhost:8080/products?country=india"
```

**Response:**
```json
[
  {
    "id": "laptop-1",
    "name": "Professional Laptop",
    "basePrice": 1000.0,
    "country": "india",
    "vat": 12.0,
    "discounts": [
      {
        "discountId": "test-discount",
        "percent": 10.0
      }
    ],
    "finalPrice": 1125.0
  }
]
```
### Apply Discount to Product

```bash
PUT /products/{id}/discount
```

**Example:**
```bash
curl -X PUT "http://localhost:8080/products/laptop-1/discount" \
  -H "Content-Type: application/json" \
  -d '{
    "discountId": "test-discount",
    "percent": 10.0
  }'
```

**Response:**
```json
{
  "id": "laptop-1",
  "name": "Professional Laptop",
  "basePrice": 1000.0,
  "country": "india",
  "vat": 0.25,
  "discounts": [
    {
      "discountId": "test-discount",
      "percent": 10.0
    }
  ],
  "finalPrice": 1125.0
}
```

## Price Calculation

The final price is calculated using:

```
finalPrice = basePrice × (1 - totalDiscount%) × (1 + VAT%)
```

**Example:**
- Base Price: 1000
- Discounts: 10% + 5% = 15%
- VAT (india): 19%
- Final Price: 1000 × (1 - 0.15) × (1.19) = 1011.50

## Data Model

### Product
| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Unique identifier |
| `name` | String | Product name |
| `basePrice` | Double | Price before tax and discount |
| `country` | String | Country name |
| `discounts` | List<Discount> | List of applied discounts |

### Discount
| Field | Type | Description |
|--------|------|-------------|
| `discountId` | String | Unique discount identifier (idempotency key) |
| `percent` | Double | Discount percentage (0–100, exclusive of 0) |

### Country VAT Rules
| Country | VAT |
|----------|-----|
| Sweden | 25% |
| Germany | 19% |
| France | 20% |

---

## Concurrency Safety

The application ensures thread-safe discount application through:

1. **Database-Level Constraints**: Composite primary key `(product_id, discount_id)` prevents duplicate entries
2. **SERIALIZABLE Isolation**: Strongest transaction isolation level to prevent race conditions
3. **Idempotent Operations**: Same discount can be applied multiple times without side effects
4. **PostgreSQL Unique Constraints**: Automatically handles concurrent inserts

## Technology Stack

- **Ktor 2.3.7**: Kotlin web framework
- **Exposed**: Kotlin SQL framework
- **PostgreSQL 16**: Relational database
- **HikariCP**: Connection pooling
- **Kotlinx Serialization**: JSON handling
- **JUnit 5**: Testing framework

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| DATABASE_URL | jdbc:postgresql://localhost:5432/productsdb | PostgreSQL connection URL |
| DATABASE_USER | postgres | Database username |
| DATABASE_PASSWORD | postgres | Database password |

## Additional Documentation

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed design decisions and architecture diagrams.

# HealthFirst Pharmacy PIMS

**Student:** Njabulo Maluleke  
**Student Number:** 402110592  
**Module:** Programming 732

HealthFirst Pharmacy PIMS is a Java Swing desktop Pharmacy Inventory Management System backed by MySQL through JDBC.

## Features
- Database-backed authentication and role-based access
- Administrator medicine, supplier and user management
- Cashier POS, stock validation and billing
- Transactional checkout with stock locking and rollback
- Sales, item-wise, low-stock and expiry reports
- Maven build with MySQL Connector/J

## Requirements
- JDK 21+
- MySQL 8+
- Maven 3.9+

Run `database.sql`, then configure `PIMS_DB_URL`, `PIMS_DB_USER` and `PIMS_DB_PASSWORD`.

Sample database accounts:
- Administrator: `admin / admin123`
- Cashier: `cashier / cash123`

Passwords are stored as SHA-256 hashes in the database; authentication is not hard-coded.

Build:
```
mvn clean package
```

The project is published as a public GitHub repository under the student's account. Only actual commits are used in the repository history.

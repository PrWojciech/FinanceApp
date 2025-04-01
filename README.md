# 💳 Wallet – Osobisty Portfel (Spring Boot)

Aplikacja do zarządzania przychodami i wydatkami.

## ✅ Funkcje:
- Rejestracja i logowanie użytkowników (JWT)
- Dodawanie transakcji (przychody / wydatki)
- Kategorie użytkownika
- Swagger UI – dokumentacja API

## 🛠 Stack technologiczny
- Java 21
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Swagger (OpenAPI)
- Maven

## 🚀 Jak uruchomić
1. Ustaw dane do bazy w `application.properties`
2. Uruchom projekt: `mvn spring-boot:run`
3. Swagger dostępny pod: `http://localhost:8080/swagger-ui.html`

## 📁 Struktura
- `controller` – REST API
- `service` – logika biznesowa
- `repository` – dostęp do bazy danych
- `model` – encje JPA
- `dto` – obiekty transferowe
- `security` – JWT, autoryzacja
- `config` – konfiguracje

## 🔒 Endpointy zabezpieczone
Użyj tokena JWT w nagłówku:
```
Authorization: Bearer <token>
```

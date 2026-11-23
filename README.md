# SmartBus – Java Bus Ticket Reservation System

A mobile-responsive bus reservation web application built with Java Spring Boot, HTML, CSS, JavaScript and SQLite.

## Requirements
- Java 17 or newer
- Maven 3.8+
- Internet connection for the first Maven dependency download

## Run locally
1. Extract the project folder.
2. Open a terminal in the `SmartBus` folder.
3. Run: `mvn spring-boot:run`
4. Open: `http://localhost:8080`

On first start, the application creates `data/smartbus.db`, creates the tables automatically through JPA/Hibernate, creates an admin account and inserts sample buses.

## Demo admin login
- Email: `admin@smartbus.com`
- Password: `Admin@123`

Change this demo password before using the application beyond a classroom demonstration.

## Included features
- User registration, login and logout
- Bus route search
- Date-based seat availability
- Seat selection and duplicate-seat checks
- PNR ticket generation
- Ticket cancellation (cancelled seats become available again)
- My bookings
- Admin bus creation and reservation viewing
- SQLite persistence

## Important notes
- Payment choices are demonstrations only. No real payment is processed and no real card data is collected.
- This is an educational starter project, not a production ticketing platform. Before public deployment, add stronger authorization/CSRF protections, rate limiting, audit logging, robust concurrent seat locking, HTTPS and backups.
- SQLite database is stored at `./data/smartbus.db`. Keep the `data` directory persistent on the hosting platform to preserve reservations.
- The sample bus routes are demonstration data and should be edited by the admin.

## Project structure
- `src/main/java/com/smartbus/model` – JPA entities
- `src/main/java/com/smartbus/repository` – database repositories
- `src/main/java/com/smartbus/controller` – REST endpoints
- `src/main/java/com/smartbus/config` – database seed/admin setup
- `src/main/resources/static` – browser interface
- `src/main/resources/application.properties` – SQLite and server settings

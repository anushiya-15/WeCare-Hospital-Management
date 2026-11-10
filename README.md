# Spring REST – Health Care Domain Capstone – WeCARE
Wellness coaching appointment system. Spring Boot 3 · Spring REST · Spring Data JPA · MySQL · HTML/CSS/JS · Postman

## Requirements
IntelliJ IDEA · JDK 17+ · MySQL 8 + Workbench · Postman

## Run
1. Open the `wecare` folder in IntelliJ (wait for Maven).
2. Set MySQL username/password in `application.properties`.
3. Run `WeCareApplication` → DB `wecare`, 4 coaches, 5 users and 3 appointments are created automatically.
4. Browse http://localhost:8080 · Import `postman/WeCARE.postman_collection.json`.

## Business rules
* Coach code `C1001…`, user code `U1001…`, appointment code `AP100001…` (auto-generated).
* Coach age 22–70, user age 20–100; mobile 10 digits (6–9 start) and unique; user email unique; pincode 6 digits.
* One-hour slots: 9 AM–1 PM and 2 PM–6 PM (S09 S10 S11 S12 S14 S15 S16 S17).
* Book from today up to 7 days ahead; slots already started today are blocked.
* A coach or a user cannot have two active appointments in the same date + slot.
* Reschedule / cancel only while BOOKED; reschedule needs 2+ hours notice; cancel only before the start time.
* Complete (with coach notes) is allowed on or after the appointment date.
* Coaches/users with appointment history cannot be deleted.

## REST API
| Method | URL | Purpose |
|---|---|---|
| GET/POST | /api/coaches | list / add |
| GET/PUT/DELETE | /api/coaches/{code} | view / edit / delete |
| GET | /api/coaches/{code}/slots?date= | slot availability |
| GET/POST | /api/users | list / register |
| GET/PUT/DELETE | /api/users/{code} | view / edit / delete |
| POST/GET | /api/appointments (?status=&coachCode=&userCode=&date=) | book / list |
| GET | /api/appointments/{code} | view |
| PUT | /api/appointments/{code}/reschedule · cancel · complete | lifecycle |
| GET | /api/slots, /api/stats | helpers / dashboard |

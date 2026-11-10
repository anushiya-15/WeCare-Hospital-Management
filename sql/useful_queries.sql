USE wecare;
SELECT * FROM coaches;
SELECT * FROM wecare_users;
SELECT * FROM appointments ORDER BY appointment_date DESC, slot DESC;

-- Add a coach straight from MySQL (code must be unique; appears on the Coaches page after refresh)
INSERT INTO coaches (code, name, date_of_birth, gender, mobile, specialization)
VALUES ('C2001', 'Neha Kapoor', '1987-04-09', 'Female', '9811100055', 'Pregnancy Wellness');

-- Block a slot manually (slot values: S09 S10 S11 S12 S14 S15 S16 S17)
-- Appointments per coach
SELECT c.name, COUNT(*) AS appointments FROM appointments a JOIN coaches c ON a.coach_id = c.id
WHERE a.status <> 'CANCELLED' GROUP BY c.name ORDER BY appointments DESC;

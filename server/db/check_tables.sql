USE integra_agency;
SELECT table_name FROM information_schema.tables WHERE table_schema='integra_agency' AND table_type='BASE TABLE' ORDER BY table_name;
SELECT COUNT(*) AS total_tabel FROM information_schema.tables WHERE table_schema='integra_agency' AND table_type='BASE TABLE';
SHOW COLUMNS FROM projects;
SHOW COLUMNS FROM payments;

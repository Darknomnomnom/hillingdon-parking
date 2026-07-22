-- ============================================================
-- Hillingdon Hospital AI Adaptive Parking System
-- Supabase PostgreSQL Schema
-- ============================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- ENUMS
-- ============================================================

CREATE TYPE user_role AS ENUM ('patient', 'staff', 'admin');

CREATE TYPE bay_type AS ENUM ('standard', 'accessible', 'ev', 'premium', 'specific_needs');

CREATE TYPE bay_status AS ENUM ('available', 'reserved', 'occupied', 'out_of_service');

CREATE TYPE booking_status AS ENUM ('pending', 'confirmed', 'arrived', 'completed', 'cancelled', 'no_show');

CREATE TYPE visit_type AS ENUM ('outpatient', 'planned_admission', 'other');

CREATE TYPE anpr_direction AS ENUM ('entry', 'exit');

CREATE TYPE badge_status AS ENUM ('pending', 'verified', 'rejected');

-- ============================================================
-- TABLES
-- ============================================================

-- Users (patients and staff)
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    first_name    TEXT NOT NULL,
    last_name     TEXT NOT NULL,
    role          user_role NOT NULL DEFAULT 'patient',
    phone         TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Parking floors / zones
CREATE TABLE floors (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number      INTEGER NOT NULL,
    name        TEXT NOT NULL,
    total_bays  INTEGER NOT NULL,
    type        TEXT NOT NULL DEFAULT 'multi_storey',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(number)
);

-- Individual parking bays
CREATE TABLE bays (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    floor_id      UUID NOT NULL REFERENCES floors(id) ON DELETE CASCADE,
    bay_number    TEXT NOT NULL,
    type          bay_type NOT NULL DEFAULT 'standard',
    status        bay_status NOT NULL DEFAULT 'available',
    is_accessible BOOLEAN NOT NULL DEFAULT FALSE,
    is_ev         BOOLEAN NOT NULL DEFAULT FALSE,
    is_premium    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(floor_id, bay_number)
);

-- Registered vehicles
-- Blue Badge is issued to a person (user), not a vehicle — badge fields live in the badges table.
-- is_whitelisted is set TRUE once the user's badge is verified, enabling frictionless future visits.
CREATE TABLE vehicles (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plate          TEXT UNIQUE NOT NULL,
    user_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    make           TEXT,
    model          TEXT,
    colour         TEXT,
    is_whitelisted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Blue Badge records (one per badge upload/verification cycle)
CREATE TABLE badges (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_id       UUID REFERENCES vehicles(id) ON DELETE SET NULL,
    badge_number     TEXT NOT NULL,
    photo_url        TEXT NOT NULL,
    status           badge_status NOT NULL DEFAULT 'pending',
    expires_at       DATE NOT NULL,          -- DATE is correct: badges expire on a date, not a time
    verified_by      UUID REFERENCES users(id) ON DELETE SET NULL,
    verified_at      TIMESTAMPTZ,
    rejection_reason TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Parking bookings
CREATE TABLE bookings (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bay_id               UUID REFERENCES bays(id) ON DELETE SET NULL,
    plate                TEXT NOT NULL,
    status               booking_status NOT NULL DEFAULT 'pending',
    visit_type           visit_type NOT NULL DEFAULT 'outpatient',
    appointment_time     TIMESTAMPTZ NOT NULL,
    arrival_window_start TIMESTAMPTZ NOT NULL,
    arrival_window_end   TIMESTAMPTZ NOT NULL,
    confirmation_code    TEXT UNIQUE NOT NULL DEFAULT upper(substr(md5(random()::text), 1, 8)),
    notes                TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ANPR plate-read events (simulated in POC via Quartz job)
CREATE TABLE anpr_events (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plate              TEXT NOT NULL,
    direction          anpr_direction NOT NULL,
    matched_booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    bay_id             UUID REFERENCES bays(id) ON DELETE SET NULL,
    camera_id          TEXT,
    timestamp          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_simulated       BOOLEAN NOT NULL DEFAULT FALSE
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_bays_floor_id    ON bays(floor_id);
CREATE INDEX idx_bays_status      ON bays(status);
CREATE INDEX idx_bays_type        ON bays(type);

CREATE INDEX idx_bookings_patient ON bookings(patient_id);
CREATE INDEX idx_bookings_bay     ON bookings(bay_id);
CREATE INDEX idx_bookings_plate   ON bookings(plate);
CREATE INDEX idx_bookings_status  ON bookings(status);
CREATE INDEX idx_bookings_appt    ON bookings(appointment_time);

CREATE INDEX idx_anpr_plate       ON anpr_events(plate);
CREATE INDEX idx_anpr_timestamp   ON anpr_events(timestamp);
CREATE INDEX idx_anpr_booking     ON anpr_events(matched_booking_id);

CREATE INDEX idx_vehicles_user    ON vehicles(user_id);
CREATE INDEX idx_vehicles_plate   ON vehicles(plate);

CREATE INDEX idx_badges_user      ON badges(user_id);
CREATE INDEX idx_badges_status    ON badges(status);

-- ============================================================
-- UPDATED_AT TRIGGER
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_bays_updated_at
    BEFORE UPDATE ON bays
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_badges_updated_at
    BEFORE UPDATE ON badges
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ============================================================
-- SEED DATA — Floors
-- ============================================================

INSERT INTO floors (number, name, total_bays, type) VALUES
    (0, 'Ground Floor',              120, 'multi_storey'),
    (1, 'Floor 1 — Specific Needs', 150, 'specific_needs'),
    (2, 'Floor 2',                   150, 'multi_storey'),
    (3, 'Floor 3',                   150, 'multi_storey'),
    (4, 'Floor 4',                   130, 'multi_storey'),
    (5, 'Floor 5 — EV & Premium',    50,  'multi_storey');

-- total_bays is used for display only; live occupancy always comes from COUNT on bays table

-- ============================================================
-- SEED DATA — Bays
-- ============================================================

-- Ground floor: 20 accessible + 100 standard
INSERT INTO bays (floor_id, bay_number, type, is_accessible)
SELECT f.id,
       'G-' || gs.n,
       CASE WHEN gs.n <= 20 THEN 'accessible'::bay_type ELSE 'standard'::bay_type END,
       gs.n <= 20
FROM floors f, generate_series(1, 120) AS gs(n)
WHERE f.number = 0;

-- Floor 1: specific_needs + accessible
INSERT INTO bays (floor_id, bay_number, type, is_accessible)
SELECT f.id,
       '1-' || gs.n,
       'specific_needs'::bay_type,
       TRUE
FROM floors f, generate_series(1, 150) AS gs(n)
WHERE f.number = 1;

-- Floors 2-3: standard (150 bays each)
INSERT INTO bays (floor_id, bay_number, type)
SELECT f.id,
       f.number || '-' || gs.n,
       'standard'::bay_type
FROM floors f, generate_series(1, 150) AS gs(n)
WHERE f.number IN (2, 3);

-- Floor 4: standard (130 bays — matches total_bays in floors seed)
INSERT INTO bays (floor_id, bay_number, type)
SELECT f.id,
       '4-' || gs.n,
       'standard'::bay_type
FROM floors f, generate_series(1, 130) AS gs(n)
WHERE f.number = 4;

-- Floor 5: 30 EV + 20 premium
INSERT INTO bays (floor_id, bay_number, type, is_ev, is_premium)
SELECT f.id,
       '5-' || gs.n,
       CASE WHEN gs.n <= 30 THEN 'ev'::bay_type ELSE 'premium'::bay_type END,
       gs.n <= 30,
       gs.n > 30
FROM floors f, generate_series(1, 50) AS gs(n)
WHERE f.number = 5;

-- ============================================================
-- SEED DATA — Demo Users
-- NOTE: Demo users are created via the /api/auth/register endpoint
-- during initial Spring Boot setup. Passwords are BCrypt-hashed
-- by Spring Security before being stored. Do not insert raw or
-- placeholder hashes here.
-- ============================================================

-- ============================================================
-- ROW LEVEL SECURITY
-- NOTE: RLS is intentionally disabled for the POC.
-- All access control is enforced by Spring Security middleware
-- (JWT role checks in AuthMiddleware.java).
-- Re-enable RLS with proper Supabase Auth integration post-POC.
-- ============================================================

-- Tables are accessible to the service role key used by Spring Boot.
-- No RLS policies are applied at this stage.

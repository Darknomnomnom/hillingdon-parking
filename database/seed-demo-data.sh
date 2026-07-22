#!/usr/bin/env bash
#
# Seeds demo data for a live demo: patient/staff accounts, bookings spread
# across statuses (confirmed, arrived, no-show, cancelled), and one pending
# Blue Badge submission for the badge queue.
#
# Goes through the real REST API (not raw SQL) — booking creation runs bay
# assignment logic (BayAssignmentService), and passwords must be BCrypt-hashed
# by Spring Security, so inserting rows directly would bypass both.
#
# Requires: backend running locally (see CLAUDE.md "Run backend locally"),
# floors/bays already seeded via schema.sql, curl, python3.
# An admin user must already exist (see CLAUDE.md admin bootstrap SQL) —
# pass its credentials via ADMIN_EMAIL / ADMIN_PASSWORD.

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:?Set ADMIN_EMAIL to an already-bootstrapped admin account}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:?Set ADMIN_PASSWORD for that admin account}"
DEMO_PASSWORD="Demo1234!"

json_get() { python3 -c "import sys, json; print(json.load(sys.stdin)$1)"; }

echo "== Logging in as admin =="
ADMIN_TOKEN=$(curl -sf -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
    | json_get "['token']")

register_patient() {
    local email="$1" first="$2" last="$3"
    curl -sf -X POST "$BASE_URL/api/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$DEMO_PASSWORD\",\"firstName\":\"$first\",\"lastName\":\"$last\"}" \
        | json_get "['token']"
}

echo "== Registering demo patients =="
TOKEN_ARRIVED=$(register_patient "patient.arrived@demo.hillingdon.nhs.uk" "Alice" "Arrived")
TOKEN_BADGE=$(register_patient "patient.badge@demo.hillingdon.nhs.uk" "Bilal" "BlueBadge")
TOKEN_NOSHOW=$(register_patient "patient.noshow@demo.hillingdon.nhs.uk" "Carol" "NoShow")
TOKEN_CANCELLED=$(register_patient "patient.cancelled@demo.hillingdon.nhs.uk" "David" "Cancelled")
TOKEN_UPCOMING=$(register_patient "patient.upcoming@demo.hillingdon.nhs.uk" "Emma" "Upcoming")
TOKEN_STAFF_USER=$(register_patient "staff.demo@demo.hillingdon.nhs.uk" "Sam" "Staff")

echo "== Promoting staff.demo to STAFF role =="
STAFF_USER_ID=$(curl -sf "$BASE_URL/api/admin/users" -H "Authorization: Bearer $ADMIN_TOKEN" \
    | python3 -c "import sys,json; u=[x for x in json.load(sys.stdin) if x['email']=='staff.demo@demo.hillingdon.nhs.uk'][0]; print(u['id'])")
curl -sf -X PATCH "$BASE_URL/api/admin/users/$STAFF_USER_ID/role" \
    -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
    -d '{"role":"STAFF"}' > /dev/null

create_booking() {
    local token="$1" plate="$2" visit_type="$3" needs_accessible="$4" appt_time="$5"
    curl -sf -X POST "$BASE_URL/api/bookings" \
        -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
        -d "{\"plate\":\"$plate\",\"visitType\":\"$visit_type\",\"appointmentTime\":\"$appt_time\",\"needsAccessible\":$needs_accessible}"
}

# appointmentTime must be in the future (@Future) regardless of the demo status
# we're about to force onto the booking via staff overrides below.
in_hours() { python3 -c "import datetime; print((datetime.datetime.utcnow()+datetime.timedelta(hours=$1)).strftime('%Y-%m-%dT%H:%M:%SZ'))"; }

echo "== Creating bookings =="
BOOKING_ARRIVED=$(create_booking "$TOKEN_ARRIVED" "AA11ARR" "OUTPATIENT" false "$(in_hours 2)")
BOOKING_BADGE=$(create_booking "$TOKEN_BADGE" "BB22BLU" "OUTPATIENT" true "$(in_hours 4)")
BOOKING_NOSHOW=$(create_booking "$TOKEN_NOSHOW" "CC33NOS" "PLANNED_ADMISSION" false "$(in_hours 1)")
BOOKING_CANCELLED=$(create_booking "$TOKEN_CANCELLED" "DD44CAN" "OTHER" false "$(in_hours 6)")
create_booking "$TOKEN_UPCOMING" "EE55UPC" "OUTPATIENT" false "$(in_hours 26)" > /dev/null

ARRIVED_ID=$(echo "$BOOKING_ARRIVED" | json_get "['id']")
NOSHOW_ID=$(echo "$BOOKING_NOSHOW" | json_get "['id']")
CANCELLED_ID=$(echo "$BOOKING_CANCELLED" | json_get "['id']")

echo "== Simulating arrival for AA11ARR (staff action) =="
curl -sf -X POST "$BASE_URL/api/anpr/event?plate=AA11ARR&direction=ENTRY" \
    -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null

echo "== Marking CC33NOS as no-show (staff override) =="
curl -sf -X PATCH "$BASE_URL/api/bookings/$NOSHOW_ID/no-show" \
    -H "Authorization: Bearer $ADMIN_TOKEN" > /dev/null

echo "== Cancelling DD44CAN (patient action) =="
curl -sf -X PATCH "$BASE_URL/api/bookings/$CANCELLED_ID/cancel" \
    -H "Authorization: Bearer $TOKEN_CANCELLED" > /dev/null

echo "== Submitting a pending Blue Badge for patient.badge (badge queue demo) =="
PLACEHOLDER_PNG="$(mktemp /tmp/badge-photo-XXXX.png)"
# 1x1 transparent PNG, just enough to satisfy the multipart upload + Supabase Storage
python3 -c "import base64; open('$PLACEHOLDER_PNG','wb').write(base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII='))"
curl -sf -X POST "$BASE_URL/api/badges/submit" \
    -H "Authorization: Bearer $TOKEN_BADGE" \
    -F "plate=BB22BLU" -F "badgeNumber=BB-DEMO-001" -F "expiresAt=2027-01-01" \
    -F "photo=@$PLACEHOLDER_PNG;type=image/png" > /dev/null
rm -f "$PLACEHOLDER_PNG"

cat <<EOF

== Demo data seeded ==
All demo accounts use password: $DEMO_PASSWORD

  patient.arrived@demo.hillingdon.nhs.uk   -> booking ARRIVED (AA11ARR)
  patient.badge@demo.hillingdon.nhs.uk     -> booking CONFIRMED + Blue Badge PENDING review (BB22BLU)
  patient.noshow@demo.hillingdon.nhs.uk    -> booking NO_SHOW (CC33NOS)
  patient.cancelled@demo.hillingdon.nhs.uk -> booking CANCELLED (DD44CAN)
  patient.upcoming@demo.hillingdon.nhs.uk  -> booking CONFIRMED, appointment in 26h (EE55UPC)
  staff.demo@demo.hillingdon.nhs.uk        -> STAFF role, no personal bookings

Admin: $ADMIN_EMAIL (unchanged, as bootstrapped)
EOF

create table app_user (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(32) not null,
    active boolean not null default true,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table patient_profile (
    id bigserial primary key,
    user_id bigint not null unique references app_user(id),
    full_name varchar(160) not null,
    age integer,
    medical_history text,
    fcm_device_token text,
    whatsapp_number varchar(32),
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table refresh_token (
    id bigserial primary key,
    user_id bigint not null references app_user(id) on delete cascade,
    token_hash varchar(128) not null unique,
    expires_at timestamp with time zone not null,
    revoked_at timestamp with time zone,
    created_at timestamp with time zone not null default now()
);

create table blacklisted_token (
    id bigserial primary key,
    token_hash varchar(128) not null unique,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null default now()
);

create table prescription (
    id bigserial primary key,
    patient_id bigint not null references patient_profile(id),
    s3_bucket varchar(255) not null,
    s3_key varchar(700) not null,
    original_filename varchar(255) not null,
    content_type varchar(120) not null,
    status varchar(32) not null,
    extracted_text text,
    uploaded_at timestamp with time zone not null default now()
);

create table medicine (
    id bigserial primary key,
    prescription_id bigint references prescription(id),
    patient_id bigint not null references patient_profile(id),
    name varchar(180) not null,
    dosage varchar(120),
    frequency varchar(60),
    duration varchar(120),
    confidence numeric(5,2),
    manually_verified boolean not null default false,
    created_at timestamp with time zone not null default now()
);

create table medicine_schedule (
    id bigserial primary key,
    patient_id bigint not null references patient_profile(id),
    medicine_id bigint references medicine(id),
    medicine_name varchar(180) not null,
    dosage varchar(120),
    frequency varchar(40) not null,
    recurrence_type varchar(40) not null,
    custom_interval_hours integer,
    start_date date not null,
    end_date date,
    active boolean not null default true,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table schedule_time_slot (
    id bigserial primary key,
    schedule_id bigint not null references medicine_schedule(id) on delete cascade,
    reminder_time time not null
);

create table reminder_log (
    id bigserial primary key,
    patient_id bigint not null references patient_profile(id),
    schedule_id bigint not null references medicine_schedule(id),
    due_at timestamp with time zone not null,
    status varchar(40) not null,
    snoozed_until timestamp with time zone,
    idempotency_key varchar(160) not null unique,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table notification_log (
    id bigserial primary key,
    patient_id bigint not null references patient_profile(id),
    schedule_id bigint,
    channel varchar(40) not null,
    provider_message_id varchar(255),
    status varchar(40) not null,
    response_body text,
    sent_at timestamp with time zone not null default now()
);

create table diet_plan (
    id bigserial primary key,
    patient_id bigint not null references patient_profile(id),
    meal_type varchar(40) not null,
    description text not null,
    scheduled_time time not null,
    dietary_restrictions text,
    calories integer,
    start_date date not null,
    end_date date,
    active boolean not null default true,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table diet_reminder_log (
    id bigserial primary key,
    patient_id bigint not null references patient_profile(id),
    diet_plan_id bigint not null references diet_plan(id),
    due_at timestamp with time zone not null,
    status varchar(40) not null,
    idempotency_key varchar(160) not null unique,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

create table audit_log (
    id bigserial primary key,
    actor_user_id bigint references app_user(id),
    action varchar(120) not null,
    resource_type varchar(120) not null,
    resource_id varchar(120),
    ip_address varchar(64),
    user_agent text,
    created_at timestamp with time zone not null default now()
);

create index idx_prescription_patient_uploaded_at on prescription(patient_id, uploaded_at desc);
create index idx_refresh_token_user_id on refresh_token(user_id);
create index idx_blacklisted_token_expires_at on blacklisted_token(expires_at);
create index idx_medicine_schedule_patient_active on medicine_schedule(patient_id, active);
create index idx_reminder_log_due_status on reminder_log(due_at, status);
create index idx_notification_log_patient_sent_at on notification_log(patient_id, sent_at desc);
create index idx_diet_plan_patient_active on diet_plan(patient_id, active);
create index idx_diet_reminder_log_due_status on diet_reminder_log(due_at, status);

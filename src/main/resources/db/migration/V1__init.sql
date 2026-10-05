create table orders (
    id               uuid primary key,
    customer_id      varchar(64) not null,
    status           varchar(16) not null check (status in ('PENDING', 'CONFIRMED', 'REJECTED')),
    rejection_reason text,
    created_at       timestamptz not null,
    version          bigint      not null
);

create table order_line (
    id       uuid primary key,
    order_id uuid        not null references orders (id) on delete cascade,
    line_no  integer     not null,
    sku      varchar(64) not null,
    quantity integer     not null check (quantity > 0),
    unique (order_id, line_no)
);

create table outbox (
    id           uuid primary key,
    aggregate_id uuid         not null,
    event_type   varchar(64)  not null,
    topic        varchar(128) not null,
    message_key  varchar(64)  not null,
    payload      text         not null,
    created_at   timestamptz  not null,
    sent_at      timestamptz
);

create index idx_outbox_pending on outbox (created_at) where sent_at is null;

create table processed_event (
    event_id     uuid primary key,
    processed_at timestamptz not null
);

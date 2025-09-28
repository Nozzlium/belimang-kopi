create table if not exists users (
	id bigserial primary key,
	username varchar not null unique,
	password varchar not null,
	email varchar not null,

	created_at timestamp default current_timestamp,
	updated_at timestamp default current_timestamp
);

create table if not exists images (
	id bigserial primary key,
	url varchar not null,

	created_at timestamp default current_timestamp,
	updated_at timestamp default current_timestamp
);

create table if not exists merchants (
	id bigserial primary key,
	category varchar not null,
    name varchar not null,
	image_url varchar,

	created_at timestamp default current_timestamp,
	updated_at timestamp default current_timestamp
);

create table if not exists merchant_items (
	id bigserial primary key,
	merchant_id bigint references merchants(id) on update cascade on delete cascade,
	category varchar not null,
	name varchar not null,
	price bigint not null,
	image_url varchar,

	created_at timestamp default current_timestamp,
	updated_at timestamp default current_timestamp
);
create index if not exists idx_merchant_items_merchant_id on merchant_items(merchant_id);

create table if not exists orders (
	id bigserial primary key,
	has_order_made bool not null default false,

	created_at timestamp default current_timestamp,
	updated_at timestamp default current_timestamp
);

create table if not exists order_details (
	id bigserial primary key,
	order_id bigint references orders(id) on update cascade on delete cascade,
	merchant_id bigint,
	items varchar,
	total_price bigint,

	created_at timestamp default current_timestamp,
	updated_at timestamp default current_timestamp
);
create index if not exists idx_order_details_order_id on order_details(order_id);
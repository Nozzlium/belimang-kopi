alter table users
add column if not exists is_admin boolean not null;

create index if not exists idx_users_username on users(username);
create index if not exists idx_users_email on users(email);
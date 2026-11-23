-- Нагрузочные данные под NFT: 10 000 пользователей и 100 000 задач.
-- Запуск: docker compose exec -T postgres psql -U tasks -d tasks < scripts/seed-load-data.sql
insert into users (name, email)
select 'Load User ' || g, 'load-user-' || g || '@example.com'
from generate_series(1, 10000) g
on conflict (email) do nothing;

insert into tasks (title, description, status, assignee_id, created_at, updated_at)
select 'Load task ' || g,
       'Generated description ' || g,
       (array ['NEW', 'IN_PROGRESS', 'DONE'])[1 + g % 3],
       (select id from users where email = 'load-user-' || (1 + g % 10000) || '@example.com'),
       now() - (g || ' seconds')::interval,
       now()
from generate_series(1, 100000) g;

analyze users;
analyze tasks;

// Нагрузочный тест (NFT: до 10k пользователей, до 100k задач).
//
// Подготовка: загрузить данные task-service/scripts/seed-load-data.sql, затем
//   k6 run task-service/scripts/load-test.js
// либо без установки k6:
//   docker run --rm -i --network <сеть compose> -e BASE_URL=http://task-service:8080 grafana/k6 run - < task-service/scripts/load-test.js
//
// Переменные: BASE_URL (по умолчанию http://localhost:8080), VUS (50), THINK_TIME в секундах (0.1).
//
// Распределение запросов — допущение о реальной работе пользователей, а не измерение:
//   50% список (из них 80% первые 10 страниц, 15% страницы до 500, 5% глубокие до 5000),
//   10% список с фильтрами, 30% получение по id, 10% создание.
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = Number(__ENV.VUS || 50);
const THINK_TIME = Number(__ENV.THINK_TIME || 0.1);
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

export const options = {
  stages: [
    { duration: '20s', target: VUS },
    { duration: '1m', target: VUS },
    { duration: '10s', target: 0 },
  ],
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'max'],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{name:list-first-pages}': ['p(95)<300'],
    'http_req_duration{name:list-mid-pages}': ['p(95)<300'],
    'http_req_duration{name:list-deep-pages}': ['p(95)<500'],
    'http_req_duration{name:list-filtered}': ['p(95)<300'],
    'http_req_duration{name:get-by-id}': ['p(95)<200'],
    'http_req_duration{name:create}': ['p(95)<300'],
  },
};

function randomInt(from, to) {
  return from + Math.floor(Math.random() * (to - from + 1));
}

function listPage() {
  const r = Math.random();
  if (r < 0.8) {
    return { page: randomInt(0, 9), name: 'list-first-pages' };
  }
  if (r < 0.95) {
    return { page: randomInt(10, 499), name: 'list-mid-pages' };
  }
  return { page: randomInt(500, 4999), name: 'list-deep-pages' };
}

export default function () {
  const r = Math.random();

  if (r < 0.5) {
    const { page, name } = listPage();
    const res = http.get(`${BASE}/api/v1/tasks?page=${page}&size=20`, { tags: { name } });
    check(res, { 'list 200': (x) => x.status === 200 });
  } else if (r < 0.6) {
    const status = ['NEW', 'IN_PROGRESS', 'DONE'][randomInt(0, 2)];
    const res = http.get(`${BASE}/api/v1/tasks?status=${status}&assigneeId=${randomInt(1, 10000)}&size=20`, {
      tags: { name: 'list-filtered' },
    });
    check(res, { 'filtered list 200': (x) => x.status === 200 });
  } else if (r < 0.9) {
    const res = http.get(`${BASE}/api/v1/tasks/${randomInt(1, 100000)}`, { tags: { name: 'get-by-id' } });
    check(res, { 'get 200': (x) => x.status === 200 });
  } else {
    const body = JSON.stringify({ title: `k6 task ${Date.now()}`, assigneeId: randomInt(1, 10000) });
    const res = http.post(`${BASE}/api/v1/tasks`, body, { ...JSON_HEADERS, tags: { name: 'create' } });
    check(res, { 'create 201': (x) => x.status === 201 });
  }

  sleep(Math.random() * THINK_TIME * 2);
}

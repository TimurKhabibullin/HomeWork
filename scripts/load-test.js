// k6 run scripts/load-test.js   (предварительно загрузить scripts/seed-load-data.sql)
import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  stages: [
    { duration: '20s', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300'],
  },
};

export default function () {
  const r = Math.random();
  if (r < 0.6) {
    const page = Math.floor(Math.random() * 5000);
    const res = http.get(`${BASE}/api/v1/tasks?page=${page}&size=20`);
    check(res, { 'list 200': (x) => x.status === 200 });
  } else if (r < 0.9) {
    const id = 1 + Math.floor(Math.random() * 100000);
    const res = http.get(`${BASE}/api/v1/tasks/${id}`);
    check(res, { 'get 200/404': (x) => x.status === 200 || x.status === 404 });
  } else {
    const res = http.post(
      `${BASE}/api/v1/tasks`,
      JSON.stringify({ title: `k6 task ${Date.now()}`, assigneeId: 1 }),
      { headers: { 'Content-Type': 'application/json' } },
    );
    check(res, { 'create 201': (x) => x.status === 201 });
  }
}

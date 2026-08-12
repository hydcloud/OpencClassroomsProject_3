import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '30s',
};

export default function () {

    const loginPayload = JSON.stringify({
        email: 'stephane@gmail.com',
        password: 'MotDePasse123',
    });

    const loginHeaders = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const loginResponse = http.post(
        'http://localhost:8080/api/login',
        loginPayload,
        loginHeaders
    );

    check(loginResponse, {
        'login status is 200': response => response.status === 200,
    });

    const token = loginResponse.json('token');

    const response = http.get(
        'http://localhost:8080/api/files',
        {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        }
    );

    check(response, {
        'history status is 200': response => response.status === 200,
        'response time < 500 ms': response => response.timings.duration < 500,
    });

    sleep(1);
}
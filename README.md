# auth-service

Аутентификация и управление пользователями АСНИ. Выдаёт JWT-токены, другие сервисы проверяют их через `/api/v1/auth/validate`.

## Запуск

```bash
docker compose up -d
```

Swagger: http://localhost:8081/swagger-ui.html

Дефолтный admin: `admin` / `Admin@1234!`

## Переменные окружения

Переопределяются через `.env` рядом с `docker-compose.yml`:

```env
JWT_SECRET=your-256bit-secret
ADMIN_DEFAULT_PASSWORD=StrongPass1!
```

## Тесты

```bash
./gradlew test
```

Требует запущенный Docker (TestContainers поднимает Postgres и Redis сам).

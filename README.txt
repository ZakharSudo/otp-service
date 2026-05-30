##  О проекте

Сервис предназначен для защиты операций с помощью временных кодов подтверждения (OTP). Реализована полная инфраструктура для генерации, отправки и проверки одноразовых кодов через различные каналы связи.

### Основной функционал

- ✅ Регистрация и аутентификация пользователей (JWT токены)
- ✅ Генерация OTP кодов (6 цифр, время жизни 5 минут)
- ✅ Валидация OTP кодов с проверкой срока действия
- ✅ Отправка кодов через Email, SMS, Telegram
- ✅ Сохранение кодов в файл
- ✅ Ролевая модель (ADMIN / USER)
- ✅ API для администратора
- ✅ Логирование всех операций

##  Запуск приложения 

### Требования
- Java 17 или выше
- Maven 3.8+

### Команды для запуска (в репозитории проекта)

```bash
# Сборка проекта
mvn clean compile

# Запуск приложения
mvn spring-boot:run

После успешного запуска вы увидите:

text
========================================
OTP Service Started Successfully!
API available at: http://localhost:8080
========================================

 API Endpoints
Публичные эндпоинты
Метод	Эндпоинт	Описание
GET	/	Информация о сервисе
GET	/health	Проверка состояния сервиса
Аутентификация
Метод	Эндпоинт	Описание
POST	/api/auth/register	Регистрация нового пользователя
POST	/api/auth/login	Вход и получение JWT токена


Тело запроса для регистрации:

json
{
    "login": "testuser",
    "password": "123456",
    "role": "USER"
}
Ответ при регистрации:

json
{
    "message": "User registered successfully",
    "role": "USER"
}
Тело запроса для логина:

json
{
    "login": "testuser",
    "password": "123456"
}
Ответ при логине:

json
{
    "token": "uuid-token",
    "role": "USER"
}

Пользовательские эндпоинты (требуется токен)
Метод	Эндпоинт	Описание
POST	/api/otp/generate	Генерация OTP кода
POST	/api/otp/validate	Проверка OTP кода
Заголовок для авторизации:

text
Authorization: Bearer <token>
Тело запроса для генерации OTP:

json
{
    "channel": "EMAIL",
    "destination": "user@example.com"
}
Доступные каналы: EMAIL, SMS, TELEGRAM, FILE

Ответ при генерации:

json
{
    "operationId": "uuid",
    "code": "123456",
    "message": "OTP sent via EMAIL and saved to file"
}
Тело запроса для валидации OTP:

json
{
    "operationId": "uuid",
    "code": "123456"
}
Ответ при валидации:

json
{
    "valid": true
}

Административные эндпоинты (требуется роль ADMIN)
Метод	Эндпоинт	Описание
GET	/api/admin/users	Получение списка пользователей
DELETE	/api/admin/users/{login}	Удаление пользователя
GET	/api/admin/config	Получение конфигурации OTP

 Тестирование API
Полный цикл тестирования (PowerShell)
powershell
# 1. Регистрация пользователя
$registerBody = @{login="testuser"; password="123456"; role="USER"} | ConvertTo-Json
$registerResult = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
Write-Host "Registration: $($registerResult.message)"

# 2. Логин
$loginBody = @{login="testuser"; password="123456"} | ConvertTo-Json
$loginResult = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResult.token
Write-Host "Token received: $($token.Substring(0, 20))..."

# 3. Генерация OTP
$headers = @{"Authorization" = "Bearer $token"}
$otpBody = @{channel="EMAIL"; destination="test@example.com"} | ConvertTo-Json
$otpResult = Invoke-RestMethod -Uri "http://localhost:8080/api/otp/generate" -Method Post -Body $otpBody -ContentType "application/json" -Headers $headers
$operationId = $otpResult.operationId
$code = $otpResult.code
Write-Host "OTP Generated: $code"

# 4. Валидация OTP
$validateBody = @{operationId=$operationId; code=$code} | ConvertTo-Json
$validateResult = Invoke-RestMethod -Uri "http://localhost:8080/api/otp/validate" -Method Post -Body $validateBody -ContentType "application/json" -Headers $headers
Write-Host "Validation result: $($validateResult.valid)"
Тестирование административных функций
powershell
# 1. Регистрация администратора
$adminBody = @{login="admin"; password="admin123"; role="ADMIN"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method Post -Body $adminBody -ContentType "application/json"

# 2. Логин администратора
$loginAdmin = @{login="admin"; password="admin123"} | ConvertTo-Json
$adminResult = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $loginAdmin -ContentType "application/json"
$adminToken = $adminResult.token

# 3. Получение списка пользователей
$headers = @{"Authorization" = "Bearer $adminToken"}
$users = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/users" -Method Get -Headers $headers
Write-Host "Users: $($users | ConvertTo-Json)"

# 4. Получение конфигурации
$config = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/config" -Method Get -Headers $headers
Write-Host "Config: $($config | ConvertTo-Json)"
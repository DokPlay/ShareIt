# Тестирование API ShareIt
$baseUrl = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ТЕСТИРОВАНИЕ API ShareIt" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Создание пользователей
Write-Host "`n1. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЕЙ" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$user1 = @{
    name = "Иван Петров"
    email = "ivan@mail.ru"
} | ConvertTo-Json -Compress

$user2 = @{
    name = "Мария Сидорова"  
    email = "maria@mail.ru"
} | ConvertTo-Json -Compress

try {
    $createdUser1 = Invoke-RestMethod -Uri "$baseUrl/users" -Method POST -Body $user1 -ContentType "application/json"
    Write-Host "✅ Пользователь 1 создан: id=$($createdUser1.id), name=$($createdUser1.name)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка создания пользователя 1: $_" -ForegroundColor Red
}

try {
    $createdUser2 = Invoke-RestMethod -Uri "$baseUrl/users" -Method POST -Body $user2 -ContentType "application/json"
    Write-Host "✅ Пользователь 2 создан: id=$($createdUser2.id), name=$($createdUser2.name)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка создания пользователя 2: $_" -ForegroundColor Red
}

# 2. Получение списка пользователей
Write-Host "`n2. ПОЛУЧЕНИЕ СПИСКА ПОЛЬЗОВАТЕЛЕЙ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET
    Write-Host "✅ Найдено пользователей: $($users.Count)" -ForegroundColor Green
    $users | ForEach-Object { Write-Host "   - id=$($_.id), name=$($_.name), email=$($_.email)" }
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

# 3. Создание вещей
Write-Host "`n3. СОЗДАНИЕ ВЕЩЕЙ" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$item1 = @{
    name = "Дрель"
    description = "Мощная дрель для ремонта"
    available = $true
} | ConvertTo-Json -Compress

$item2 = @{
    name = "Отвертка"
    description = "Набор отверток"
    available = $true
} | ConvertTo-Json -Compress

try {
    $createdItem1 = Invoke-RestMethod -Uri "$baseUrl/items" -Method POST -Body $item1 -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Вещь 1 создана: id=$($createdItem1.id), name=$($createdItem1.name)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка создания вещи 1: $_" -ForegroundColor Red
}

try {
    $createdItem2 = Invoke-RestMethod -Uri "$baseUrl/items" -Method POST -Body $item2 -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Вещь 2 создана: id=$($createdItem2.id), name=$($createdItem2.name)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка создания вещи 2: $_" -ForegroundColor Red
}

# 4. Поиск вещей
Write-Host "`n4. ПОИСК ВЕЩЕЙ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $searchResult = Invoke-RestMethod -Uri "$baseUrl/items/search?text=дрель" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "✅ Найдено вещей по запросу 'дрель': $($searchResult.Count)" -ForegroundColor Green
    $searchResult | ForEach-Object { Write-Host "   - $($_.name): $($_.description)" }
} catch {
    Write-Host "❌ Ошибка поиска: $_" -ForegroundColor Red
}

# 5. Создание бронирования
Write-Host "`n5. СОЗДАНИЕ БРОНИРОВАНИЯ" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$booking = @{
    itemId = $createdItem1.id
    start = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss")
    end = (Get-Date).AddDays(3).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json -Compress

try {
    $createdBooking = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method POST -Body $booking -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "✅ Бронирование создано: id=$($createdBooking.id), status=$($createdBooking.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка создания бронирования: $_" -ForegroundColor Red
}

# 6. Подтверждение бронирования владельцем
Write-Host "`n6. ПОДТВЕРЖДЕНИЕ БРОНИРОВАНИЯ ВЛАДЕЛЬЦЕМ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $approvedBooking = Invoke-RestMethod -Uri "$baseUrl/bookings/$($createdBooking.id)?approved=true" -Method PATCH -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Бронирование подтверждено: status=$($approvedBooking.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка подтверждения: $_" -ForegroundColor Red
}

# 7. Получение списка бронирований пользователя
Write-Host "`n7. СПИСОК БРОНИРОВАНИЙ ПОЛЬЗОВАТЕЛЯ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $userBookings = Invoke-RestMethod -Uri "$baseUrl/bookings?state=ALL" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "✅ Бронирований пользователя: $($userBookings.Count)" -ForegroundColor Green
    $userBookings | ForEach-Object { Write-Host "   - id=$($_.id), item=$($_.item.name), status=$($_.status)" }
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

# 8. Получение списка бронирований владельца
Write-Host "`n8. СПИСОК БРОНИРОВАНИЙ ВЛАДЕЛЬЦА" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $ownerBookings = Invoke-RestMethod -Uri "$baseUrl/bookings/owner?state=ALL" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Бронирований у владельца: $($ownerBookings.Count)" -ForegroundColor Green
    $ownerBookings | ForEach-Object { Write-Host "   - id=$($_.id), item=$($_.item.name), booker=$($_.booker.name), status=$($_.status)" }
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

# 9. Создание завершённого бронирования для комментария
Write-Host "`n9. СОЗДАНИЕ ЗАВЕРШЁННОГО БРОНИРОВАНИЯ ДЛЯ КОММЕНТАРИЯ" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$pastBooking = @{
    itemId = $createdItem2.id
    start = (Get-Date).AddDays(-5).ToString("yyyy-MM-ddTHH:mm:ss")
    end = (Get-Date).AddDays(-3).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json -Compress

try {
    $pastCreatedBooking = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method POST -Body $pastBooking -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "✅ Прошлое бронирование создано: id=$($pastCreatedBooking.id)" -ForegroundColor Green
    
    # Подтверждаем прошлое бронирование
    $approvedPastBooking = Invoke-RestMethod -Uri "$baseUrl/bookings/$($pastCreatedBooking.id)?approved=true" -Method PATCH -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Прошлое бронирование подтверждено: status=$($approvedPastBooking.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

# 10. Добавление комментария
Write-Host "`n10. ДОБАВЛЕНИЕ КОММЕНТАРИЯ" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$comment = @{
    text = "Отличная отвертка! Очень удобная в использовании."
} | ConvertTo-Json -Compress

try {
    $createdComment = Invoke-RestMethod -Uri "$baseUrl/items/$($createdItem2.id)/comment" -Method POST -Body $comment -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "✅ Комментарий добавлен: '$($createdComment.text)' от $($createdComment.authorName)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка добавления комментария: $_" -ForegroundColor Red
}

# 11. Получение вещи с комментариями (для владельца - с датами бронирования)
Write-Host "`n11. ПОЛУЧЕНИЕ ВЕЩИ С КОММЕНТАРИЯМИ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $itemWithComments = Invoke-RestMethod -Uri "$baseUrl/items/$($createdItem2.id)" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Вещь: $($itemWithComments.name)" -ForegroundColor Green
    Write-Host "   Описание: $($itemWithComments.description)"
    if ($itemWithComments.lastBooking) {
        Write-Host "   Последнее бронирование: id=$($itemWithComments.lastBooking.id)"
    }
    if ($itemWithComments.nextBooking) {
        Write-Host "   Следующее бронирование: id=$($itemWithComments.nextBooking.id)"
    }
    if ($itemWithComments.comments -and $itemWithComments.comments.Count -gt 0) {
        Write-Host "   Комментарии:"
        $itemWithComments.comments | ForEach-Object { Write-Host "     - $($_.authorName): $($_.text)" }
    }
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

# 12. Получение списка вещей владельца с бронированиями
Write-Host "`n12. СПИСОК ВЕЩЕЙ ВЛАДЕЛЬЦА С БРОНИРОВАНИЯМИ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $ownerItems = Invoke-RestMethod -Uri "$baseUrl/items" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Вещей у владельца: $($ownerItems.Count)" -ForegroundColor Green
    $ownerItems | ForEach-Object { 
        Write-Host "   - $($_.name) (available=$($_.available))"
        if ($_.lastBooking) { Write-Host "     lastBooking: id=$($_.lastBooking.id)" }
        if ($_.nextBooking) { Write-Host "     nextBooking: id=$($_.nextBooking.id)" }
    }
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

# 13. Фильтрация бронирований по статусу
Write-Host "`n13. ФИЛЬТРАЦИЯ БРОНИРОВАНИЙ ПО СТАТУСУ" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$states = @("CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED")
foreach ($state in $states) {
    try {
        $filteredBookings = Invoke-RestMethod -Uri "$baseUrl/bookings?state=$state" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
        Write-Host "✅ state=$state : найдено $($filteredBookings.Count) бронирований" -ForegroundColor Green
    } catch {
        Write-Host "❌ state=$state : ошибка" -ForegroundColor Red
    }
}

# 14. Обновление пользователя
Write-Host "`n14. ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$updateUser = @{
    name = "Иван Петрович"
} | ConvertTo-Json -Compress

try {
    $updatedUser = Invoke-RestMethod -Uri "$baseUrl/users/$($createdUser1.id)" -Method PATCH -Body $updateUser -ContentType "application/json"
    Write-Host "✅ Пользователь обновлён: name=$($updatedUser.name)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

# 15. Обновление вещи
Write-Host "`n15. ОБНОВЛЕНИЕ ВЕЩИ" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$updateItem = @{
    available = $false
} | ConvertTo-Json -Compress

try {
    $updatedItem = Invoke-RestMethod -Uri "$baseUrl/items/$($createdItem1.id)" -Method PATCH -Body $updateItem -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "✅ Вещь обновлена: available=$($updatedItem.available)" -ForegroundColor Green
} catch {
    Write-Host "❌ Ошибка: $_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "ТЕСТИРОВАНИЕ ЗАВЕРШЕНО" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

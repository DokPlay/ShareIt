# ShareIt API Testing Script
$baseUrl = "http://localhost:8080"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ShareIt API TESTING" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Create users
Write-Host "`n1. CREATE USERS" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$user1 = @{
    name = "Ivan Petrov"
    email = "ivan@mail.ru"
} | ConvertTo-Json -Compress

$user2 = @{
    name = "Maria Sidorova"  
    email = "maria@mail.ru"
} | ConvertTo-Json -Compress

try {
    $createdUser1 = Invoke-RestMethod -Uri "$baseUrl/users" -Method POST -Body $user1 -ContentType "application/json"
    Write-Host "[OK] User 1 created: id=$($createdUser1.id), name=$($createdUser1.name)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error creating user 1: $_" -ForegroundColor Red
}

try {
    $createdUser2 = Invoke-RestMethod -Uri "$baseUrl/users" -Method POST -Body $user2 -ContentType "application/json"
    Write-Host "[OK] User 2 created: id=$($createdUser2.id), name=$($createdUser2.name)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error creating user 2: $_" -ForegroundColor Red
}

# 2. Get all users
Write-Host "`n2. GET ALL USERS" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET
    Write-Host "[OK] Found users: $($users.Count)" -ForegroundColor Green
    $users | ForEach-Object { Write-Host "   - id=$($_.id), name=$($_.name), email=$($_.email)" }
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 3. Create items
Write-Host "`n3. CREATE ITEMS" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$item1 = @{
    name = "Drill"
    description = "Powerful drill for repair"
    available = $true
} | ConvertTo-Json -Compress

$item2 = @{
    name = "Screwdriver"
    description = "Screwdriver set"
    available = $true
} | ConvertTo-Json -Compress

try {
    $createdItem1 = Invoke-RestMethod -Uri "$baseUrl/items" -Method POST -Body $item1 -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Item 1 created: id=$($createdItem1.id), name=$($createdItem1.name)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error creating item 1: $_" -ForegroundColor Red
}

try {
    $createdItem2 = Invoke-RestMethod -Uri "$baseUrl/items" -Method POST -Body $item2 -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Item 2 created: id=$($createdItem2.id), name=$($createdItem2.name)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error creating item 2: $_" -ForegroundColor Red
}

# 4. Search items
Write-Host "`n4. SEARCH ITEMS" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $searchResult = Invoke-RestMethod -Uri "$baseUrl/items/search?text=drill" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "[OK] Found items for 'drill': $($searchResult.Count)" -ForegroundColor Green
    $searchResult | ForEach-Object { Write-Host "   - $($_.name): $($_.description)" }
} catch {
    Write-Host "[FAIL] Search error: $_" -ForegroundColor Red
}

# 5. Create booking
Write-Host "`n5. CREATE BOOKING" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$booking = @{
    itemId = $createdItem1.id
    start = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss")
    end = (Get-Date).AddDays(3).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json -Compress

try {
    $createdBooking = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method POST -Body $booking -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "[OK] Booking created: id=$($createdBooking.id), status=$($createdBooking.status)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error creating booking: $_" -ForegroundColor Red
}

# 6. Approve booking by owner
Write-Host "`n6. APPROVE BOOKING BY OWNER" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $approvedBooking = Invoke-RestMethod -Uri "$baseUrl/bookings/$($createdBooking.id)?approved=true" -Method PATCH -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Booking approved: status=$($approvedBooking.status)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Approval error: $_" -ForegroundColor Red
}

# 7. Get user bookings
Write-Host "`n7. GET USER BOOKINGS" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $userBookings = Invoke-RestMethod -Uri "$baseUrl/bookings?state=ALL" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "[OK] User bookings: $($userBookings.Count)" -ForegroundColor Green
    $userBookings | ForEach-Object { Write-Host "   - id=$($_.id), item=$($_.item.name), status=$($_.status)" }
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 8. Get owner bookings
Write-Host "`n8. GET OWNER BOOKINGS" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $ownerBookings = Invoke-RestMethod -Uri "$baseUrl/bookings/owner?state=ALL" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Owner bookings: $($ownerBookings.Count)" -ForegroundColor Green
    $ownerBookings | ForEach-Object { Write-Host "   - id=$($_.id), item=$($_.item.name), booker=$($_.booker.name), status=$($_.status)" }
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 9. Create past booking for comment
Write-Host "`n9. CREATE PAST BOOKING FOR COMMENT" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$pastBooking = @{
    itemId = $createdItem2.id
    start = (Get-Date).AddDays(-5).ToString("yyyy-MM-ddTHH:mm:ss")
    end = (Get-Date).AddDays(-3).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json -Compress

try {
    $pastCreatedBooking = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method POST -Body $pastBooking -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "[OK] Past booking created: id=$($pastCreatedBooking.id)" -ForegroundColor Green
    
    # Approve past booking
    $approvedPastBooking = Invoke-RestMethod -Uri "$baseUrl/bookings/$($pastCreatedBooking.id)?approved=true" -Method PATCH -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Past booking approved: status=$($approvedPastBooking.status)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 10. Add comment
Write-Host "`n10. ADD COMMENT" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$comment = @{
    text = "Great screwdriver! Very convenient to use."
} | ConvertTo-Json -Compress

try {
    $createdComment = Invoke-RestMethod -Uri "$baseUrl/items/$($createdItem2.id)/comment" -Method POST -Body $comment -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "[OK] Comment added: '$($createdComment.text)' by $($createdComment.authorName)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error adding comment: $_" -ForegroundColor Red
}

# 11. Get item with comments (for owner - with booking dates)
Write-Host "`n11. GET ITEM WITH COMMENTS" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $itemWithComments = Invoke-RestMethod -Uri "$baseUrl/items/$($createdItem2.id)" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Item: $($itemWithComments.name)" -ForegroundColor Green
    Write-Host "   Description: $($itemWithComments.description)"
    if ($itemWithComments.lastBooking) {
        Write-Host "   Last booking: id=$($itemWithComments.lastBooking.id)"
    }
    if ($itemWithComments.nextBooking) {
        Write-Host "   Next booking: id=$($itemWithComments.nextBooking.id)"
    }
    if ($itemWithComments.comments -and $itemWithComments.comments.Count -gt 0) {
        Write-Host "   Comments:"
        $itemWithComments.comments | ForEach-Object { Write-Host "     - $($_.authorName): $($_.text)" }
    }
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 12. Get owner items with bookings
Write-Host "`n12. GET OWNER ITEMS WITH BOOKINGS" -ForegroundColor Yellow
Write-Host "----------------------------------------"
try {
    $ownerItems = Invoke-RestMethod -Uri "$baseUrl/items" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Owner items: $($ownerItems.Count)" -ForegroundColor Green
    $ownerItems | ForEach-Object { 
        Write-Host "   - $($_.name) (available=$($_.available))"
        if ($_.lastBooking) { Write-Host "     lastBooking: id=$($_.lastBooking.id)" }
        if ($_.nextBooking) { Write-Host "     nextBooking: id=$($_.nextBooking.id)" }
    }
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 13. Filter bookings by state
Write-Host "`n13. FILTER BOOKINGS BY STATE" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$states = @("CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED")
foreach ($state in $states) {
    try {
        $filteredBookings = Invoke-RestMethod -Uri "$baseUrl/bookings?state=$state" -Method GET -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
        Write-Host "[OK] state=$state : found $($filteredBookings.Count) bookings" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] state=$state : error" -ForegroundColor Red
    }
}

# 14. Update user
Write-Host "`n14. UPDATE USER" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$updateUser = @{
    name = "Ivan Petrovich"
} | ConvertTo-Json -Compress

try {
    $updatedUser = Invoke-RestMethod -Uri "$baseUrl/users/$($createdUser1.id)" -Method PATCH -Body $updateUser -ContentType "application/json"
    Write-Host "[OK] User updated: name=$($updatedUser.name)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 15. Update item
Write-Host "`n15. UPDATE ITEM" -ForegroundColor Yellow
Write-Host "----------------------------------------"
$updateItem = @{
    available = $false
} | ConvertTo-Json -Compress

try {
    $updatedItem = Invoke-RestMethod -Uri "$baseUrl/items/$($createdItem1.id)" -Method PATCH -Body $updateItem -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Item updated: available=$($updatedItem.available)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

# 16. Reject booking test
Write-Host "`n16. CREATE AND REJECT BOOKING" -ForegroundColor Yellow
Write-Host "----------------------------------------"

$item3 = @{
    name = "Hammer"
    description = "Heavy hammer"
    available = $true
} | ConvertTo-Json -Compress

try {
    $createdItem3 = Invoke-RestMethod -Uri "$baseUrl/items" -Method POST -Body $item3 -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Item 3 created: id=$($createdItem3.id)" -ForegroundColor Green

    $booking3 = @{
        itemId = $createdItem3.id
        start = (Get-Date).AddDays(5).ToString("yyyy-MM-ddTHH:mm:ss")
        end = (Get-Date).AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss")
    } | ConvertTo-Json -Compress

    $createdBooking3 = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method POST -Body $booking3 -ContentType "application/json" -Headers @{"X-Sharer-User-Id" = $createdUser2.id}
    Write-Host "[OK] Booking 3 created: id=$($createdBooking3.id), status=$($createdBooking3.status)" -ForegroundColor Green

    $rejectedBooking = Invoke-RestMethod -Uri "$baseUrl/bookings/$($createdBooking3.id)?approved=false" -Method PATCH -Headers @{"X-Sharer-User-Id" = $createdUser1.id}
    Write-Host "[OK] Booking rejected: status=$($rejectedBooking.status)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Error: $_" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TESTING COMPLETED" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

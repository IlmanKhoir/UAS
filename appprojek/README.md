# PHP + MySQL Backend (phpMyAdmin)

This folder contains a minimal backend to store:
- User emails (and login)
- Wishlist
- Order history

## 1) Import Database Schema

Use phpMyAdmin to import `db.sql` into your MySQL server.

Database name: `appprojek`

## 2) Configure Database Credentials

Copy `config.example.php` to `config.php` and set your MySQL credentials:

```php
<?php
// backend-php/config.php
return [
    'host' => 'localhost',
    'db' => 'appprojek',
    'user' => 'root',
    'pass' => '',
    'charset' => 'utf8mb4',
];
```

## 3) Deploy PHP Files

Place the contents of this folder in your web server root (e.g., `htdocs/appprojek/`).

Available endpoints (all return JSON):
- `users.php?action=register` (POST: email, password)
- `users.php?action=login` (POST: email, password)
- `users.php?action=me&email=...` (GET)
- `wishlist.php?action=list&user_id=...` (GET)
- `wishlist.php?action=add` (POST: user_id, product_id)
- `wishlist.php?action=remove` (POST: user_id, product_id)
- `orders.php?action=list&user_id=...` (GET)
- `orders.php?action=create` (POST: user_id, total_amount, status, shipping_address, payment_method, tracking_number, items_json)

Notes:
- `items_json` is a JSON array of items: `[{"product_id":"p1","qty":2,"price":15000}]`

## 4) Point Android App to Backend

In `ApiClient.kt`, set `BASE_URL` to your server URL, e.g. `http://192.168.1.10/appprojek`.

## 5) Quick Test (cURL)

```bash
curl -X POST -d "email=user@example.com&password=secret" http://localhost/appprojek/users.php?action=register
curl -X POST -d "email=user@example.com&password=secret" http://localhost/appprojek/users.php?action=login
curl "http://localhost/appprojek/wishlist.php?action=list&user_id=1"
```



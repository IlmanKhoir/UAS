<?php
require __DIR__ . '/bootstrap.php';

$action = $_GET['action'] ?? '';

if ($action === 'list') {
    $userId = (int) ($_GET['user_id'] ?? 0);
    $stmt = $pdo->prepare('SELECT id, total_amount, status, order_date, shipping_address, payment_method, tracking_number, items_json FROM orders WHERE user_id = ? ORDER BY id DESC');
    $stmt->execute([$userId]);
    $rows = $stmt->fetchAll();
    foreach ($rows as &$r) {
        $decoded = json_decode($r['items_json'], true);
        $r['items'] = is_array($decoded) ? $decoded : [];
        unset($r['items_json']);
    }
    respond(['orders' => $rows]);
}

if ($action === 'create') {
    $userId = (int) ($_POST['user_id'] ?? 0);
    $totalAmount = (int) ($_POST['total_amount'] ?? 0);
    $status = $_POST['status'] ?? 'pending';
    $shippingAddress = $_POST['shipping_address'] ?? '';
    $paymentMethod = $_POST['payment_method'] ?? '';
    $trackingNumber = $_POST['tracking_number'] ?? null;
    $itemsJson = $_POST['items_json'] ?? '[]';
    if ($userId <= 0 || $totalAmount <= 0 || $itemsJson === '') {
        respond(['error' => 'Invalid input'], 400);
    }
    $stmt = $pdo->prepare('INSERT INTO orders (user_id, total_amount, status, shipping_address, payment_method, tracking_number, items_json) VALUES (?, ?, ?, ?, ?, ?, ?)');
    $stmt->execute([$userId, $totalAmount, $status, $shippingAddress, $paymentMethod, $trackingNumber, $itemsJson]);
    respond(['success' => true, 'order_id' => (int) $pdo->lastInsertId()]);
}

respond(['error' => 'Unknown action'], 400);

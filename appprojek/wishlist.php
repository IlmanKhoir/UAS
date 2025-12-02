<?php
require __DIR__ . '/bootstrap.php';

$action = $_GET['action'] ?? '';

if ($action === 'list') {
    $userId = (int) ($_GET['user_id'] ?? 0);
    $stmt = $pdo->prepare('SELECT product_id, created_at FROM wishlist WHERE user_id = ? ORDER BY created_at DESC');
    $stmt->execute([$userId]);
    respond(['items' => $stmt->fetchAll()]);
}

if ($action === 'add') {
    $userId = (int) ($_POST['user_id'] ?? 0);
    $productId = $_POST['product_id'] ?? '';
    if ($userId <= 0 || $productId === '')
        respond(['error' => 'Invalid input'], 400);
    $stmt = $pdo->prepare('INSERT IGNORE INTO wishlist (user_id, product_id) VALUES (?, ?)');
    $stmt->execute([$userId, $productId]);
    respond(['success' => true]);
}

if ($action === 'remove') {
    $userId = (int) ($_POST['user_id'] ?? 0);
    $productId = $_POST['product_id'] ?? '';
    $stmt = $pdo->prepare('DELETE FROM wishlist WHERE user_id = ? AND product_id = ?');
    $stmt->execute([$userId, $productId]);
    respond(['success' => true]);
}

respond(['error' => 'Unknown action'], 400);

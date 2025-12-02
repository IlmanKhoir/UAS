
<?php
require __DIR__ . '/bootstrap.php';

$action = $_GET['action'] ?? '';

if ($action === 'register') {
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';
    if (!filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($password) < 6) {
        respond(['error' => 'Invalid email or password'], 400);
    }
    $stmt = $pdo->prepare('SELECT id FROM users WHERE email = ?');
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        respond(['error' => 'Email already registered'], 409);
    }
    $phone = $_POST['phone']
        ?? $_POST['nohp']
        ?? $_POST['telepon']
        ?? $_POST['phone_number']
        ?? $_POST['no_telp']
        ?? $_POST['telp']
        ?? $_POST['hp']
        ?? null;

    $address = $_POST['address']
        ?? $_POST['alamat']
        ?? $_POST['alamat_lengkap']
        ?? null;

    $name = $_POST['name']
        ?? $_POST['nama']
        ?? $_POST['full_name']
        ?? null;

    $hash = password_hash($password, PASSWORD_BCRYPT);

    $stmt = $pdo->prepare('INSERT INTO users (email, password_hash, name, phone, address) VALUES (?, ?, ?, ?, ?)');
    $stmt->execute([$email, $hash, $name, $phone, $address]);

    respond([
        'success' => true,
        'user_id' => (int) $pdo->lastInsertId(),
        'email' => $email,
        'name' => $name,
        'phone' => $phone,
        'address' => $address,
    ]);
}

if ($action === 'login') {
    $email = $_POST['email'] ?? '';
    $password = $_POST['password'] ?? '';
    $stmt = $pdo->prepare('SELECT id, password_hash, name, phone, address FROM users WHERE email = ?');
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    if (!$user || !password_verify($password, $user['password_hash'])) {
        respond(['error' => 'Invalid credentials'], 401);
    }
    respond([
        'success' => true,
        'user_id' => (int) $user['id'],
        'email' => $email,
        'name' => $user['name'] ?? null,
        'phone' => $user['phone'] ?? null,
        'address' => $user['address'] ?? null,
    ]);
}

if ($action === 'me') {
    $email = $_GET['email'] ?? '';
    $stmt = $pdo->prepare('SELECT id, email, name, phone, address, created_at FROM users WHERE email = ?');
    $stmt->execute([$email]);
    $user = $stmt->fetch();
    if (!$user)
        respond(['error' => 'Not found'], 404);
    respond([
        'success' => true,
        'user_id' => (int) $user['id'],
        'email' => $user['email'],
        'name' => $user['name'] ?? null,
        'phone' => $user['phone'] ?? null,
        'address' => $user['address'] ?? null,
        'created_at' => $user['created_at'],
    ]);
}

if ($action === 'update') {
    $user_id = $_POST['user_id'] ?? null;
    $email = $_POST['email'] ?? null;

    if (!$user_id && !$email) {
        respond(['error' => 'user_id or email required'], 400);
    }

    $name = $_POST['name'] ?? $_POST['nama'] ?? $_POST['full_name'] ?? null;
    $phone = $_POST['phone'] ?? $_POST['nohp'] ?? $_POST['telepon'] ?? $_POST['phone_number'] ?? $_POST['no_telp'] ?? $_POST['telp'] ?? $_POST['hp'] ?? null;
    $address = $_POST['address'] ?? $_POST['alamat'] ?? $_POST['alamat_lengkap'] ?? null;
    // New: accept latitude and longitude if provided
    $latitude = isset($_POST['latitude']) ? $_POST['latitude'] : null;
    $longitude = isset($_POST['longitude']) ? $_POST['longitude'] : null;

    $fields = [];
    $params = [];
    if ($name !== null) {
        $fields[] = 'name = ?';
        $params[] = $name;
    }
    if ($phone !== null) {
        $fields[] = 'phone = ?';
        $params[] = $phone;
    }
    if ($address !== null) {
        $fields[] = 'address = ?';
        $params[] = $address;
    }
    if ($latitude !== null) {
        $fields[] = 'latitude = ?';
        $params[] = $latitude;
    }
    if ($longitude !== null) {
        $fields[] = 'longitude = ?';
        $params[] = $longitude;
    }

    if (empty($fields)) {
        respond(['error' => 'No fields to update'], 400);
    }

    $where = '';
    if ($user_id) {
        $where = 'id = ?';
        $params[] = $user_id;
    } else {
        $where = 'email = ?';
        $params[] = $email;
    }

    $sql = 'UPDATE users SET ' . implode(', ', $fields) . ' WHERE ' . $where . ' LIMIT 1';
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);

    respond(['success' => true]);
}

respond(['error' => 'Unknown action'], 400);
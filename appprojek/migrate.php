<?php
require __DIR__ . '/bootstrap.php';

echo "<h2>Starting Database Migration...</h2>";

try {
    $sql = file_get_contents(__DIR__ . '/appprojek.sql');
    
    // Split SQL by semicolon to execute multiple statements if needed, 
    // but PDO::exec can sometimes handle multiple statements depending on driver.
    // For safety with simple dumps, let's try executing the whole block or splitting.
    // appprojek.sql contains transaction commands, so executing as one block is often better if driver allows.
    
    $pdo->exec($sql);
    
    echo "<h3 style='color: green;'>Migration Successful! Tables created.</h3>";
    echo "<p>You can now delete this file or ignore it.</p>";
    
} catch (PDOException $e) {
    echo "<h3 style='color: red;'>Migration Failed:</h3>";
    echo "<pre>" . $e->getMessage() . "</pre>";
}

<?php
// update-stats.php
// Server-side script to update statistics in stats.json file

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit;
}

// Get the posted data
$input = json_decode(file_get_contents('php://input'), true);

if (!$input) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid JSON input']);
    exit;
}

// Load existing stats
$statsFile = 'stats.json';
$existingData = [];

if (file_exists($statsFile)) {
    $existingData = json_decode(file_get_contents($statsFile), true);
    if (!$existingData) {
        $existingData = [
            'pageViews' => [],
            'downloadClicks' => [],
            'lastUpdated' => null
        ];
    }
} else {
    $existingData = [
        'pageViews' => [],
        'downloadClicks' => [],
        'lastUpdated' => null
    ];
}

// Update the stats with new values
foreach ($input['pageViews'] as $page => $count) {
    if (isset($existingData['pageViews'][$page])) {
        $existingData['pageViews'][$page] = max($existingData['pageViews'][$page], $count);
    } else {
        $existingData['pageViews'][$page] = $count;
    }
}

foreach ($input['downloadClicks'] as $page => $count) {
    if (isset($existingData['downloadClicks'][$page])) {
        $existingData['downloadClicks'][$page] = max($existingData['downloadClicks'][$page], $count);
    } else {
        $existingData['downloadClicks'][$page] = $count;
    }
}

$existingData['lastUpdated'] = $input['lastUpdated'];

// Save updated stats
if (file_put_contents($statsFile, json_encode($existingData, JSON_PRETTY_PRINT))) {
    echo json_encode(['success' => true, 'message' => 'Statistics updated successfully']);
} else {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Failed to update statistics']);
}
?>
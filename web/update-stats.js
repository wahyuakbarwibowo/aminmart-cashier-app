// update-stats.js
// Node.js server-side script to update statistics in stats.json file

const fs = require('fs');
const path = require('path');

// Handle POST request to update stats
function updateStats(req, res) {
    // Set CORS headers
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'POST');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method !== 'POST') {
        res.statusCode = 405;
        res.end(JSON.stringify({ error: 'Method not allowed' }));
        return;
    }

    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });

    req.on('end', () => {
        try {
            const inputData = JSON.parse(body);

            // Load existing stats
            const statsFilePath = path.join(__dirname, 'stats.json');
            let existingData = {
                pageViews: {},
                downloadClicks: {},
                lastUpdated: null
            };

            if (fs.existsSync(statsFilePath)) {
                const fileContent = fs.readFileSync(statsFilePath, 'utf8');
                existingData = JSON.parse(fileContent);
            }

            // Update the stats with new values
            for (const [page, count] of Object.entries(inputData.pageViews)) {
                if (existingData.pageViews[page]) {
                    existingData.pageViews[page] = Math.max(existingData.pageViews[page], count);
                } else {
                    existingData.pageViews[page] = count;
                }
            }

            for (const [page, count] of Object.entries(inputData.downloadClicks)) {
                if (existingData.downloadClicks[page]) {
                    existingData.downloadClicks[page] = Math.max(existingData.downloadClicks[page], count);
                } else {
                    existingData.downloadClicks[page] = count;
                }
            }

            existingData.lastUpdated = inputData.lastUpdated;

            // Save updated stats
            fs.writeFileSync(statsFilePath, JSON.stringify(existingData, null, 2));

            res.statusCode = 200;
            res.end(JSON.stringify({ success: true, message: 'Statistics updated successfully' }));
        } catch (error) {
            console.error('Error updating stats:', error);
            res.statusCode = 500;
            res.end(JSON.stringify({ success: false, message: 'Failed to update statistics' }));
        }
    });
}

module.exports = { updateStats };
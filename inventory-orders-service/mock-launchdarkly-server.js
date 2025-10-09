const http = require('http');
const url = require('url');

// Simple in-memory store for flags
const flags = {};

const server = http.createServer((req, res) => {
    const parsedUrl = url.parse(req.url, true);
    const path = parsedUrl.pathname;
    const method = req.method;

    // Enable CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }

    console.log(`${method} ${path}`);

    // Health check endpoint
    if (path === '/status') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ status: 'ok' }));
        return;
    }

    // Create flag endpoint: POST /api/v2/flags/{projectKey}
    if (method === 'POST' && path.match(/^\/api\/v2\/flags\/([^\/]+)$/)) {
        const projectKey = path.match(/^\/api\/v2\/flags\/([^\/]+)$/)[1];

        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });

        req.on('end', () => {
            try {
                const flagData = JSON.parse(body);
                const flagKey = flagData.key;

                // Store the flag
                if (!flags[projectKey]) {
                    flags[projectKey] = {};
                }
                flags[projectKey][flagKey] = {
                    ...flagData,
                    _version: 1,
                    archived: false
                };

                console.log(`Created flag: ${flagKey} in project: ${projectKey}`);

                res.writeHead(201, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify(flags[projectKey][flagKey]));
            } catch (error) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: 'Invalid JSON' }));
            }
        });
        return;
    }

    // Get flag endpoint: GET /api/v2/flags/{projectKey}/{flagKey}
    if (method === 'GET' && path.match(/^\/api\/v2\/flags\/([^\/]+)\/([^\/]+)$/)) {
        const matches = path.match(/^\/api\/v2\/flags\/([^\/]+)\/([^\/]+)$/);
        const projectKey = matches[1];
        const flagKey = matches[2];

        if (flags[projectKey] && flags[projectKey][flagKey]) {
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify(flags[projectKey][flagKey]));
        } else {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'Flag not found' }));
        }
        return;
    }

    // Delete flag endpoint: DELETE /api/v2/flags/{projectKey}/{flagKey}
    if (method === 'DELETE' && path.match(/^\/api\/v2\/flags\/([^\/]+)\/([^\/]+)$/)) {
        const matches = path.match(/^\/api\/v2\/flags\/([^\/]+)\/([^\/]+)$/);
        const projectKey = matches[1];
        const flagKey = matches[2];

        if (flags[projectKey] && flags[projectKey][flagKey]) {
            delete flags[projectKey][flagKey];
            console.log(`Deleted flag: ${flagKey} from project: ${projectKey}`);
            res.writeHead(204);
            res.end();
        } else {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'Flag not found' }));
        }
        return;
    }

    // Archive flag endpoint: POST /api/v2/flags/{projectKey}/{flagKey}/archive
    if (method === 'POST' && path.match(/^\/api\/v2\/flags\/([^\/]+)\/([^\/]+)\/archive$/)) {
        const matches = path.match(/^\/api\/v2\/flags\/([^\/]+)\/([^\/]+)\/archive$/);
        const projectKey = matches[1];
        const flagKey = matches[2];

        if (flags[projectKey] && flags[projectKey][flagKey]) {
            flags[projectKey][flagKey].archived = true;
            console.log(`Archived flag: ${flagKey} in project: ${projectKey}`);
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify(flags[projectKey][flagKey]));
        } else {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'Flag not found' }));
        }
        return;
    }

    // Default 404
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Not found' }));
});

const PORT = process.env.PORT || 8765;
server.listen(PORT, () => {
    console.log(`Mock LaunchDarkly Management API server running on port ${PORT}`);
    console.log('Supported endpoints:');
    console.log('  GET  /status');
    console.log('  POST /api/v2/flags/{projectKey}');
    console.log('  GET  /api/v2/flags/{projectKey}/{flagKey}');
    console.log('  DELETE /api/v2/flags/{projectKey}/{flagKey}');
    console.log('  POST /api/v2/flags/{projectKey}/{flagKey}/archive');
});
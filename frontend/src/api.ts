export async function startScan(domain: string) {
    const response = await fetch('/api/scan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ domain }),
    });

    if (!response.ok) {
        throw new Error('Failed to start scan');
    }

    return response.json();
}

export async function fetchScans() {
    const response = await fetch('/api/scans');

    if (!response.ok) {
        throw new Error('Failed to fetch scans');
    }

    return response.json();
}

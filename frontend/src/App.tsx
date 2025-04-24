import React, { useEffect, useState, useRef } from 'react';
import { fetchScans } from './api';
import ScanModal from './components/ScanModal';
import ResultCard from './components/ResultCard';
import { ScanResponseDto } from './types';

const App: React.FC = () => {
    const [scans, setScans] = useState<ScanResponseDto[]>([]);
    const [refresh, setRefresh] = useState(false);
    const hasFetched = useRef(false);

    useEffect(() => {
        if (hasFetched.current) return;
        hasFetched.current = true;

        fetchScans()
            .then(setScans)
            .catch(err => console.error('Error fetching scans:', err));
    }, [refresh]);

    return (
        <div className="min-h-screen p-4 bg-gray-100">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Domain Scan Dashboard</h1>
                <ScanModal onScanSuccess={() => setRefresh(r => !r)} />
            </div>
            <div className="grid gap-4">
                {scans.map(scan => (
                    <ResultCard key={scan.id} scan={scan} />
                ))}
            </div>
        </div>
    );
};

export default App;

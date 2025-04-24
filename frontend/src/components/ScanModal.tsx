import React, { useState } from 'react';
import { startScan } from '../api';

interface ScanModalProps {
    onScanSuccess: () => void;
}

const ScanModal: React.FC<ScanModalProps> = ({ onScanSuccess }) => {
    const [domain, setDomain] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const handleScan = async () => {
        setLoading(true);
        setError(null);
        try {
            await startScan(domain);
            onScanSuccess();
            setDomain('');
            setIsModalOpen(false);
        } catch (e) {
            setError('Scan failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="relative">
            <button
                className="px-4 py-2 bg-blue-500 text-white rounded"
                onClick={() => setIsModalOpen(true)}
            >
                Start Scan
            </button>

            {}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                    <div className="bg-white p-6 rounded shadow-md w-96">
                        <h3 className="text-xl mb-4">Enter Domain to Scan</h3>
                        <input
                            type="text"
                            className="border p-2 w-full mb-4"
                            value={domain}
                            onChange={(e) => setDomain(e.target.value)}
                            placeholder="Enter domain"
                        />
                        {error && <p className="text-red-500 text-sm">{error}</p>}
                        <button
                            className={`px-4 py-2 bg-green-500 text-white rounded ${loading ? 'opacity-50' : ''}`}
                            onClick={handleScan}
                            disabled={loading}
                        >
                            {loading ? 'Scanning...' : 'Start Scan'}
                        </button>
                        <button
                            className="mt-2 px-4 py-2 bg-gray-500 text-white rounded"
                            onClick={() => setIsModalOpen(false)}
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ScanModal;

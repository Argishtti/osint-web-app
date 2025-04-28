import React from 'react';
import { ScanResponseDto, ScanResultResponseDto } from '../types';

interface SubdomainModalProps {
    scan: ScanResponseDto;
    onClose: () => void;
}

const SubdomainModal: React.FC<SubdomainModalProps> = ({ scan, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4">
            <div className="bg-white p-6 rounded shadow-md w-full max-w-4xl max-h-[90vh] overflow-y-auto">
                <h3 className="text-2xl font-semibold mb-4">Subdomains for {scan.domain}</h3>
                <ul className="space-y-4">
                    {scan.result.length === 0 ? (
                        <li>No subdomains found.</li>
                    ) : (
                        scan.result.map((result: ScanResultResponseDto) => (
                            <li key={result.id} className="border-b pb-2">
                                <div className="font-mono break-words">{result.value}</div>
                                <p className="text-sm text-gray-600">Started at: {new Date(result.startedAt).toLocaleString()}</p>
                                <p className="text-sm text-gray-600">Finished at: {new Date(result.finishedAt).toLocaleString()}</p>
                            </li>
                        ))
                    )}
                </ul>
                <button
                    className="mt-6 px-6 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                    onClick={onClose}
                >
                    Close
                </button>
            </div>
        </div>
    );
};

export default SubdomainModal;

import React from 'react';
import { ScanResponseDto, ScanResultResponseDto } from '../types';

interface SubdomainModalProps {
    scan: ScanResponseDto;
    onClose: () => void;
}

const SubdomainModal: React.FC<SubdomainModalProps> = ({ scan, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <div className="bg-white p-6 rounded shadow-md w-96">
                <h3 className="text-xl font-semibold mb-4">Subdomains for {scan.domain}</h3>
                <ul>
                    {scan.result.length === 0 ? (
                        <li>No subdomains found.</li>
                    ) : (
                        scan.result.map((result: ScanResultResponseDto) => (
                            <li key={result.id}>
                                <div>{result.value}</div>
                                <p>Started at: {new Date(result.startedAt).toLocaleString()}</p>
                                <p>Finished at: {new Date(result.finishedAt).toLocaleString()}</p>
                            </li>
                        ))
                    )}
                </ul>
                <button
                    className="mt-4 px-4 py-2 bg-red-500 text-white rounded"
                    onClick={onClose}
                >
                    Close
                </button>
            </div>
        </div>
    );
};

export default SubdomainModal;

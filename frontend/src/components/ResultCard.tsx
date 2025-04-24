import React, { useState } from 'react';
import { ScanResponseDto, ScanResultResponseDto } from '../types';
import SubdomainModal from './SubdomainModal';

interface ResultCardProps {
    scan: ScanResponseDto;
}

const ResultCard: React.FC<ResultCardProps> = ({ scan }) => {
    const [showSubdomains, setShowSubdomains] = useState(false);

    return (
        <div className="bg-white p-4 shadow-md rounded-lg">
            <h3 className="text-lg font-semibold">{scan.domain}</h3>
            <p>Status: {scan.result.length > 0 ? 'Completed' : 'No Results'}</p>
            <button
                className="mt-4 px-4 py-2 bg-blue-500 text-white rounded"
                onClick={() => setShowSubdomains(true)}
                disabled={scan.result.length === 0}
            >
                View Subdomains
            </button>
            {showSubdomains && (
                <SubdomainModal
                    scan={scan}
                    onClose={() => setShowSubdomains(false)}
                />
            )}
        </div>
    );
};

export default ResultCard;

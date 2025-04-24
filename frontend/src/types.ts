export interface ScanResultResponseDto {
    id: string;
    value: string;
    startedAt: string;
    finishedAt: string;
}

export interface ScanResponseDto {
    id: string;
    domain: string;
    result: ScanResultResponseDto[];
}

export const mockEndpoints = [
  {
    method: 'POST',
    path: '/api/statements/upload',
    description:
      'Upload a CSV bank statement. Parses rows according to the specified bank format (ALPHA or BETA), skips duplicate reference numbers, and persists new transactions.',
    params: 'file (multipart), bank (ALPHA | BETA)',
    response: 'IngestionResult { savedCount, duplicateCount }',
  },
  {
    method: 'POST',
    path: '/api/reconciliation/run',
    description:
      'Trigger the full reconciliation engine over all persisted transactions. Detects duplicates and cross-bank matches within a configurable date-tolerance window.',
    params: 'daysTolerance (query param, default: 1)',
    response: 'ReconciliationSummary { matched, unmatched, duplicates, total }',
  },
  {
    method: 'GET',
    path: '/api/reconciliation/matched',
    description:
      'Retrieve all transactions that were successfully matched across bank sources.',
    params: '—',
    response: 'List<Transaction>',
  },
  {
    method: 'GET',
    path: '/api/reconciliation/unmatched',
    description:
      'Retrieve all transactions that could not be matched and are not marked as duplicates.',
    params: '—',
    response: 'List<Transaction>',
  },
  {
    method: 'GET',
    path: '/api/reconciliation/duplicates',
    description:
      'Retrieve all transactions identified as duplicates — same amount, date, and reference number appearing more than once.',
    params: '—',
    response: 'List<Transaction>',
  },
  {
    method: 'GET',
    path: '/api/transactions/{accountId}',
    description:
      'Retrieve all normalized transactions associated with a specific account ID.',
    params: 'accountId (path variable, e.g. ACC001)',
    response: 'List<Transaction>',
  },
]

import {
  Upload,
  SlidersHorizontal,
  CopyX,
  GitMerge,
  AlertTriangle,
  CheckSquare,
} from 'lucide-react'

export const pipelineSteps = [
  {
    step: 1,
    icon: Upload,
    title: 'Data Input',
    subtitle: 'Ingest CSV Bank Statements',
    description:
      'Upload multi-bank CSV statements via the REST API. The engine accepts two bank formats — ALPHA and BETA — each with different column layouts and date conventions. Files are streamed and validated on receipt.',
    detail: 'POST /api/statements/upload',
    accent: 'bg-indigo-600',
  },
  {
    step: 2,
    icon: SlidersHorizontal,
    title: 'Normalization',
    subtitle: 'Unified Transaction Schema',
    description:
      "Each raw row is parsed by a bank-specific parser and mapped to a canonical Transaction entity with consistent date formatting (ISO-8601), decimal amounts, trimmed references, and a normalized account ID.",
    detail: 'BankStatementParserFactory → ALPHA / BETA Parser',
    accent: 'bg-violet-600',
  },
  {
    step: 3,
    icon: CopyX,
    title: 'Duplicate Detection',
    subtitle: 'Within-Bank Deduplication',
    description:
      "Before persisting, each transaction's reference number is checked against existing records for the same bank source. Rows with duplicate reference numbers are counted and skipped, never written to the database.",
    detail: 'Keyed by (referenceNumber, sourceBank)',
    accent: 'bg-rose-500',
  },
  {
    step: 4,
    icon: GitMerge,
    title: 'Cross-Bank Matching',
    subtitle: 'Tolerance-Based Reconciliation',
    description:
      'The reconciliation engine groups transactions across banks by amount and compares dates within a configurable tolerance window (default ±1 day). Matching pairs are linked and marked MATCHED in the database.',
    detail: 'POST /api/reconciliation/run?daysTolerance=1',
    accent: 'bg-emerald-600',
  },
  {
    step: 5,
    icon: AlertTriangle,
    title: 'Discrepancy Flagging',
    subtitle: 'Unmatched Transaction Review',
    description:
      'Any transaction that survives duplicate detection but fails to find a cross-bank match is flagged as UNMATCHED. These records are surfaced for manual review via a dedicated query endpoint.',
    detail: 'GET /api/reconciliation/unmatched',
    accent: 'bg-amber-500',
  },
  {
    step: 6,
    icon: CheckSquare,
    title: 'Output & Query',
    subtitle: 'Classified Results via REST',
    description:
      'All reconciled transactions are classified into three buckets — MATCHED, UNMATCHED, and DUPLICATE — and queryable via dedicated GET endpoints. Account-level drill-down is also available by account ID.',
    detail: 'GET /api/reconciliation/matched | unmatched | duplicates',
    accent: 'bg-sky-600',
  },
]

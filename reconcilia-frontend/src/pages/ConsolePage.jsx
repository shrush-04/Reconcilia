import { useState, useEffect, useCallback } from 'react'
import { motion } from 'framer-motion'
import {
  Upload,
  Play,
  CheckCircle,
  AlertCircle,
  RefreshCw,
  FileSpreadsheet,
  Layers,
  Search,
} from 'lucide-react'
import { API_BASE_URL } from '../config'
import Badge from '../components/Badge'

export default function ConsolePage() {
  // Upload State
  const [bankFormat, setBankFormat] = useState('ALPHA')
  const [selectedFile, setSelectedFile] = useState(null)
  const [uploadLoading, setUploadLoading] = useState(false)
  const [uploadResult, setUploadResult] = useState(null)
  const [uploadError, setUploadError] = useState(null)

  // Reconciliation Run State
  const [reconLoading, setReconLoading] = useState(false)
  const [reconResult, setReconResult] = useState(null)
  const [reconError, setReconError] = useState(null)

  // Results State
  const [activeTab, setActiveTab] = useState('matched') // matched, unmatched, duplicates
  const [transactions, setTransactions] = useState([])
  const [transactionsLoading, setTransactionsLoading] = useState(false)
  const [transactionsError, setTransactionsError] = useState(null)
  const [searchQuery, setSearchQuery] = useState('')

  // Fetch transactions for the active tab
  const fetchTransactions = useCallback(async (tab) => {
    setTransactionsLoading(true)
    setTransactionsError(null)
    try {
      const response = await fetch(`${API_BASE_URL}/api/reconciliation/${tab}`)
      if (!response.ok) {
        throw new Error(`Failed to fetch ${tab} transactions (Status: ${response.status})`)
      }
      const data = await response.json()
      setTransactions(data)
    } catch (err) {
      console.error(err)
      setTransactionsError(err.message || 'Failed to connect to the backend server.')
    } finally {
      setTransactionsLoading(false)
    }
  }, [])

  // Load transactions when active tab changes
  useEffect(() => {
    fetchTransactions(activeTab)
  }, [activeTab, fetchTransactions])

  // Handle File Select
  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      setSelectedFile(e.target.files[0])
      setUploadResult(null)
      setUploadError(null)
    }
  }

  // Handle Upload
  const handleUpload = async (e) => {
    e.preventDefault()
    if (!selectedFile) {
      setUploadError('Please select a CSV file to upload.')
      return
    }

    setUploadLoading(true)
    setUploadError(null)
    setUploadResult(null)

    const formData = new FormData()
    formData.append('file', selectedFile)
    formData.append('bank', bankFormat)

    try {
      const response = await fetch(`${API_BASE_URL}/api/statements/upload`, {
        method: 'POST',
        body: formData,
      })

      if (!response.ok) {
        // Try to parse error response if it exists
        let errorMsg = `Upload failed (Status: ${response.status})`
        try {
          const errData = await response.json()
          if (errData && errData.message) {
            errorMsg = errData.message
          }
        } catch (_) {}
        throw new Error(errorMsg)
      }

      const result = await response.json()
      setUploadResult(result)
      setSelectedFile(null)
      // Reset file input element
      const fileInput = document.getElementById('csv-file-input')
      if (fileInput) fileInput.value = ''
    } catch (err) {
      console.error(err)
      setUploadError(err.message || 'Failed to connect to the backend server.')
    } finally {
      setUploadLoading(false)
    }
  }

  // Handle Run Reconciliation
  const handleRunReconciliation = async () => {
    setReconLoading(true)
    setReconError(null)
    setReconResult(null)

    try {
      const response = await fetch(`${API_BASE_URL}/api/reconciliation/run`, {
        method: 'POST',
      })

      if (!response.ok) {
        throw new Error(`Reconciliation run failed (Status: ${response.status})`)
      }

      const result = await response.json()
      setReconResult(result)
      // Refresh current tab results
      fetchTransactions(activeTab)
    } catch (err) {
      console.error(err)
      setReconError(err.message || 'Failed to connect to the backend server.')
    } finally {
      setReconLoading(false)
    }
  }

  // Filtered transactions by search query
  const filteredTransactions = transactions.filter((t) => {
    const query = searchQuery.toLowerCase()
    return (
      (t.accountId && t.accountId.toLowerCase().includes(query)) ||
      (t.description && t.description.toLowerCase().includes(query)) ||
      (t.referenceNumber && t.referenceNumber.toLowerCase().includes(query)) ||
      (t.sourceBank && t.sourceBank.toLowerCase().includes(query))
    );
  })

  return (
    <div className="pt-16 min-h-screen bg-gray-50/50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        
        {/* Header */}
        <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Reconcilia Console</h1>
            <p className="text-sm text-gray-500 mt-1">
              Ingest bank statements, trigger the matching pipeline, and review reconciled transactions.
            </p>
          </div>
          <button
            onClick={handleRunReconciliation}
            disabled={reconLoading}
            className="inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white text-sm font-semibold rounded-xl shadow-sm hover:shadow active:scale-98 transition-all duration-150 shrink-0"
          >
            {reconLoading ? (
              <RefreshCw className="w-4 h-4 animate-spin" />
            ) : (
              <Play className="w-4 h-4" />
            )}
            Run Reconciliation
          </button>
        </div>

        {/* Top Section: Upload & Run Status */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          
          {/* Card 1: CSV Ingestion */}
          <div className="bg-white rounded-2xl border border-gray-200/80 shadow-sm p-6 lg:col-span-2">
            <div className="flex items-center gap-2 mb-4">
              <FileSpreadsheet className="w-5 h-5 text-indigo-600" />
              <h2 className="text-lg font-bold text-gray-900">Ingest Bank Statement</h2>
            </div>
            
            <form onSubmit={handleUpload} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {/* Bank Format Selector */}
                <div>
                  <label htmlFor="bank-format" className="block text-xs font-semibold text-gray-500 uppercase mb-1.5">
                    Bank Format
                  </label>
                  <select
                    id="bank-format"
                    value={bankFormat}
                    onChange={(e) => setBankFormat(e.target.value)}
                    className="w-full rounded-xl border border-gray-200 bg-white px-3.5 py-2 text-sm text-gray-700 shadow-sm focus:border-indigo-500 focus:outline-none transition-colors"
                  >
                    <option value="ALPHA">ALPHA Bank Format</option>
                    <option value="BETA">BETA Bank Format</option>
                  </select>
                </div>

                {/* File Input */}
                <div>
                  <label htmlFor="csv-file-input" className="block text-xs font-semibold text-gray-500 uppercase mb-1.5">
                    Select CSV File
                  </label>
                  <input
                    type="file"
                    id="csv-file-input"
                    accept=".csv"
                    onChange={handleFileChange}
                    className="w-full rounded-xl border border-gray-200 bg-white px-3.5 py-1.5 text-sm text-gray-500 shadow-sm file:mr-3 file:py-1 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 cursor-pointer focus:outline-none"
                  />
                </div>
              </div>

              <div className="flex items-center justify-between gap-4 pt-2">
                <button
                  type="submit"
                  disabled={uploadLoading || !selectedFile}
                  className="inline-flex items-center gap-2 px-4 py-2 bg-gray-900 hover:bg-gray-800 disabled:bg-gray-300 text-white text-xs font-bold rounded-lg shadow-sm active:scale-98 transition-all duration-150"
                >
                  {uploadLoading ? (
                    <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  ) : (
                    <Upload className="w-3.5 h-3.5" />
                  )}
                  Upload Statement
                </button>

                {selectedFile && (
                  <span className="text-xs text-gray-500 truncate max-w-[200px] sm:max-w-xs">
                    Selected: {selectedFile.name}
                  </span>
                )}
              </div>
            </form>

            {/* Ingestion Response Messages */}
            {uploadResult && (
              <motion.div
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                className="mt-4 p-4 rounded-xl bg-emerald-50 border border-emerald-100 flex items-start gap-3"
              >
                <CheckCircle className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm font-bold text-emerald-900">Ingestion Successful</p>
                  <p className="text-xs text-emerald-700 mt-0.5">
                    Saved Transactions: <span className="font-semibold">{uploadResult.savedCount}</span> · Duplicates Skipped: <span className="font-semibold">{uploadResult.duplicateCount}</span>
                  </p>
                </div>
              </motion.div>
            )}

            {uploadError && (
              <motion.div
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                className="mt-4 p-4 rounded-xl bg-rose-50 border border-rose-100 flex items-start gap-3"
              >
                <AlertCircle className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm font-bold text-rose-900">Ingestion Error</p>
                  <p className="text-xs text-rose-700 mt-0.5">{uploadError}</p>
                </div>
              </motion.div>
            )}
          </div>

          {/* Card 2: Run Status */}
          <div className="bg-white rounded-2xl border border-gray-200/80 shadow-sm p-6 flex flex-col justify-between">
            <div>
              <div className="flex items-center gap-2 mb-4">
                <Layers className="w-5 h-5 text-indigo-600" />
                <h2 className="text-lg font-bold text-gray-900">Pipeline Status</h2>
              </div>
              <p className="text-xs text-gray-500 leading-relaxed mb-4">
                Run the reconciliation engine to process matching logic across all ingested records.
              </p>
            </div>

            <div>
              {reconResult ? (
                <motion.div
                  initial={{ opacity: 0, scale: 0.95 }}
                  animate={{ opacity: 1, scale: 1 }}
                  className="p-4 rounded-xl bg-indigo-50 border border-indigo-100"
                >
                  <p className="text-xs font-bold text-indigo-900 uppercase tracking-wider mb-2">Reconciliation Summary</p>
                  <div className="grid grid-cols-2 gap-2 text-xs text-indigo-950">
                    <div>Matched: <span className="font-bold">{reconResult.matched ?? 0}</span></div>
                    <div>Unmatched: <span className="font-bold">{reconResult.unmatched ?? 0}</span></div>
                    <div>Duplicates: <span className="font-bold">{reconResult.duplicates ?? 0}</span></div>
                    <div>Total Processed: <span className="font-bold">{reconResult.total ?? 0}</span></div>
                  </div>
                </motion.div>
              ) : reconError ? (
                <div className="p-4 rounded-xl bg-rose-50 border border-rose-100 text-xs text-rose-700">
                  <p className="font-bold text-rose-900">Run Failed</p>
                  <p className="mt-1">{reconError}</p>
                </div>
              ) : (
                <div className="p-4 rounded-xl bg-gray-50 border border-gray-100 text-center text-xs text-gray-400 font-medium">
                  No recent execution. Click "Run Reconciliation" to trigger matching.
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Bottom Section: Classified Transactions Table */}
        <div className="bg-white rounded-2xl border border-gray-200/80 shadow-sm overflow-hidden">
          
          {/* Tabs & Search Header */}
          <div className="border-b border-gray-200 bg-gray-50/50 px-6 py-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            
            {/* Tabs */}
            <div className="flex gap-1 bg-gray-200/60 p-1 rounded-xl w-fit">
              {[
                { id: 'matched', label: 'Matched', variant: 'green' },
                { id: 'unmatched', label: 'Unmatched', variant: 'amber' },
                { id: 'duplicates', label: 'Duplicates', variant: 'rose' },
              ].map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-4 py-1.5 rounded-lg text-xs font-bold transition-all duration-150 ${
                    activeTab === tab.id
                      ? 'bg-white text-gray-900 shadow-sm'
                      : 'text-gray-500 hover:text-gray-900'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </div>

            {/* Search Bar */}
            <div className="relative max-w-xs w-full">
              <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search transactions..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full rounded-xl border border-gray-200 bg-white pl-9 pr-4 py-2 text-xs shadow-sm focus:border-indigo-500 focus:outline-none transition-colors"
              />
            </div>
          </div>

          {/* Table Container */}
          <div className="overflow-x-auto">
            {transactionsLoading ? (
              <div className="py-20 flex flex-col items-center justify-center gap-3">
                <RefreshCw className="w-8 h-8 text-indigo-600 animate-spin" />
                <p className="text-xs text-gray-400 font-medium">Loading transactions...</p>
              </div>
            ) : transactionsError ? (
              <div className="py-16 px-6 text-center">
                <AlertCircle className="w-10 h-10 text-rose-500 mx-auto mb-3" />
                <p className="text-sm font-bold text-gray-800">Failed to Load Transactions</p>
                <p className="text-xs text-gray-500 mt-1 max-w-md mx-auto">{transactionsError}</p>
                <button
                  onClick={() => fetchTransactions(activeTab)}
                  className="mt-4 inline-flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-bold rounded-lg transition-colors"
                >
                  <RefreshCw className="w-3 h-3" />
                  Retry
                </button>
              </div>
            ) : filteredTransactions.length === 0 ? (
              <div className="py-20 text-center text-gray-400">
                <p className="text-sm font-medium">No transactions found.</p>
                <p className="text-xs mt-1">
                  {searchQuery ? 'Try adjusting your search filter.' : 'Upload statements and run reconciliation to view data.'}
                </p>
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-gray-50 border-b border-gray-100 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    <th className="px-6 py-4">Account ID</th>
                    <th className="px-6 py-4">Date</th>
                    <th className="px-6 py-4">Amount</th>
                    <th className="px-6 py-4">Description</th>
                    <th className="px-6 py-4">Reference No.</th>
                    <th className="px-6 py-4">Bank</th>
                    <th className="px-6 py-4">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 bg-white text-gray-700">
                  {filteredTransactions.map((t, index) => (
                    <motion.tr
                      key={t.id || index}
                      initial={{ opacity: 0, y: 4 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.2, delay: Math.min(index * 0.02, 0.2) }}
                      className="hover:bg-indigo-50/20 transition-colors"
                    >
                      <td className="px-6 py-4 whitespace-nowrap font-mono text-xs font-semibold text-gray-900">
                        {t.accountId}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-xs">
                        {t.date}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-xs font-bold text-gray-900">
                        ${t.amount?.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 text-xs max-w-xs truncate" title={t.description}>
                        {t.description}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap font-mono text-xs text-gray-500">
                        {t.referenceNumber}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge variant={t.sourceBank === 'ALPHA' ? 'default' : 'violet'}>
                          {t.sourceBank}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge
                          variant={
                            t.status === 'MATCHED'
                              ? 'green'
                              : t.status === 'UNMATCHED'
                              ? 'amber'
                              : 'rose'
                          }
                        >
                          {t.status}
                        </Badge>
                      </td>
                    </motion.tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

      </div>
    </div>
  )
}

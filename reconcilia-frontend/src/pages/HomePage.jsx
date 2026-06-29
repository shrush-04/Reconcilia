import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  ArrowRight,
  GitMerge,
  CopyX,
  AlertTriangle,
  CheckCircle2,
  Layers,
  Cpu,
  Database,
  FileText,
  Zap,
  Code2,
} from 'lucide-react'
import Badge from '../components/Badge'
import SectionHeading from '../components/SectionHeading'

const features = [
  {
    icon: GitMerge,
    title: 'Intelligent Cross-Bank Matching',
    description:
      'Automatically reconcile transactions across multiple bank sources using amount-based matching with a configurable date-tolerance window.',
    color: 'text-indigo-600',
    bg: 'bg-indigo-50',
  },
  {
    icon: CopyX,
    title: 'Duplicate Detection',
    description:
      'Hash-based deduplication identifies and skips duplicate records by reference number during ingestion — no silent data corruption.',
    color: 'text-rose-500',
    bg: 'bg-rose-50',
  },
  {
    icon: AlertTriangle,
    title: 'Discrepancy Flagging',
    description:
      'Transactions that fail reconciliation are classified as UNMATCHED and surfaced instantly via dedicated REST endpoints for review.',
    color: 'text-amber-500',
    bg: 'bg-amber-50',
  },
]

const techStack = [
  { label: 'Spring Boot 3', variant: 'default', icon: Zap },
  { label: 'Java 17', variant: 'gray', icon: Code2 },
  { label: 'PostgreSQL', variant: 'sky', icon: Database },
  { label: 'H2 (Test)', variant: 'gray', icon: Database },
  { label: 'CSV Parsing', variant: 'green', icon: FileText },
  { label: 'Flyway', variant: 'violet', icon: Layers },
  { label: 'REST API', variant: 'default', icon: Cpu },
  { label: 'Swagger UI', variant: 'green', icon: Code2 },
  { label: 'React 19', variant: 'sky', icon: Cpu },
  { label: 'Tailwind CSS v4', variant: 'violet', icon: Layers },
]

const stats = [
  { value: '2', label: 'Bank Formats Supported' },
  { value: '6', label: 'REST Endpoints' },
  { value: '3', label: 'Transaction States' },
  { value: '±1d', label: 'Default Date Tolerance' },
]

export default function HomePage() {
  return (
    <div className="pt-16">
      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-indigo-50 via-white to-violet-50 py-24 sm:py-32">
        <div
          className="absolute inset-0 opacity-[0.03]"
          style={{
            backgroundImage:
              'linear-gradient(#6366f1 1px, transparent 1px), linear-gradient(90deg, #6366f1 1px, transparent 1px)',
            backgroundSize: '40px 40px',
          }}
        />

        <div className="absolute top-0 right-1/4 w-96 h-96 bg-indigo-200 rounded-full opacity-20 blur-3xl -translate-y-1/2" />
        <div className="absolute bottom-0 left-1/4 w-72 h-72 bg-violet-200 rounded-full opacity-20 blur-3xl translate-y-1/2" />

        <div className="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, ease: [0.22, 1, 0.36, 1] }}
          >
            <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-indigo-100 border border-indigo-200 text-xs font-semibold text-indigo-700 mb-6">
              <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-pulse" />
              Multi-Bank Statement Reconciliation Engine
            </span>
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 28 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.65, delay: 0.08, ease: [0.22, 1, 0.36, 1] }}
            className="text-5xl sm:text-6xl lg:text-7xl font-black text-gray-900 tracking-tight leading-none mb-6"
          >
            Recon
            <span className="text-indigo-600">cilia</span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.65, delay: 0.16, ease: [0.22, 1, 0.36, 1] }}
            className="text-xl sm:text-2xl text-gray-500 font-medium max-w-2xl mx-auto mb-10 leading-relaxed"
          >
            Automated multi-bank statement reconciliation,{' '}
            <span className="text-gray-700 font-semibold">built for precision.</span>
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.24 }}
            className="flex flex-col sm:flex-row items-center justify-center gap-3"
          >
            <Link
              to="/how-it-works"
              className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white text-sm font-semibold rounded-xl hover:bg-indigo-700 active:scale-95 shadow-sm hover:shadow-md transition-all duration-200"
            >
              How It Works
              <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              to="/architecture"
              className="inline-flex items-center gap-2 px-6 py-3 bg-white text-gray-700 text-sm font-semibold rounded-xl border border-gray-200 hover:border-indigo-300 hover:text-indigo-600 hover:shadow-sm active:scale-95 transition-all duration-200"
            >
              View Architecture
            </Link>
          </motion.div>
        </div>
      </section>

      {/* Stats */}
      <section className="border-b border-gray-100 bg-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {stats.map(({ value, label }, i) => (
              <motion.div
                key={label}
                initial={{ opacity: 0, y: 16 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.45, delay: i * 0.08 }}
                className="text-center"
              >
                <p className="text-3xl sm:text-4xl font-black text-indigo-600 mb-1">{value}</p>
                <p className="text-sm text-gray-500 font-medium">{label}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="py-20 sm:py-28 bg-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-14">
            <SectionHeading
              label="Features"
              title="Built for reliable reconciliation"
              subtitle="A focused set of capabilities that cover the full reconciliation lifecycle — from ingestion through classification."
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {features.map(({ icon: Icon, title, description, color, bg }, i) => (
              <motion.div
                key={title}
                initial={{ opacity: 0, y: 24 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: '-50px' }}
                transition={{ duration: 0.5, delay: i * 0.1 }}
                className="group relative bg-white rounded-2xl border border-gray-100 p-7 shadow-sm hover:shadow-md hover:-translate-y-1 transition-all duration-300"
              >
                <div className={`w-11 h-11 rounded-xl ${bg} flex items-center justify-center mb-5`}>
                  <Icon className={`w-5 h-5 ${color}`} strokeWidth={2} />
                </div>
                <h3 className="text-base font-bold text-gray-900 mb-2">{title}</h3>
                <p className="text-sm text-gray-500 leading-relaxed">{description}</p>
                <div className="absolute bottom-0 inset-x-0 h-0.5 rounded-b-2xl bg-gradient-to-r from-indigo-500 to-violet-500 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Strip */}
      <section className="py-16 bg-gray-50 border-y border-gray-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-indigo-600 flex items-center justify-center shadow-sm shrink-0">
                <CheckCircle2 className="w-6 h-6 text-white" strokeWidth={2} />
              </div>
              <div>
                <h3 className="text-lg font-bold text-gray-900">See the full pipeline</h3>
                <p className="text-sm text-gray-500">6-step reconciliation process explained end-to-end.</p>
              </div>
            </div>
            <Link
              to="/how-it-works"
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 text-white text-sm font-semibold rounded-xl hover:bg-indigo-700 shadow-sm hover:shadow transition-all duration-200 shrink-0"
            >
              Explore Pipeline <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </section>

      {/* Tech Stack */}
      <section className="py-20 sm:py-24 bg-white">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-10">
            <SectionHeading
              label="Tech Stack"
              title="Built on proven foundations"
              subtitle="A carefully chosen set of technologies for reliability, testability, and extensibility."
            />
          </div>

          <motion.div
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="flex flex-wrap gap-3"
          >
            {techStack.map(({ label, variant }, i) => (
              <motion.div
                key={label}
                initial={{ opacity: 0, scale: 0.9 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                transition={{ duration: 0.35, delay: i * 0.045 }}
              >
                <Badge variant={variant}>{label}</Badge>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>
    </div>
  )
}

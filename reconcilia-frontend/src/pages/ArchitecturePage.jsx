import { motion } from 'framer-motion'
import { ArrowDown, ArrowRight, Database, FileText, Cpu, Server, GitMerge } from 'lucide-react'
import { mockEndpoints } from '../data/mockEndpoints'
import EndpointRow from '../components/EndpointRow'
import SectionHeading from '../components/SectionHeading'

// Diagram nodes
const diagramNodes = [
  {
    id: 'client',
    label: 'Client',
    sublabel: 'CSV upload / REST calls',
    icon: FileText,
    color: 'bg-indigo-600',
    textColor: 'text-white',
  },
  {
    id: 'ingestion',
    label: 'Ingestion Service',
    sublabel: 'Parse · Validate · Persist',
    icon: Cpu,
    color: 'bg-violet-600',
    textColor: 'text-white',
  },
  {
    id: 'database',
    label: 'PostgreSQL',
    sublabel: 'Transaction store',
    icon: Database,
    color: 'bg-sky-600',
    textColor: 'text-white',
  },
  {
    id: 'engine',
    label: 'Reconciliation Engine',
    sublabel: 'Duplicate · Match · Flag',
    icon: GitMerge,
    color: 'bg-emerald-600',
    textColor: 'text-white',
  },
  {
    id: 'api',
    label: 'REST API Layer',
    sublabel: 'Query results',
    icon: Server,
    color: 'bg-amber-500',
    textColor: 'text-white',
  },
]

function DiagramNode({ label, sublabel, icon: Icon, color, textColor, index }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true }}
      transition={{ duration: 0.45, delay: index * 0.09 }}
      className="flex flex-col items-center"
    >
      <div className={`w-full max-w-[180px] rounded-2xl ${color} shadow-sm px-5 py-4 text-center`}>
        <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center mx-auto mb-2">
          <Icon className={`w-4.5 h-4.5 ${textColor}`} strokeWidth={2} />
        </div>
        <p className={`text-sm font-bold ${textColor} leading-tight`}>{label}</p>
        <p className={`text-xs mt-0.5 ${textColor} opacity-75`}>{sublabel}</p>
      </div>
    </motion.div>
  )
}

function Arrow({ horizontal = false }) {
  return (
    <div className={`flex items-center justify-center ${horizontal ? 'flex-row' : 'flex-col'} shrink-0`}>
      {horizontal ? (
        <ArrowRight className="w-5 h-5 text-gray-300" />
      ) : (
        <ArrowDown className="w-5 h-5 text-gray-300" />
      )}
    </div>
  )
}

export default function ArchitecturePage() {
  return (
    <div className="pt-16">
      {/* Page header */}
      <section className="bg-gradient-to-b from-indigo-50 to-white py-16 sm:py-20 border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <SectionHeading
            center
            label="Architecture"
            title="System Design"
            subtitle="The end-to-end flow from CSV upload through persistence, reconciliation, and REST query endpoints."
          />
        </div>
      </section>

      {/* Diagram */}
      <section className="py-16 sm:py-24 bg-gray-50">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-lg font-bold text-gray-900 mb-8 text-center">Pipeline Overview</h2>

          {/* Desktop: horizontal flow */}
          <div className="hidden md:flex items-center justify-center gap-2 overflow-x-auto pb-4">
            {diagramNodes.map((node, i) => (
              <div key={node.id} className="flex items-center gap-2">
                <DiagramNode {...node} index={i} />
                {i < diagramNodes.length - 1 && <Arrow horizontal />}
              </div>
            ))}
          </div>

          {/* Mobile: vertical flow */}
          <div className="md:hidden flex flex-col items-center gap-3">
            {diagramNodes.map((node, i) => (
              <div key={node.id} className="flex flex-col items-center gap-3 w-full max-w-xs">
                <DiagramNode {...node} index={i} />
                {i < diagramNodes.length - 1 && <Arrow />}
              </div>
            ))}
          </div>

          {/* Legend */}
          <motion.div
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className="mt-10 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 max-w-3xl mx-auto"
          >
            {[
              { label: 'Spring Boot (Java 17)', desc: 'Application runtime & DI container' },
              { label: 'PostgreSQL / H2', desc: 'Prod & test database backends' },
              { label: 'Flyway', desc: 'Schema migration management' },
              { label: 'Pluggable Parsers', desc: 'ALPHA & BETA bank formats via factory pattern' },
              { label: 'Swagger / OpenAPI 3', desc: 'Interactive API docs at /swagger-ui.html' },
              { label: 'Spring Boot Test', desc: 'Integration tests with embedded H2' },
            ].map(({ label, desc }, i) => (
              <motion.div
                key={label}
                initial={{ opacity: 0, y: 10 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.35, delay: i * 0.06 }}
                className="flex gap-3 bg-white rounded-xl border border-gray-100 p-4 shadow-sm"
              >
                <span className="w-2 h-2 rounded-full bg-indigo-500 mt-1.5 shrink-0" />
                <div>
                  <p className="text-sm font-semibold text-gray-800">{label}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{desc}</p>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* REST API Reference */}
      <section className="py-16 sm:py-24 bg-white border-t border-gray-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-10">
            <SectionHeading
              label="REST API"
              title="Endpoint Reference"
              subtitle="All endpoints exposed by the Spring Boot backend. Served at localhost:8080 by default."
            />
          </div>

          <motion.div
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
            className="overflow-x-auto rounded-2xl border border-gray-100 shadow-sm"
          >
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="px-5 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">Method</th>
                  <th className="px-5 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Endpoint</th>
                  <th className="px-5 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">Description</th>
                  <th className="px-5 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider hidden lg:table-cell">Response</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50 bg-white">
                {mockEndpoints.map((ep, i) => (
                  <EndpointRow key={ep.path} {...ep} index={i} />
                ))}
              </tbody>
            </table>
          </motion.div>

          <motion.p
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.4, delay: 0.3 }}
            className="mt-4 text-xs text-gray-400 text-center"
          >
            Base URL: <code className="font-mono text-indigo-600">http://localhost:8080</code> · Full interactive docs at{' '}
            <code className="font-mono text-indigo-600">/swagger-ui.html</code>
          </motion.p>
        </div>
      </section>
    </div>
  )
}

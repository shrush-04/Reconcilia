import { motion } from 'framer-motion'
import { ExternalLink, Globe, Mail, GitMerge, Layers, Cpu, Database, FileCode2, Zap } from 'lucide-react'
import SectionHeading from '../components/SectionHeading'

const techDetails = [
  { label: 'Runtime', value: 'Java 17 + Spring Boot 3', icon: Zap },
  { label: 'Persistence', value: 'PostgreSQL (prod) / H2 (test)', icon: Database },
  { label: 'Migrations', value: 'Flyway', icon: Layers },
  { label: 'Parsing', value: 'Custom CSV parsers (ALPHA & BETA formats)', icon: FileCode2 },
  { label: 'API', value: 'Spring MVC REST + OpenAPI / Swagger UI', icon: Cpu },
  { label: 'Testing', value: 'Spring Boot Test, @DataJpaTest, @SpringBootTest', icon: FileCode2 },
  { label: 'Frontend', value: 'React 19 + Vite + Tailwind CSS v4', icon: Layers },
  { label: 'Routing', value: 'React Router v6', icon: Cpu },
]

const decisions = [
  {
    title: 'Pluggable Parser Architecture',
    description:
      'A BankStatementParserFactory dispatches to bank-specific parser implementations (ALPHA, BETA) based on an enum. Adding a new bank format requires only a new Parser class and a factory entry — zero changes to the ingestion service.',
  },
  {
    title: 'Tolerance-Based Date Matching',
    description:
      'Cross-bank transaction matching uses a configurable daysTolerance window (default ±1 day) rather than exact-date equality. This accommodates common value-date vs. processing-date discrepancies between banks.',
  },
  {
    title: 'H2 / PostgreSQL Dual-Profile Support',
    description:
      'The application uses Flyway migrations for schema management, making it compatible with both H2 (embedded, for integration tests) and PostgreSQL (production), with no schema differences.',
  },
  {
    title: 'Reference Number Deduplication',
    description:
      'Duplicate detection is scoped to (referenceNumber, sourceBank) — not global. This ensures the same reference appearing in two different banks is treated as a cross-bank match candidate, not a duplicate.',
  },
]

const links = [
  {
    href: 'https://github.com/shrush-04/Reconcilia',
    icon: ExternalLink,
    label: 'GitHub Repository',
    desc: 'Source code, backend & frontend',
    color: 'text-gray-900',
    border: 'border-gray-200 hover:border-gray-400',
  },
  {
    href: '#',
    icon: Globe,
    label: 'LinkedIn Profile',
    desc: 'Connect professionally',
    color: 'text-sky-700',
    border: 'border-sky-200 hover:border-sky-400',
  },
  {
    href: 'mailto:placeholder@email.com',
    icon: Mail,
    label: 'Send an Email',
    desc: 'placeholder@email.com',
    color: 'text-indigo-700',
    border: 'border-indigo-200 hover:border-indigo-400',
  },
]

export default function AboutPage() {
  return (
    <div className="pt-16">
      {/* Page header */}
      <section className="bg-gradient-to-b from-indigo-50 to-white py-16 sm:py-20 border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <SectionHeading
            center
            label="About"
            title="About Reconcilia"
            subtitle="A focused backend engineering project demonstrating multi-source data reconciliation at scale."
          />
        </div>
      </section>

      {/* Project Description */}
      <section className="py-16 sm:py-20 bg-white">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.55 }}
            className="prose prose-gray max-w-none"
          >
            <div className="flex items-center gap-3 mb-8">
              <div className="w-11 h-11 rounded-xl bg-indigo-600 flex items-center justify-center shadow-sm">
                <GitMerge className="w-5 h-5 text-white" strokeWidth={2} />
              </div>
              <div>
                <h2 className="text-xl font-bold text-gray-900 leading-none">Reconcilia</h2>
                <p className="text-sm text-indigo-600 font-medium">Multi-Bank Statement Reconciliation Engine</p>
              </div>
            </div>

            <p className="text-base text-gray-600 leading-relaxed mb-5">
              Reconcilia is a Spring Boot-based backend system that automates the reconciliation of financial transactions
              across multiple bank sources. Given CSV statements from two or more banks, it ingests, normalizes, deduplicates,
              and cross-matches transactions — producing a classified output of matched, unmatched, and duplicate records.
            </p>

            <p className="text-base text-gray-600 leading-relaxed mb-5">
              The project focuses on correctness and extensibility: parsers are pluggable, matching logic is tolerance-aware,
              and all results are surfaced via a clean REST API with full OpenAPI documentation. Both an embedded H2 database
              (for development and testing) and PostgreSQL (for production) are supported through Flyway-managed migrations.
            </p>

            <p className="text-base text-gray-600 leading-relaxed">
              This frontend — built with React, Tailwind CSS v4, Framer Motion, and React Router — provides a visual walkthrough
              of the system: its pipeline, architecture, and API surface. Backend integration (live data from the Spring Boot API)
              will be added in a subsequent phase.
            </p>
          </motion.div>
        </div>
      </section>

      {/* Design Decisions */}
      <section className="py-16 sm:py-20 bg-gray-50 border-t border-gray-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-10">
            <SectionHeading
              label="Design"
              title="Key Engineering Decisions"
              subtitle="Architectural choices that make the system reliable, extensible, and testable."
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {decisions.map(({ title, description }, i) => (
              <motion.div
                key={title}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: '-40px' }}
                transition={{ duration: 0.5, delay: i * 0.09 }}
                className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 hover:shadow-md hover:-translate-y-0.5 transition-all duration-300"
              >
                <div className="flex items-start gap-3 mb-3">
                  <span className="w-6 h-6 rounded-md bg-indigo-600 text-white text-xs font-bold flex items-center justify-center shrink-0 mt-0.5">
                    {i + 1}
                  </span>
                  <h3 className="text-base font-bold text-gray-900 leading-snug">{title}</h3>
                </div>
                <p className="text-sm text-gray-500 leading-relaxed pl-9">{description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Tech Stack Table */}
      <section className="py-16 sm:py-20 bg-white border-t border-gray-100">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="mb-8">
            <SectionHeading
              label="Stack"
              title="Technology Summary"
            />
          </div>

          <motion.div
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
            className="rounded-2xl border border-gray-100 overflow-hidden shadow-sm"
          >
            <table className="w-full text-sm">
              <tbody className="divide-y divide-gray-50">
                {techDetails.map(({ label, value, icon: Icon }, i) => (
                  <motion.tr
                    key={label}
                    initial={{ opacity: 0 }}
                    whileInView={{ opacity: 1 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.3, delay: i * 0.05 }}
                    className="bg-white hover:bg-indigo-50/30 transition-colors duration-150"
                  >
                    <td className="px-5 py-3.5 align-middle">
                      <div className="flex items-center gap-2.5">
                        <Icon className="w-4 h-4 text-indigo-400 shrink-0" strokeWidth={2} />
                        <span className="font-semibold text-gray-700 whitespace-nowrap">{label}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 align-middle text-gray-500">{value}</td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </motion.div>
        </div>
      </section>

      {/* Connect */}
      <section className="py-16 sm:py-20 bg-gray-50 border-t border-gray-100">
        <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
          >
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Get in touch</h2>
            <p className="text-gray-500 text-sm mb-8">Find the project on GitHub or connect directly.</p>

            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              {links.map(({ href, icon: Icon, label, desc, color, border }) => (
                <a
                  key={label}
                  href={href}
                  target="_blank"
                  rel="noopener noreferrer"
                  className={`group flex items-center gap-3 px-5 py-4 bg-white rounded-2xl border ${border} shadow-sm hover:shadow-md transition-all duration-200 text-left`}
                >
                  <div className={`w-10 h-10 rounded-xl bg-gray-50 flex items-center justify-center shrink-0 ${color} group-hover:bg-indigo-50 transition-colors duration-200`}>
                    <Icon className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-900">{label}</p>
                    <p className="text-xs text-gray-400">{desc}</p>
                  </div>
                </a>
              ))}
            </div>
          </motion.div>
        </div>
      </section>
    </div>
  )
}

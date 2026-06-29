import { motion } from 'framer-motion'
import { pipelineSteps } from '../data/mockPipelineSteps'
import StepCard from '../components/StepCard'
import SectionHeading from '../components/SectionHeading'

export default function HowItWorksPage() {
  return (
    <div className="pt-16">
      {/* Page header */}
      <section className="bg-gradient-to-b from-indigo-50 to-white py-16 sm:py-20 border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <SectionHeading
            center
            label="Pipeline"
            title="How Reconcilia Works"
            subtitle="A six-step automated pipeline that ingests raw bank statements and produces classified, queryable transaction records."
          />
        </div>
      </section>

      {/* Timeline */}
      <section className="py-16 sm:py-24 bg-gray-50">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Desktop: alternating two-column with center spine */}
          <div className="hidden md:block relative">
            {/* Center spine line */}
            <div className="absolute left-1/2 top-0 bottom-0 w-px bg-gradient-to-b from-indigo-200 via-indigo-300 to-transparent -translate-x-1/2" />

            <div className="space-y-12">
              {pipelineSteps.map((step, i) => {
                const isLeft = i % 2 === 0
                return (
                  <div key={step.step} className="grid grid-cols-2 gap-12 items-center">
                    {/* Left slot */}
                    <div className={isLeft ? '' : 'col-start-2'}>
                      <StepCard {...step} index={i} side={isLeft ? 'left' : 'right'} />
                    </div>

                    {/* Center dot (overlaid on spine) */}
                    <motion.div
                      initial={{ scale: 0 }}
                      whileInView={{ scale: 1 }}
                      viewport={{ once: true }}
                      transition={{ duration: 0.3, delay: i * 0.07 + 0.2 }}
                      className={`absolute left-1/2 -translate-x-1/2 w-6 h-6 rounded-full border-2 border-white shadow-sm ${step.accent} flex items-center justify-center`}
                      style={{ top: `calc(${i * (100 / (pipelineSteps.length - 1))}% - 12px)` }}
                    >
                      <span className="w-2 h-2 rounded-full bg-white/80" />
                    </motion.div>

                    {/* Right slot */}
                    {isLeft && <div />}
                  </div>
                )
              })}
            </div>
          </div>

          {/* Mobile: single-column stacked */}
          <div className="md:hidden space-y-6">
            {pipelineSteps.map((step, i) => (
              <StepCard key={step.step} {...step} index={i} side="left" />
            ))}
          </div>
        </div>
      </section>

      {/* Summary strip */}
      <section className="py-14 bg-white border-t border-gray-100">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
            className="bg-indigo-600 rounded-2xl px-8 py-10 text-white text-center"
          >
            <h3 className="text-2xl font-bold mb-3">End result: three classification buckets</h3>
            <p className="text-indigo-200 text-sm mb-8 max-w-xl mx-auto">
              Every transaction processed by the engine exits in one of three states, each queryable via a dedicated REST endpoint.
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
              {[
                { label: 'MATCHED', desc: 'Successfully reconciled across banks', color: 'bg-emerald-500' },
                { label: 'UNMATCHED', desc: 'No cross-bank match found — flagged for review', color: 'bg-amber-400' },
                { label: 'DUPLICATE', desc: 'Same reference, amount, and date already seen', color: 'bg-rose-400' },
              ].map(({ label, desc, color }) => (
                <div key={label} className="bg-white/10 rounded-xl p-5 text-left">
                  <div className="flex items-center gap-2 mb-2">
                    <span className={`w-2.5 h-2.5 rounded-full ${color}`} />
                    <span className="text-sm font-bold font-mono tracking-wide">{label}</span>
                  </div>
                  <p className="text-xs text-indigo-200 leading-relaxed">{desc}</p>
                </div>
              ))}
            </div>
          </motion.div>
        </div>
      </section>
    </div>
  )
}

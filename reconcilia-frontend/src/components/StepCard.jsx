import { motion } from 'framer-motion'

export default function StepCard({ step, icon: Icon, title, subtitle, description, detail, accent, index, side }) {
  const isLeft = side === 'left'

  return (
    <motion.div
      initial={{ opacity: 0, x: isLeft ? -32 : 32 }}
      whileInView={{ opacity: 1, x: 0 }}
      viewport={{ once: true, margin: '-60px' }}
      transition={{ duration: 0.55, delay: index * 0.07, ease: [0.22, 1, 0.36, 1] }}
      className="group relative bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 p-6 sm:p-8"
    >
      {/* Step number badge */}
      <div className={`w-10 h-10 rounded-xl ${accent} flex items-center justify-center mb-5 shadow-sm`}>
        <Icon className="w-5 h-5 text-white" strokeWidth={2} />
      </div>

      <div className="flex items-start justify-between gap-3 mb-3">
        <div>
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-0.5">Step {step}</p>
          <h3 className="text-xl font-bold text-gray-900">{title}</h3>
          <p className="text-sm font-medium text-indigo-600 mt-0.5">{subtitle}</p>
        </div>
        <span className="shrink-0 text-4xl font-black text-gray-100 group-hover:text-indigo-50 transition-colors duration-300 select-none leading-none">
          {String(step).padStart(2, '0')}
        </span>
      </div>

      <p className="text-sm text-gray-600 leading-relaxed mb-4">{description}</p>

      {detail && (
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-gray-50 border border-gray-100">
          <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 shrink-0" />
          <code className="text-xs text-gray-600 font-mono">{detail}</code>
        </div>
      )}
    </motion.div>
  )
}

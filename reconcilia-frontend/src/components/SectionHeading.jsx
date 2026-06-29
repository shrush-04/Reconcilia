import { motion } from 'framer-motion'

export default function SectionHeading({ label, title, subtitle, center = false }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-60px' }}
      transition={{ duration: 0.55, ease: [0.22, 1, 0.36, 1] }}
      className={center ? 'text-center' : ''}
    >
      {label && (
        <span className="inline-block text-xs font-semibold tracking-widest text-indigo-600 uppercase mb-3">
          {label}
        </span>
      )}
      <h2 className="text-3xl sm:text-4xl font-bold text-gray-900 leading-tight mb-3">
        {title}
      </h2>
      {subtitle && (
        <p className="text-lg text-gray-500 leading-relaxed max-w-2xl mx-auto">
          {subtitle}
        </p>
      )}
    </motion.div>
  )
}

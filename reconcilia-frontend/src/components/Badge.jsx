export default function Badge({ children, variant = 'default' }) {
  const variants = {
    default: 'bg-indigo-50 text-indigo-700 border border-indigo-100',
    gray: 'bg-gray-100 text-gray-700 border border-gray-200',
    green: 'bg-emerald-50 text-emerald-700 border border-emerald-100',
    amber: 'bg-amber-50 text-amber-700 border border-amber-100',
    rose: 'bg-rose-50 text-rose-700 border border-rose-100',
    sky: 'bg-sky-50 text-sky-700 border border-sky-100',
    violet: 'bg-violet-50 text-violet-700 border border-violet-100',
  }

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium ${variants[variant] ?? variants.default}`}
    >
      {children}
    </span>
  )
}

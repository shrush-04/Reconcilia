import { motion } from 'framer-motion'

const methodStyles = {
  GET:    'bg-emerald-50 text-emerald-700 border border-emerald-200',
  POST:   'bg-indigo-50 text-indigo-700 border border-indigo-200',
  PUT:    'bg-amber-50 text-amber-700 border border-amber-200',
  DELETE: 'bg-rose-50 text-rose-700 border border-rose-200',
  PATCH:  'bg-sky-50 text-sky-700 border border-sky-200',
}

export default function EndpointRow({ method, path, description, params, response, index }) {
  return (
    <motion.tr
      initial={{ opacity: 0, y: 12 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-40px' }}
      transition={{ duration: 0.4, delay: index * 0.06 }}
      className="group hover:bg-indigo-50/40 transition-colors duration-200"
    >
      {/* Method */}
      <td className="px-5 py-4 whitespace-nowrap align-top">
        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-bold font-mono tracking-wide ${methodStyles[method] ?? 'bg-gray-100 text-gray-700'}`}>
          {method}
        </span>
      </td>

      {/* Path */}
      <td className="px-5 py-4 align-top">
        <code className="text-sm font-mono font-semibold text-gray-800 break-all">
          {path}
        </code>
      </td>

      {/* Description */}
      <td className="px-5 py-4 align-top">
        <p className="text-sm text-gray-600 leading-relaxed">{description}</p>
        {params && params !== '—' && (
          <p className="text-xs text-gray-400 mt-1">
            <span className="font-medium text-gray-500">Params: </span>{params}
          </p>
        )}
      </td>

      {/* Response */}
      <td className="px-5 py-4 align-top whitespace-nowrap hidden lg:table-cell">
        <code className="text-xs font-mono text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
          {response}
        </code>
      </td>
    </motion.tr>
  )
}

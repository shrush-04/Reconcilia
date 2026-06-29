import { Link } from 'react-router-dom'
import { GitMerge, ExternalLink, Globe, Mail } from 'lucide-react'

const navLinks = [
  { to: '/', label: 'Home' },
  { to: '/how-it-works', label: 'How It Works' },
  { to: '/architecture', label: 'Architecture' },
  { to: '/console', label: 'Console' },
  { to: '/about', label: 'About' },
]

const socialLinks = [
  { href: 'https://github.com/shrush-04/Reconcilia', icon: ExternalLink, label: 'GitHub' },
  { href: '#', icon: Globe, label: 'LinkedIn' },
  { href: 'mailto:placeholder@email.com', icon: Mail, label: 'Email' },
]

export default function Footer() {
  return (
    <footer className="border-t border-gray-100 bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2.5 mb-3">
              <div className="w-7 h-7 rounded-md bg-indigo-600 flex items-center justify-center">
                <GitMerge className="w-4 h-4 text-white" strokeWidth={2.5} />
              </div>
              <span className="text-base font-bold text-gray-900">Reconcilia</span>
            </div>
            <p className="text-sm text-gray-500 leading-relaxed max-w-xs">
              Automated multi-bank statement reconciliation engine built with Spring Boot and Java.
            </p>
          </div>

          {/* Navigation */}
          <div>
            <h4 className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">Navigation</h4>
            <ul className="space-y-2">
              {navLinks.map(({ to, label }) => (
                <li key={to}>
                  <Link
                    to={to}
                    className="text-sm text-gray-500 hover:text-indigo-600 transition-colors duration-200"
                  >
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Connect */}
          <div>
            <h4 className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">Connect</h4>
            <div className="flex gap-3">
              {socialLinks.map(({ href, icon: Icon, label }) => (
                <a
                  key={label}
                  href={href}
                  target="_blank"
                  rel="noopener noreferrer"
                  aria-label={label}
                  className="w-9 h-9 rounded-lg border border-gray-200 bg-white flex items-center justify-center text-gray-500 hover:text-indigo-600 hover:border-indigo-300 hover:shadow-sm transition-all duration-200"
                >
                  <Icon className="w-4 h-4" />
                </a>
              ))}
            </div>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="pt-6 border-t border-gray-200 flex flex-col sm:flex-row items-center justify-between gap-2">
          <p className="text-xs text-gray-400">
            © {new Date().getFullYear()} Reconcilia. Built with Spring Boot, React & Tailwind CSS.
          </p>
          <p className="text-xs text-gray-400">
            Multi-bank statement reconciliation engine.
          </p>
        </div>
      </div>
    </footer>
  )
}

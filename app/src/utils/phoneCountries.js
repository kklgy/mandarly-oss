export const PHONE_COUNTRIES = [
  { iso: 'AU', name: 'Australia', dialCode: '61' },
  { iso: 'BH', name: 'Bahrain', dialCode: '973' },
  { iso: 'CA', name: 'Canada', dialCode: '1' },
  { iso: 'CN', name: 'China mainland', dialCode: '86' },
  { iso: 'FR', name: 'France', dialCode: '33' },
  { iso: 'DE', name: 'Germany', dialCode: '49' },
  { iso: 'GH', name: 'Ghana', dialCode: '233' },
  { iso: 'HK', name: 'Hong Kong', dialCode: '852' },
  { iso: 'ID', name: 'Indonesia', dialCode: '62' },
  { iso: 'IT', name: 'Italy', dialCode: '39' },
  { iso: 'JP', name: 'Japan', dialCode: '81' },
  { iso: 'KW', name: 'Kuwait', dialCode: '965' },
  { iso: 'MO', name: 'Macau', dialCode: '853' },
  { iso: 'MY', name: 'Malaysia', dialCode: '60' },
  { iso: 'NL', name: 'Netherlands', dialCode: '31' },
  { iso: 'NZ', name: 'New Zealand', dialCode: '64' },
  { iso: 'NG', name: 'Nigeria', dialCode: '234' },
  { iso: 'OM', name: 'Oman', dialCode: '968' },
  { iso: 'PH', name: 'Philippines', dialCode: '63' },
  { iso: 'QA', name: 'Qatar', dialCode: '974' },
  { iso: 'SA', name: 'Saudi Arabia', dialCode: '966' },
  { iso: 'SG', name: 'Singapore', dialCode: '65' },
  { iso: 'ZA', name: 'South Africa', dialCode: '27' },
  { iso: 'KR', name: 'South Korea', dialCode: '82' },
  { iso: 'ES', name: 'Spain', dialCode: '34' },
  { iso: 'TW', name: 'Taiwan', dialCode: '886' },
  { iso: 'TH', name: 'Thailand', dialCode: '66' },
  { iso: 'AE', name: 'United Arab Emirates', dialCode: '971' },
  { iso: 'GB', name: 'United Kingdom', dialCode: '44' },
  { iso: 'US', name: 'United States', dialCode: '1' },
  { iso: 'VN', name: 'Vietnam', dialCode: '84' }
]

const COUNTRY_BY_ISO = new Map(PHONE_COUNTRIES.map((item) => [item.iso, item]))
const MATCH_COUNTRIES = [...PHONE_COUNTRIES].sort((a, b) => b.dialCode.length - a.dialCode.length)

export function digitsOnly(value) {
  return String(value || '').replace(/\D/g, '')
}

export function getCountryByIso(iso) {
  return COUNTRY_BY_ISO.get(iso) || COUNTRY_BY_ISO.get('HK')
}

export function defaultPhoneCountry(locale, timezone = Intl.DateTimeFormat().resolvedOptions().timeZone) {
  if (timezone === 'Africa/Accra') return getCountryByIso('GH')
  if (timezone === 'Asia/Shanghai') return getCountryByIso('CN')
  if (timezone === 'Asia/Taipei') return getCountryByIso('TW')
  if (timezone === 'Asia/Dubai') return getCountryByIso('AE')

  const normalized = String(locale || '').toLowerCase()
  if (normalized.startsWith('zh-cn')) return getCountryByIso('CN')
  if (normalized.startsWith('zh-tw')) return getCountryByIso('TW')
  if (normalized.startsWith('ar')) return getCountryByIso('AE')
  return getCountryByIso('HK')
}

export function parseE164Phone(value) {
  if (!value || !String(value).startsWith('+')) return null
  const digits = digitsOnly(value)
  const country = MATCH_COUNTRIES.find((item) => digits.startsWith(item.dialCode))
  if (!country) return null
  return {
    country,
    nationalNumber: digits.slice(country.dialCode.length)
  }
}

export function buildE164Phone(countryIso, nationalNumber) {
  const country = getCountryByIso(countryIso)
  const localDigits = digitsOnly(nationalNumber)
  return localDigits ? `+${country.dialCode}${localDigits}` : ''
}

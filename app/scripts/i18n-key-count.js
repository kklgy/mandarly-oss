#!/usr/bin/env node
/**
 * i18n 4 套同步校验
 *
 * 用法: node app/scripts/i18n-key-count.js
 *
 * 检查项:
 *   1. 4 文件 leaf key 总数对齐(深度展平)
 *   2. 各文件独有 key 报警(以 zh-CN 为基准做差集)
 *   3. 任何 leaf 字符串值含 raw `@` 字符(未通过 backtick `{'@'}` 转义)报警
 *
 * 退出码:
 *   0 = 通过(4 套 key 一致 + 无未转义 @)
 *   1 = 不一致或检测到危险字符
 *
 * 依赖:纯 Node 18+ ESM,不引第三方包,不依赖 vitest / vue。
 */

import { pathToFileURL } from 'node:url'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const LOCALES_DIR = resolve(__dirname, '../src/i18n/locales')

const LOCALES = ['en', 'zh-CN', 'zh-TW', 'ar']

// 颜色(终端友好,不依赖 chalk)
const c = {
  red: (s) => `\x1b[31m${s}\x1b[0m`,
  green: (s) => `\x1b[32m${s}\x1b[0m`,
  yellow: (s) => `\x1b[33m${s}\x1b[0m`,
  cyan: (s) => `\x1b[36m${s}\x1b[0m`,
  bold: (s) => `\x1b[1m${s}\x1b[0m`
}

/**
 * 深度展平 object 为路径列表
 * 只把 string / number / boolean 视为 leaf;数组也视为 leaf(整体当一条)
 *
 * @param {any} obj
 * @param {string} prefix
 * @param {Map<string, any>} acc - path -> leaf value
 */
function flatten(obj, prefix = '', acc = new Map()) {
  if (obj === null || obj === undefined) return acc
  if (typeof obj !== 'object' || Array.isArray(obj)) {
    acc.set(prefix, obj)
    return acc
  }
  for (const [k, v] of Object.entries(obj)) {
    const next = prefix ? `${prefix}.${k}` : k
    if (v !== null && typeof v === 'object' && !Array.isArray(v)) {
      flatten(v, next, acc)
    } else {
      acc.set(next, v)
    }
  }
  return acc
}

/**
 * 检测 leaf 字符串是否含未转义 `@`
 * vue-i18n message compiler 会把 `@:foo` 当 link key、`@.lower:foo` 当 modifier
 * 安全写法:用 backtick 字符串 + `{'@'}` 转义,运行时合并成 `@`
 *
 * 这里采用务实策略:对每条 string leaf,扫描所有 `@` 位置;
 * 若任意一个 `@` 后跟随 `:` / `.lower:` / `.upper:` / `.capitalize:`,视为含未转义 link 语法。
 * 文案中正常出现的 `@example.com` / `student@xxx` 不会触发。
 */
function findRawAtIssues(value) {
  if (typeof value !== 'string') return []
  const issues = []
  const atRe = /@(\.(lower|upper|capitalize))?:/g
  let m
  while ((m = atRe.exec(value)) !== null) {
    issues.push({ index: m.index, snippet: value.slice(Math.max(0, m.index - 8), m.index + 16) })
  }
  return issues
}

async function loadLocale(name) {
  const file = resolve(LOCALES_DIR, `${name}.js`)
  const url = pathToFileURL(file).href
  const mod = await import(url)
  return { name, file, data: mod.default }
}

async function main() {
  console.log(c.bold('i18n 4 套同步校验\n'))

  // 加载 4 套
  const locales = []
  for (const name of LOCALES) {
    try {
      locales.push(await loadLocale(name))
    } catch (err) {
      console.error(c.red(`✗ 加载 ${name}.js 失败:`), err.message)
      process.exit(1)
    }
  }

  // 展平
  const flats = locales.map(({ name, data }) => ({ name, map: flatten(data) }))

  // 1. 计数报告
  console.log(c.bold('# Leaf key 数量'))
  for (const { name, map } of flats) {
    console.log(`  ${c.cyan(name.padEnd(6))} : ${map.size}`)
  }
  console.log()

  // 2. 差集报告(以 en 为基准,因为本项目主语言 + fallback)
  const base = flats[0]
  const baseKeys = new Set(base.map.keys())

  let hasDiff = false
  console.log(c.bold('# Key 集合一致性'))
  for (const { name, map } of flats) {
    if (name === base.name) continue
    const keys = new Set(map.keys())
    const missing = [...baseKeys].filter((k) => !keys.has(k))
    const extra = [...keys].filter((k) => !baseKeys.has(k))
    if (missing.length === 0 && extra.length === 0) {
      console.log(`  ${c.green('✓')} ${name} 与 ${base.name} 一致`)
    } else {
      hasDiff = true
      if (missing.length) {
        console.log(`  ${c.red('✗')} ${name} 缺少 ${missing.length} 个 key(相比 ${base.name}):`)
        for (const k of missing.slice(0, 20)) console.log(`      - ${k}`)
        if (missing.length > 20) console.log(`      ...(还有 ${missing.length - 20} 条)`)
      }
      if (extra.length) {
        console.log(`  ${c.yellow('!')} ${name} 多出 ${extra.length} 个 key(${base.name} 没有):`)
        for (const k of extra.slice(0, 20)) console.log(`      + ${k}`)
        if (extra.length > 20) console.log(`      ...(还有 ${extra.length - 20} 条)`)
      }
    }
  }
  console.log()

  // 3. raw @ 检测
  console.log(c.bold('# vue-i18n @ 字符转义检测'))
  let hasAtIssue = false
  for (const { name, map } of flats) {
    const offenders = []
    for (const [path, value] of map) {
      const issues = findRawAtIssues(value)
      if (issues.length) offenders.push({ path, value, issues })
    }
    if (offenders.length === 0) {
      console.log(`  ${c.green('✓')} ${name} 无未转义 @link 语法`)
    } else {
      hasAtIssue = true
      console.log(`  ${c.red('✗')} ${name} 发现 ${offenders.length} 条疑似未转义的 @link 语法:`)
      for (const { path, value } of offenders.slice(0, 10)) {
        console.log(`      - ${path} => ${JSON.stringify(value).slice(0, 80)}`)
      }
      if (offenders.length > 10) console.log(`      ...(还有 ${offenders.length - 10} 条)`)
      console.log(c.yellow(`      提示:用 backtick 字符串 + {'@'} 转义,如 \`hello{'@'}example.com\``))
    }
  }
  console.log()

  if (hasDiff || hasAtIssue) {
    console.log(c.red(c.bold('校验未通过')))
    process.exit(1)
  } else {
    console.log(c.green(c.bold('校验通过')))
    process.exit(0)
  }
}

main().catch((err) => {
  console.error(c.red('意外错误:'), err)
  process.exit(1)
})

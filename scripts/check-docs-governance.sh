#!/usr/bin/env bash
set -euo pipefail

failures=0

fail() {
  printf 'FAIL: %s\n' "$1" >&2
  failures=$((failures + 1))
}

check_exists() {
  local file="$1"
  [ -f "$file" ] || fail "missing required file: $file"
}

check_frontmatter() {
  local file="$1"
  check_exists "$file"
  [ -f "$file" ] || return 0

  if [ "$(sed -n '1p' "$file")" != "---" ]; then
    fail "$file missing frontmatter opening"
    return 0
  fi

  sed -n '1,12p' "$file" | grep -q '^status: ' || fail "$file missing status frontmatter"
  sed -n '1,12p' "$file" | grep -q '^type: ' || fail "$file missing type frontmatter"
  sed -n '1,12p' "$file" | grep -q '^domain: ' || fail "$file missing domain frontmatter"
}

check_line_limit() {
  local file="$1"
  local limit="$2"
  check_exists "$file"
  [ -f "$file" ] || return 0

  local lines
  lines="$(wc -l < "$file" | tr -d ' ')"
  if [ "$lines" -gt "$limit" ]; then
    fail "$file has $lines lines, expected <= $limit"
  fi
}

required_frontmatter_files="
docs/README.md
docs/AGENT_INDEX.md
docs/DOCS_CONTRACT.md
docs/progress/README.md
docs/product/prd-v4.md
docs/product/prd-v3-final.md
docs/database/README.md
docs/frontend/README.md
docs/frontend/ui-style-guide-v1.md
docs/frontend/visual-reference/DESIGN-mandarly-v1.md
docs/operations/server-info.md
docs/compliance/company-entity.md
docs/changelog/2026-05.md
"

for file in $required_frontmatter_files; do
  check_frontmatter "$file"
done

check_line_limit "docs/README.md" 250
check_line_limit "docs/progress/README.md" 350
check_line_limit "docs/changelog/2026-05.md" 150

for old_dir in docs/PRD docs/db-design docs/前端规范 docs/合规 docs/运维 docs/调研 docs/经验沉淀 docs/进度; do
  if [ -e "$old_dir" ]; then
    fail "legacy docs directory should not exist: $old_dir"
  fi
done

grep -q 'AGENT_INDEX.md' docs/README.md || fail "docs/README.md does not link AGENT_INDEX.md"
grep -q 'DOCS_CONTRACT.md' docs/README.md || fail "docs/README.md does not link DOCS_CONTRACT.md"
grep -q 'docs-governance' docs/README.md || fail "docs/README.md does not mention docs-governance routing"

if [ ! -d docs/changelog/2026-05 ]; then
  fail "missing daily changelog directory: docs/changelog/2026-05"
else
  changelog_count="$(find docs/changelog/2026-05 -type f -name '2026-05-*.md' | wc -l | tr -d ' ')"
  if [ "$changelog_count" -lt 1 ]; then
    fail "docs/changelog/2026-05 has no daily entries"
  fi
  for file in docs/changelog/2026-05/2026-05-*.md; do
    [ -e "$file" ] || continue
    check_frontmatter "$file"
  done
fi

check_frontmatter "docs/progress/d22-cross-border-access-draft.md"
grep -q '^status: superseded' docs/progress/d22-cross-border-access-draft.md \
  || fail "D22 discussion draft should be marked superseded"

if [ "$failures" -gt 0 ]; then
  printf '\nDocs governance check failed: %s issue(s).\n' "$failures" >&2
  exit 1
fi

printf 'Docs governance check passed.\n'

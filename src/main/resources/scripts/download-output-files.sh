#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# download-output-files.sh
#
# Downloads CSV files located under "output/**/*.csv" from a GitHub repository.
# Intended to be invoked by a Spring Batch SystemCommandTasklet, which passes
# the relevant parameters extracted from a GitHub push-event webhook payload.
#
# Required environment variables (set by the SystemCommandTasklet):
#   REPO_FULL_NAME  – owner/repo  (e.g. "anisbouhadida/medz-data")
#   BRANCH          – branch ref  (e.g. "refs/heads/main" or "main")
#   MODIFIED_FILES  – comma-separated list of added/modified file paths
#                     coming from the push event commits
#   INPUT_DIR       – local directory where downloaded files will be stored
#
# Optional:
#   GITHUB_TOKEN    – personal access token for private repos
# ---------------------------------------------------------------------------
set -euo pipefail

# ── Validate required variables ──────────────────────────────────────────────
: "${REPO_FULL_NAME:?Variable REPO_FULL_NAME is required}"
: "${BRANCH:?Variable BRANCH is required}"
: "${MODIFIED_FILES:?Variable MODIFIED_FILES is required}"
: "${INPUT_DIR:?Variable INPUT_DIR is required}"

# ── Normalise branch name (strip refs/heads/ prefix if present) ──────────────
BRANCH_NAME="${BRANCH#refs/heads/}"

# ── Build auth header if a token is available ────────────────────────────────
AUTH_HEADER=""
if [[ -n "${GITHUB_TOKEN:-}" ]]; then
  AUTH_HEADER="Authorization: token ${GITHUB_TOKEN}"
fi

# ── Base URL for raw file download ───────────────────────────────────────────
RAW_BASE="https://raw.githubusercontent.com/${REPO_FULL_NAME}/${BRANCH_NAME}"

# ── Download each relevant file ─────────────────────────────────────────────
IFS=',' read -ra FILES <<< "${MODIFIED_FILES}"

downloaded=0
for file in "${FILES[@]}"; do
  # Trim whitespace
  file="$(echo -e "${file}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"

  # Only process CSV files under the output/ directory
  if [[ "${file}" != output/* ]] || [[ "${file}" != *.csv ]]; then
    echo "SKIP (not under output/**/*.csv): ${file}"
    continue
  fi

  # Derive the local target path: strip the leading "output/" prefix so that
  # output/2024-08/nomenclature.csv  →  <INPUT_DIR>/2024-08/nomenclature.csv
  relative="${file#output/}"
  target_path="${INPUT_DIR}/${relative}"
  target_dir="$(dirname "${target_path}")"

  mkdir -p "${target_dir}"

  download_url="${RAW_BASE}/${file}"

  echo "Downloading ${download_url} → ${target_path}"

  curl_args=(--fail --silent --show-error --location --output "${target_path}")
  if [[ -n "${AUTH_HEADER}" ]]; then
    curl_args+=(--header "${AUTH_HEADER}")
  fi

  if curl "${curl_args[@]}" "${download_url}"; then
    echo "  ✓ saved ${target_path}"
    downloaded=$((downloaded + 1))
  else
    echo "  ✗ failed to download ${file}" >&2
    exit 1
  fi
done

echo ""
echo "Download complete: ${downloaded} file(s) saved to ${INPUT_DIR}"



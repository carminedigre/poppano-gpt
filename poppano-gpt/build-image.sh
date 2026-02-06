#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 [native|jvm] [image-name]" >&2
  echo "  native: use Dockerfile.native" >&2
  echo "  jvm   : use Dockerfile (default)" >&2
  echo "  image-name: optional, default poppano-gpt" >&2
}

mode="${1:-jvm}"
image_name="${2:-poppano-gpt}"

case "$mode" in
  native)
    dockerfile="Dockerfile.native"
    tag_suffix="-native"
    ;;
  jvm)
    dockerfile="Dockerfile"
    tag_suffix=""
    ;;
  -h|--help)
    usage
    exit 0
    ;;
  *)
    echo "Unknown mode: $mode" >&2
    usage
    exit 1
    ;;
 esac

tag="${image_name}${tag_suffix}"

if [[ ! -f "$dockerfile" ]]; then
  echo "Missing $dockerfile in current directory." >&2
  exit 1
fi

echo "Building image '$tag' using $dockerfile..."
docker build -f "$dockerfile" -t "$tag" .

#!/bin/bash
set -euo pipefail

# =============================================================================
# GCP VM startup script: installs Docker Engine + Compose plugin.
#
# Runs automatically on first boot when passed via
# `--metadata-from-file startup-script=...` (see create-vm.sh). Also safe to
# re-run by hand over SSH (`sudo bash startup-script.sh`) — apt/get.docker.com
# are idempotent.
# =============================================================================

if command -v docker &> /dev/null; then
  echo "Docker already installed, skipping."
  exit 0
fi

apt-get update
apt-get install -y ca-certificates curl gnupg git

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Let the default GCE user run docker without sudo.
usermod -aG docker "$(logname 2>/dev/null || echo ubuntu)" || true

systemctl enable docker
systemctl start docker

echo "Docker install complete: $(docker --version)"
echo "Compose plugin: $(docker compose version)"

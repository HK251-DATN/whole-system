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

# Runs on every boot, ahead of the early-exit below, so it still applies
# after Docker's already installed (a reboot on this VM would otherwise skip
# straight past everything else in this file). Hardcoded to this one account
# since this VM is only ever SSHed into by khoitrananh. `|| true` matters: on
# a from-scratch VM this runs before GCE has lazily created the account
# (that happens on first SSH login), so usermod would otherwise fail here
# and — with set -e above — abort the rest of this script.
usermod -aG docker khoitrananh    || true
usermod -aG docker trananhkhoitv  || true

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

# Not adding any user to the `docker` group here: this script runs as root
# at boot time, before GCE has created an account for whoever SSHes in later
# (that account's name depends on your Google identity / OS Login config,
# and isn't knowable in advance). Run this once after your first SSH login
# instead: `sudo usermod -aG docker $USER && newgrp docker`.

systemctl enable docker
systemctl start docker

echo "Docker install complete: $(docker --version)"
echo "Compose plugin: $(docker compose version)"

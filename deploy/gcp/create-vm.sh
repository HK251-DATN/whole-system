#!/bin/bash
set -euo pipefail

# =============================================================================
# Creates the GCP VM, reserves a static IP, and opens the firewall ports for
# the backend stack. Run this from your own authenticated `gcloud` shell
# (not on the VM). Idempotent-ish: re-running skips resources that already
# exist where gcloud supports that check.
# =============================================================================

PROJECT_ID="ecommerce-system-503610"
ZONE="asia-southeast1-c"
REGION="asia-southeast1"
VM_NAME="ecommerce-backend"
MACHINE_TYPE="e2-standard-4"     # 4 vCPU / 16GB — Postgres + Kafka + 4 JVMs
BOOT_DISK_SIZE="30GB"            # backend-only checkout + docker images; frontend repos not needed
IMAGE_FAMILY="ubuntu-2404-lts-amd64"
IMAGE_PROJECT="ubuntu-os-cloud"
NETWORK_TAG="ecommerce-backend"
STATIC_IP_NAME="${VM_NAME}-ip"

gcloud config set project "$PROJECT_ID"

echo "==> Reserving static external IP: ${STATIC_IP_NAME}"
gcloud compute addresses create "$STATIC_IP_NAME" \
  --region="$REGION" \
  --project="$PROJECT_ID" || echo "  (already exists, skipping)"

STATIC_IP=$(gcloud compute addresses describe "$STATIC_IP_NAME" \
  --region="$REGION" --project="$PROJECT_ID" --format='get(address)')
echo "  Static IP: $STATIC_IP"

echo "==> Creating firewall rule: allow SSH"
gcloud compute firewall-rules create "${VM_NAME}-allow-ssh" \
  --project="$PROJECT_ID" \
  --network=default \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:22 \
  --target-tags="$NETWORK_TAG" \
  --source-ranges=0.0.0.0/0 || echo "  (already exists, skipping)"

echo "==> Creating firewall rule: allow HTTP/HTTPS (Caddy reverse proxy)"
gcloud compute firewall-rules create "${VM_NAME}-allow-http-https" \
  --project="$PROJECT_ID" \
  --network=default \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:80,tcp:443 \
  --target-tags="$NETWORK_TAG" \
  --source-ranges=0.0.0.0/0 || echo "  (already exists, skipping)"

echo "==> Creating firewall rule: allow core service ports (9000/9100/9200/9300)"
gcloud compute firewall-rules create "${VM_NAME}-allow-core-services" \
  --project="$PROJECT_ID" \
  --network=default \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:9000,tcp:9100,tcp:9200,tcp:9300 \
  --target-tags="$NETWORK_TAG" \
  --source-ranges=0.0.0.0/0 || echo "  (already exists, skipping)"

# Postgres publishes on DB_HOST_PORT from .env, which defaults to 5432
# (.env.example) — falls back to 5433 if DB_HOST_PORT is unset entirely, see
# docker-compose.yml. Open whichever one your .env actually uses.
#
# SECURITY: this exposes Postgres to the whole internet with whatever
# DB_USERNAME/DB_PASSWORD is in .env (the repo defaults, khoidev/khoicktv,
# are not safe to use here). Change the password in .env before deploying,
# or narrow --source-ranges below to your own IP (e.g. "1.2.3.4/32") instead
# of 0.0.0.0/0.
echo "==> Creating firewall rule: allow Postgres (5432)"
gcloud compute firewall-rules create "${VM_NAME}-allow-postgres" \
  --project="$PROJECT_ID" \
  --network=default \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:5432 \
  --target-tags="$NETWORK_TAG" \
  --source-ranges=0.0.0.0/0 || echo "  (already exists, skipping)"

# Kafka (9092), Kafka UI (9280) and pgAdmin (5480) are still deliberately NOT
# opened — reachable only from inside the VM (or via
# `gcloud compute ssh --ssh-flag="-L 5480:localhost:5480"` tunneling).

echo "==> Creating VM: $VM_NAME"
gcloud compute instances create "$VM_NAME" \
  --project="$PROJECT_ID" \
  --zone="$ZONE" \
  --machine-type="$MACHINE_TYPE" \
  --image-family="$IMAGE_FAMILY" \
  --image-project="$IMAGE_PROJECT" \
  --boot-disk-size="$BOOT_DISK_SIZE" \
  --boot-disk-type=pd-balanced \
  --tags="$NETWORK_TAG" \
  --address="$STATIC_IP" \
  --metadata-from-file=startup-script=deploy/gcp/startup-script.sh

echo ""
echo "==> Done. VM external IP: $STATIC_IP"
echo "    SSH in with: gcloud compute ssh $VM_NAME --zone=$ZONE --project=$PROJECT_ID"
echo "    Docker install runs automatically via the startup script (takes ~1-2 min after boot)."

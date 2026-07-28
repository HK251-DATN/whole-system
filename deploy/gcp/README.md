# Deploying the backend to a GCP VM

Deploys Postgres + Kafka + the 4 core services (identity, back-office,
product-storage, ecommerce) to a single GCP Compute Engine VM, plus
tools/scenario-seeder for optionally seeding demo data. Search/chat and the
frontend UIs are intentionally left out (bring them up later with
`docker compose --profile search|frontend ...` if needed).

- **Project:** `ecommerce-system-503610`
- **Zone:** `asia-southeast1-c`
- **Machine:** `e2-standard-4` (4 vCPU / 16GB) — Kafka + Postgres + 4 JVMs need
  real headroom; see the heap caps in `deploy/gcp/compose.prod.yml`.

All commands below run from **your own machine** (with `gcloud` authenticated)
unless marked "on the VM".

## 0. Create the VM

```bash
gcloud auth login   # if not already
./deploy/gcp/create-vm.sh
```

This reserves a static IP, opens firewall ports (22, 80, 443, 9000, 9100,
9200, 9300, 5432, 5480 — **not** 9092/9280, which stay internal-only), and
creates the VM with `deploy/gcp/startup-script.sh` wired up as its boot
startup script, so Docker installs itself automatically (~1-2 min after
first boot).

pgAdmin (5480) is open to the internet — it still doesn't start with a plain
`docker compose up` (behind the `infra-management` profile, see step 3), but
once you bring it up it's reachable at `http://<STATIC_IP>:5480` from
anywhere. Its default login is `admin@ecommerce.local` / `admin`
(`PGADMIN_DEFAULT_EMAIL`/`PGADMIN_DEFAULT_PASSWORD` in `docker-compose.yml`)
— change `PGADMIN_PASSWORD` in `.env` before relying on this, since it's a
full database browser/query tool. Kafka UI (9280) is not opened — reach it
via SSH tunneling instead if you bring it up (see step 3).

Postgres (5432) being open to the internet means whatever's in `DB_USERNAME`
/ `DB_PASSWORD` in your `.env` is your only line of defense — change them
from the repo defaults (`khoidev`/`khoicktv`) before filling in `.env` in
step 2, or narrow the `allow-postgres` rule's `--source-ranges` in
`create-vm.sh` to your own IP instead of `0.0.0.0/0`.

SSH in once it's up:

```bash
gcloud compute ssh ecommerce-backend --zone=asia-southeast1-c --project=ecommerce-system-503610
```

If `docker --version` doesn't work yet, the startup script is still running —
wait a minute and retry, or run it by hand:
`sudo bash deploy/gcp/startup-script.sh` (after step 2 below gets the repo
onto the VM).

## 1. Docker / Docker Compose

Installed by step 0's startup script. To verify (on the VM):

```bash
docker --version && docker compose version
```

Docker itself being installed doesn't mean your SSH user can run it yet —
that needs one-time group membership, done from your own account (on the
VM):

```bash
sudo usermod -aG docker $USER
newgrp docker
```

Without this, `docker compose` commands fail with `permission denied while
trying to connect to the docker API at unix:///var/run/docker.sock`.
`newgrp docker` applies it to your current session immediately; a fresh SSH
login would also pick it up, but doesn't require one.

## 2. Get the code onto the VM

The 4 core services, 3 infra configs (database/kafka/pgadmin), and
scenario-seeder are separate private repos under `git@github.com:HK251-DATN/*`,
so the VM needs SSH access to GitHub.

**On the VM**, generate a keypair and register it:

```bash
ssh-keygen -t ed25519 -C "ecommerce-backend-vm" -f ~/.ssh/id_ed25519 -N ""
cat ~/.ssh/id_ed25519.pub
```

Add that public key to GitHub — either:
- **Your personal GitHub account** → Settings → SSH and GPG keys (simplest,
  if your account already has read access to all `HK251-DATN` repos), or
- **Per-repo deploy keys** (read-only, tighter scope) on each of the 8 repos
  cloned below, if you'd rather not put a VM key on your personal account.

Then clone the whole-system repo and the trimmed set of sub-repos:

```bash
git clone git@github.com:HK251-DATN/whole-system.git
cd whole-system
./deploy/gcp/clone-backend-repos.sh
```

Create `.env` (not committed — copy your local one over, or fill it in by
hand):

```bash
cp .env.example .env
nano .env   # fill in DB_USERNAME/PASSWORD, R2_*, GOONG_API_KEY
```

## 3. Deploy the trimmed stack

```bash
docker compose -f docker-compose.yml -f deploy/gcp/compose.prod.yml up -d --build
```

`deploy/gcp/compose.prod.yml` swaps each service's build to the plain
`Dockerfile` (self-contained Maven multi-stage build — no JDK/Maven needed on
the VM, unlike `Dockerfile.local`), caps JVM/Kafka heap sizes, and adds the
Caddy reverse proxy. No `--profile` flag means search/frontend/seed/
infra-management stay off — that's Postgres, Kafka, and the 4 core services
only.

Need pgAdmin or Kafka UI (e.g. to inspect data)? Bring them up separately:

```bash
docker compose --profile infra-management up -d pgadmin kafka-ui
```

pgAdmin is open to the internet (see step 0's security note) — just visit
`http://<STATIC_IP>:5480`. Kafka UI isn't opened, so reach it by SSH tunnel:

```bash
# from your own machine — repeat --ssh-flag per -L, packing both into one
# flag value doesn't reliably split. If this hangs or the forward silently
# doesn't apply, you likely have another gcloud ssh session already open to
# this VM (it reuses a multiplexed connection); close it and retry, or add
# --ssh-flag="-o ControlMaster=no" --ssh-flag="-o ControlPath=none".
gcloud compute ssh ecommerce-backend --zone=asia-southeast1-c --project=ecommerce-system-503610 \
  --ssh-flag="-L 9280:localhost:9280"
# then open http://localhost:9280 (Kafka UI) locally
```

Check health:

```bash
docker compose ps
curl -s http://localhost:9000/actuator/health/readiness
```

From outside, the services are reachable directly at
`http://<STATIC_IP>:9000` / `:9100` / `:9200` / `:9300`, and via the reverse
proxy at `http://<STATIC_IP>/identity/...`, `/back-office/...`,
`/product-storage/...`, `/ecommerce/...`.

To seed scenario data afterward (optional — `tools/scenario-seeder` is already
cloned by step 2):

```bash
docker compose --profile seed run --rm scenario-seeder all
```

## 4. Reverse proxy + TLS

`deploy/caddy/Caddyfile` already runs as part of step 3 (the `caddy` service
in `deploy/gcp/compose.prod.yml`), listening on `:80` with path-based routing
to the 4 services. No domain yet, so it's HTTP-only for now.

**Once you have a domain** pointed at the static IP (an A record), switch it
on:

1. Edit `deploy/caddy/Caddyfile`: change `:80 {` to `api.yourdomain.com {`.
2. `docker compose -f docker-compose.yml -f deploy/gcp/compose.prod.yml up -d caddy`

Caddy automatically obtains and renews a Let's Encrypt cert on :443 — no
other config needed. Port 443 is already open in the firewall from step 0.

## Updating / redeploying

```bash
git pull   # in whole-system and in each services/* / infrastructure/* repo
docker compose -f docker-compose.yml -f deploy/gcp/compose.prod.yml up -d --build
```

## Teardown

```bash
gcloud compute instances delete ecommerce-backend --zone=asia-southeast1-c --project=ecommerce-system-503610
gcloud compute addresses delete ecommerce-backend-ip --region=asia-southeast1 --project=ecommerce-system-503610
gcloud compute firewall-rules delete ecommerce-backend-allow-ssh ecommerce-backend-allow-http-https ecommerce-backend-allow-core-services ecommerce-backend-allow-postgres ecommerce-backend-allow-pgadmin --project=ecommerce-system-503610
```

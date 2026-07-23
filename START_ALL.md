# Starting the Whole System

`linux/start-all.sh` (Linux/Mac) and `windows/start-all.bat` (Windows) are
one-shot scripts that build the service JARs, bring up all backend containers,
seed scenario data, and optionally start the frontend UIs — all with one
command. Run them from the repo root.

Every scenario below shows the Linux form; on Windows swap
`./linux/start-all.sh` for `windows\start-all.bat`.

## Scenarios

| I want to... | Command |
|---|---|
| Start the system, backend only (frontends run locally via `npm run dev`) | `./linux/start-all.sh` |
| Start the system **with** the frontend UIs as containers too | `./linux/start-all.sh --enable-front-end` |
| First time ever — repos aren't cloned yet | `./linux/start-all.sh --with-setup-repos` |
| Restart, but skip the Maven build (JARs already built) | `./linux/start-all.sh --skip-build` |
| Restart, but skip re-seeding scenario data (already seeded) | `./linux/start-all.sh --skip-seed` |
| Replace an already-running system with a clean one (keeps DB/Kafka data) | `./linux/start-all.sh --fresh` |
| Replace an already-running system **and** wipe all data (Postgres, Kafka, pgAdmin, Elasticsearch) | `./linux/start-all.sh --wipe-data` |
| Same as above, but skip the "type yes" confirmation (e.g. scripted use) | `./linux/start-all.sh --wipe-data -y` |
| Fresh restart with frontends, without re-seeding | `./linux/start-all.sh --fresh --enable-front-end --skip-seed` |
| Just see all available flags | `./linux/start-all.sh --help` |

Flags can be combined freely.

## What it does, step by step

1. **(optional)** Clone all repos — `--with-setup-repos`
2. Pre-flight checks: Docker is running, `docker compose` is available, `.env`
   exists (created from `.env.example` if missing — you'll need to fill in
   Cloudflare R2 credentials and re-run)
3. **(optional)** Tear down an already-running system — `--fresh` /
   `--wipe-data`
4. Build service JARs (`linux/build-local.sh`) — skipped with `--skip-build`
5. `docker compose up -d --build` — Postgres, Kafka (+ UI), pgAdmin,
   Elasticsearch, and the 4 core microservices + search-chat-service
6. Seed scenario data via `scenario-seeder` — skipped with `--skip-seed`
7. **(optional)** Build and start the frontend containers — `--enable-front-end`
   (ecommerce-ui on :3000, back-office-ui on :5173, provider-ui on :5273)

## Notes

- Without `--fresh`/`--wipe-data`, the script only does what
  `docker compose up -d --build` does by default: it reconciles the existing
  system in place rather than tearing it down first.
- `--wipe-data` is destructive — it deletes named volumes, so all database and
  Kafka data is lost. It always asks for a typed `yes` unless `-y`/`--yes` is
  passed.
- `--enable-front-end` is experimental: each backend service only accepts CORS
  requests from an allow-listed set of origins (see each service's
  `WebConfig.java`). If a containerized UI can't reach a backend, check that
  its origin (`http://localhost:<ui-port>`) is in that service's list.
- The default dev workflow for frontends is still `cd frontend/<app> && npm
  run dev` — `--enable-front-end` is for when you want them running as
  containers instead (e.g. to test the built/served version).

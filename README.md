# k8s-helm-observability-starter

Minimal but production-flavoured Kubernetes + Helm starter project.

## What this repo demonstrates (skills)
- **Helm charting**: templating, values, helpers, sane defaults
- **Kubernetes readiness**: liveness/readiness probes, requests/limits, config via ConfigMap
- **Autoscaling scaffold**: optional HPA template
- **Observability mindset**: `/metrics` endpoint + Prometheus scrape annotations
- **Runbooks**: how to debug common issues
- **CI**: `helm lint` + `helm template` on every push/PR

## Repo layout
- `app/` - tiny Java 14 HTTP service (health + readiness + metrics)
- `charts/hello-svc/` - Helm chart to deploy the service
- `docs/runbooks/` - operational runbook
- `.github/workflows/` - GitHub Actions CI

## Local build (app)
```bash
docker build -t hello-svc:local ./app
```

## Helm quickstart (render + lint)
```bash
helm lint charts/hello-svc
helm template hello charts/hello-svc --namespace demo
```

## Deploy to a cluster (kind/minikube/etc.)
1) Load/push the image to your cluster (method depends on kind/minikube/registry).
2) Install:
```bash
helm upgrade --install hello charts/hello-svc --namespace demo --create-namespace \
  --set image.repository=hello-svc \
  --set image.tag=local \
  --set image.pullPolicy=IfNotPresent
```

## Endpoints (inside cluster)
- `/healthz` - liveness
- `/readyz` - readiness
- `/metrics` - simple Prometheus-style text

See `docs/runbooks/hello-svc.md` for debugging tips.

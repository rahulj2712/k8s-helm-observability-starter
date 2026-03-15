
 # hello-svc runbook

## Symptoms and checks

### Pods are CrashLoopBackOff
- Check logs:
  ```bash
  kubectl -n <ns> logs deploy/<release>-hello-svc
  ```
- Describe pod to see probe failures / OOMKilled:
  ```bash
  kubectl -n <ns> describe pod <pod-name>
  ```

### Readiness probe failing (service not receiving traffic)
- Check endpoint inside pod:
  ```bash
  kubectl -n <ns> exec -it <pod-name> -- sh
  wget -qO- http://localhost:8080/readyz
  ```
- If you intentionally want slower readiness, tune:
  - `env.READY_DELAY_MS` in `values.yaml`

### CPU/memory throttling
- Review requests/limits in `values.yaml`
- Check resource usage:
  ```bash
  kubectl -n <ns> top pod
  ```

### Prometheus scraping not working
- Confirm pod annotations exist:
  ```bash
  kubectl -n <ns> get pod <pod-name> -o yaml | grep -A3 prometheus.io
  ```
- Confirm `/metrics` is reachable:
  ```bash
  kubectl -n <ns> port-forward deploy/<release>-hello-svc 18080:8080
  curl -s http://localhost:18080/metrics
  ```

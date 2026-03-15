
 package com.rahulj.hellosvc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
  private static final AtomicBoolean READY = new AtomicBoolean(false);
  private static final AtomicLong REQUESTS = new AtomicLong(0);
  private static final Instant STARTED_AT = Instant.now();

  public static void main(String[] args) throws Exception {
    int port = Integer.parseInt(env("PORT", "8080"));
    int threads = Integer.parseInt(env("HTTP_THREADS", "8"));

    // Simulate "startup warmup" before readiness turns true (configurable)
    long readyDelayMs = Long.parseLong(env("READY_DELAY_MS", "1000"));

    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.setExecutor(Executors.newFixedThreadPool(threads));

    server.createContext("/", ex -> {
      REQUESTS.incrementAndGet();
      respondJson(ex, 200, "{\"service\":\"hello-svc\",\"ok\":true}");
    });

    server.createContext("/healthz", ex -> respondText(ex, 200, "ok\n"));

    server.createContext("/readyz", ex -> {
      if (READY.get()) respondText(ex, 200, "ready\n");
      else respondText(ex, 503, "not-ready\n");
    });

    server.createContext("/metrics", ex -> {
      // Very small Prometheus-style exposition (text)
      String body =
        "# HELP hello_svc_requests_total Total HTTP requests handled\n" +
        "# TYPE hello_svc_requests_total counter\n" +
        "hello_svc_requests_total " + REQUESTS.get() + "\n" +
        "# HELP hello_svc_uptime_seconds Service uptime in seconds\n" +
        "# TYPE hello_svc_uptime_seconds gauge\n" +
        "hello_svc_uptime_seconds " + (Instant.now().getEpochSecond() - STARTED_AT.getEpochSecond()) + "\n";
      ex.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4");
      respondRaw(ex, 200, body);
    });

    server.start();

    // Flip readiness after warm-up
    new Thread(() -> {
      try {
        Thread.sleep(readyDelayMs);
        READY.set(true);
      } catch (InterruptedException ignored) {}
    }).start();
  }

  private static String env(String key, String def) {
    String v = System.getenv(key);
    return (v == null || v.isBlank()) ? def : v;
  }

  private static void respondJson(HttpExchange ex, int code, String json) throws IOException {
    ex.getResponseHeaders().add("Content-Type", "application/json");
    respondRaw(ex, code, json + "\n");
  }

  private static void respondText(HttpExchange ex, int code, String text) throws IOException {
    ex.getResponseHeaders().add("Content-Type", "text/plain");
    respondRaw(ex, code, text);
  }

  private static void respondRaw(HttpExchange ex, int code, String body) throws IOException {
    byte[] bytes = body.getBytes();
    ex.sendResponseHeaders(code, bytes.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(bytes);
    } finally {
      ex.close();
    }
  }
}

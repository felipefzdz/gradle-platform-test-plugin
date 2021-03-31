package com.felipefzdz.base.infrastructure;

import com.felipefzdz.base.extension.Probe;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Optional;

import static com.felipefzdz.base.infrastructure.RetryStrategy.delayRetry;

public class HttpProbe {

    public static HttpProbeStatus run(Probe probe) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .setConnectTimeout(5 * 1000)
                .setSocketTimeout(60 * 1000)
                .build();

        final Integer retries = probe.getRetries().get();
        final Integer delay = probe.getDelay().get();
        final Integer status = probe.getStatus().get();
        CloseableHttpClient httpClient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler((e, executionCount, httpContext) -> {
                    System.out.println("Retrying since the response was: " + e.getMessage());
                    return executionCount <= retries && delayRetry(delay);
                })
                .setServiceUnavailableRetryStrategy(new RetryStrategy(retries, delay, status))
                .build();
        try {
            httpClient.execute(new HttpGet("http://localhost:" + probe.getPort().get() + "" + probe.getPath().get()));
            return HttpProbeStatus.success();
        } catch (IOException e) {
            return HttpProbeStatus.failure(e);
        }
    }

    public static class HttpProbeStatus {
        public final boolean failure;
        public final Optional<Exception> maybeException;

        private HttpProbeStatus(boolean failure, Optional<Exception> maybeException) {
            this.failure = failure;
            this.maybeException = maybeException;
        }

        public static HttpProbeStatus success() {
            return new HttpProbeStatus(false, Optional.empty());
        }

        public static HttpProbeStatus failure(Exception e) {
            return new HttpProbeStatus(true, Optional.of(e));
        }

    }
}

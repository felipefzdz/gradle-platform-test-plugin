package com.felipefzdz.kubernetes.infrastructure;


import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

public final class RetryStrategy implements ServiceUnavailableRetryStrategy {

    private final Integer retries;
    private final Integer delay;
    private final Integer status;

    public RetryStrategy(Integer retries, Integer delay, Integer status) {
        this.retries = retries;
        this.delay = delay;
        this.status = status;
    }

    @Override
    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
        System.out.println("Potentially retrying since the response was: " + response.getStatusLine().getStatusCode());
        return response.getStatusLine().getStatusCode() != status && executionCount < retries && delayRetry(delay);
    }

    @Override
    public long getRetryInterval() {
        return 0;
    }

    public static boolean delayRetry(Integer delay) {
        try {
            Thread.sleep(delay * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}

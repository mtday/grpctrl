package com.grpctrl.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.client.error.ClientException;
import com.grpctrl.common.model.ApiLogin;
import com.grpctrl.common.model.EndPoint;
import com.grpctrl.rest.resource.v1.status.AccountStatusResponse;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides remote access over REST to the back-end server for account status.
 */
public class AccountStatusClient {
    private static final String API = "api/v1/status";

    @Nonnull
    private final ObjectMapper objectMapper;

    @Nonnull
    private final OkHttpClient httpClient;

    @Nonnull
    private final EndPoint endPoint;

    public AccountStatusClient(
            @Nonnull final ObjectMapper objectMapper, @Nonnull final OkHttpClient httpClient,
            @Nonnull final EndPoint endPoint) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.endPoint = Objects.requireNonNull(endPoint);
    }

    @Nonnull
    private String getEndPointUrl() {
        return this.endPoint.asUrl() + API;
    }

    public AccountStatusResponse get(@Nonnull final ApiLogin apiLogin) throws ClientException {
        try {
            final Request request = new Request.Builder().url(getEndPointUrl())
                    .header(apiLogin.getHeaderKey(), apiLogin.getHeaderValue()).get().build();
            final Response response = this.httpClient.newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    return this.objectMapper.readValue(response.body().byteStream(), AccountStatusResponse.class);
                default:
                    throw new ClientException(
                            "Response code " + response.code() + " with body: " + response.body().string());
            }
        } catch (final IOException ioException) {
            throw new ClientException("Failed to communicate with back-end server", ioException);
        }
    }
}

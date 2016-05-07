package com.grpctrl.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.client.error.ClientException;
import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.EndPoint;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides remote access over REST to the back-end server for account management.
 */
public class AccountClient {
    //private static final MediaType POST_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String API = "api/v1/account";

    @Nonnull
    private final ObjectMapper objectMapper;

    @Nonnull
    private final OkHttpClient httpClient;

    @Nonnull
    private final EndPoint endPoint;

    /**
     * @param objectMapper the {@link ObjectMapper} responsible for performing JSON serialization and deserialization
     * @param httpClient the {@link OkHttpClient} responsible for performing HTTP communication with the remote service
     * @param endPoint the {@link EndPoint} representing the remote service to communicate with
     */
    public AccountClient(
            @Nonnull final ObjectMapper objectMapper, @Nonnull final OkHttpClient httpClient,
            @Nonnull final EndPoint endPoint) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.endPoint = Objects.requireNonNull(endPoint);
    }

    @Nonnull
    private ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Nonnull
    private OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    @Nonnull
    private EndPoint getEndPoint() {
        return this.endPoint;
    }

    @Nonnull
    private String getEndPointUrl() {
        return getEndPoint().asUrl() + API;
    }

    public void get(final int accountId, @Nonnull final Consumer<Account> consumer) throws ClientException {
        try {
            final Request request = new Request.Builder().url(getEndPointUrl() + "/" + accountId).get().build();
            final Response response = getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    try {
                        final JsonParser jsonParser =
                                getObjectMapper().getFactory().createParser(response.body().byteStream());
                        jsonParser.nextToken(); // Start array.
                        jsonParser.nextToken(); // Start object.
                        final Iterator<Account> iter = jsonParser.readValuesAs(Account.class);
                        while (iter.hasNext()) {
                            consumer.accept(iter.next());
                        }
                    } catch (final IOException ioException) {
                        throw new ClientException("Failed to parse response data from server", ioException);
                    }
                    break;
                default:
                    throw new ClientException(
                            "Response code " + response.code() + " with body: " + response.body().string());
            }
        } catch (final IOException ioException) {
            throw new ClientException("Failed to communicate with back-end server", ioException);
        }
    }

    public void getAll(@Nonnull final Consumer<Account> consumer) throws ClientException {
        try {
            final Request request = new Request.Builder().url(getEndPointUrl()).get().build();
            final Response response = getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    try {
                        final JsonParser jsonParser =
                                getObjectMapper().getFactory().createParser(response.body().byteStream());
                        jsonParser.nextToken(); // Start array.
                        jsonParser.nextToken(); // Start object.
                        final Iterator<Account> iter = jsonParser.readValuesAs(Account.class);
                        while (iter.hasNext()) {
                            consumer.accept(iter.next());
                        }
                    } catch (final IOException ioException) {
                        throw new ClientException("Failed to parse response data from server", ioException);
                    }
                    break;
                default:
                    throw new ClientException(
                            "Response code " + response.code() + " with body: " + response.body().string());
            }
        } catch (final IOException ioException) {
            throw new ClientException("Failed to communicate with back-end server", ioException);
        }
    }

    public void remove(final long id) throws ClientException {
        try {
            final Request request = new Request.Builder().url(getEndPointUrl() + "/" + id).delete().build();
            final Response response = getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    // We don't really care what the response is, but we need to read it so the connection can close.
                    response.body().string();
                    break;
                default:
                    throw new ClientException(
                            "Response code " + response.code() + " with body: " + response.body().string());
            }
        } catch (final IOException ioException) {
            throw new ClientException("Failed to communicate with back-end server", ioException);
        }
    }
}

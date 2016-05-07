package com.grpctrl.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
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

    public void get(final long accountId, @Nonnull final Consumer<Account> consumer) throws ClientException {
        try {
            final Request request = new Request.Builder().url(getEndPointUrl() + "/" + accountId).get().build();
            final Response response = getHttpClient().newCall(request).execute();
            switch (response.code()) {
                case HttpServletResponse.SC_OK:
                    try {
                        final JsonParser jsonParser =
                                getObjectMapper().getFactory().createParser(response.body().byteStream());
                        final JsonToken startObj = jsonParser.nextToken();
                        Preconditions.checkArgument(startObj == JsonToken.START_OBJECT);
                        final JsonToken successField = jsonParser.nextToken();
                        Preconditions.checkArgument(successField == JsonToken.FIELD_NAME);
                        Preconditions.checkArgument("success".equals(jsonParser.getCurrentName()));
                        final JsonToken successValue = jsonParser.nextToken();
                        Preconditions.checkArgument(
                                successValue == JsonToken.VALUE_TRUE || successValue == JsonToken.VALUE_FALSE);
                        final boolean success = jsonParser.getBooleanValue();
                        if (success) {
                            final JsonToken accountField = jsonParser.nextToken();
                            Preconditions.checkArgument(accountField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("account".equals(jsonParser.getCurrentName()));
                            final JsonToken accountValue = jsonParser.nextToken();
                            Preconditions.checkArgument(accountValue == JsonToken.START_OBJECT);

                            consumer.accept(jsonParser.readValueAs(Account.class));
                        } else {
                            final JsonToken codeField = jsonParser.nextToken();
                            Preconditions.checkArgument(codeField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("code".equals(jsonParser.getCurrentName()));
                            final JsonToken codeValue = jsonParser.nextToken();
                            Preconditions.checkArgument(codeValue == JsonToken.VALUE_NUMBER_INT);
                            final int code = jsonParser.getIntValue();

                            final JsonToken messageField = jsonParser.nextToken();
                            Preconditions.checkArgument(messageField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("message".equals(jsonParser.getCurrentName()));
                            final JsonToken messageValue = jsonParser.nextToken();
                            Preconditions.checkArgument(messageValue == JsonToken.VALUE_STRING);
                            final String message = jsonParser.getValueAsString();

                            throw new ClientException("Failed with code " + code + " and message: " + message);
                        }
                    } catch (final Exception exception) {
                        throw new ClientException("Failed to parse response data from server", exception);
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
                        final JsonToken startObj = jsonParser.nextToken();
                        Preconditions.checkArgument(startObj == JsonToken.START_OBJECT);
                        final JsonToken successField = jsonParser.nextToken();
                        Preconditions.checkArgument(successField == JsonToken.FIELD_NAME);
                        Preconditions.checkArgument("success".equals(jsonParser.getCurrentName()));
                        final JsonToken successValue = jsonParser.nextToken();
                        Preconditions.checkArgument(
                                successValue == JsonToken.VALUE_TRUE || successValue == JsonToken.VALUE_FALSE);
                        final boolean success = jsonParser.getBooleanValue();
                        if (success) {
                            final JsonToken accountsField = jsonParser.nextToken();
                            Preconditions.checkArgument(accountsField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("accounts".equals(jsonParser.getCurrentName()));
                            final JsonToken accountsArray = jsonParser.nextToken();
                            Preconditions.checkArgument(accountsArray == JsonToken.START_ARRAY);
                            final JsonToken firstAccount = jsonParser.nextToken();
                            Preconditions.checkArgument(firstAccount == JsonToken.START_OBJECT);
                            final Iterator<Account> iter = jsonParser.readValuesAs(Account.class);
                            while (iter.hasNext()) {
                                consumer.accept(iter.next());
                            }
                        } else {
                            final JsonToken codeField = jsonParser.nextToken();
                            Preconditions.checkArgument(codeField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("code".equals(jsonParser.getCurrentName()));
                            final JsonToken codeValue = jsonParser.nextToken();
                            Preconditions.checkArgument(codeValue == JsonToken.VALUE_NUMBER_INT);
                            final int code = jsonParser.getIntValue();

                            final JsonToken messageField = jsonParser.nextToken();
                            Preconditions.checkArgument(messageField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("message".equals(jsonParser.getCurrentName()));
                            final JsonToken messageValue = jsonParser.nextToken();
                            Preconditions.checkArgument(messageValue == JsonToken.VALUE_STRING);
                            final String message = jsonParser.getValueAsString();

                            throw new ClientException("Failed with code " + code + " and message: " + message);
                        }
                    } catch (final Exception exception) {
                        throw new ClientException("Failed to parse response data from server", exception);
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
                    try {
                        final JsonParser jsonParser =
                                getObjectMapper().getFactory().createParser(response.body().byteStream());
                        final JsonToken startObj = jsonParser.nextToken();
                        Preconditions.checkArgument(startObj == JsonToken.START_OBJECT);
                        final JsonToken successField = jsonParser.nextToken();
                        Preconditions.checkArgument(successField == JsonToken.FIELD_NAME);
                        Preconditions.checkArgument("success".equals(jsonParser.getCurrentName()));
                        final JsonToken successValue = jsonParser.nextToken();
                        Preconditions.checkArgument(
                                successValue == JsonToken.VALUE_TRUE || successValue == JsonToken.VALUE_FALSE);
                        final boolean success = jsonParser.getBooleanValue();
                        if (!success) {
                            final JsonToken codeField = jsonParser.nextToken();
                            Preconditions.checkArgument(codeField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("code".equals(jsonParser.getCurrentName()));
                            final JsonToken codeValue = jsonParser.nextToken();
                            Preconditions.checkArgument(codeValue == JsonToken.VALUE_NUMBER_INT);
                            final int code = jsonParser.getIntValue();

                            final JsonToken messageField = jsonParser.nextToken();
                            Preconditions.checkArgument(messageField == JsonToken.FIELD_NAME);
                            Preconditions.checkArgument("message".equals(jsonParser.getCurrentName()));
                            final JsonToken messageValue = jsonParser.nextToken();
                            Preconditions.checkArgument(messageValue == JsonToken.VALUE_STRING);
                            final String message = jsonParser.getValueAsString();

                            throw new ClientException("Failed with code " + code + " and message: " + message);
                        }
                    } catch (final Exception exception) {
                        throw new ClientException("Failed to parse response data from server", exception);
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
}

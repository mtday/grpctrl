package com.grpctrl.security.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserEmail;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.typesafe.config.Config;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class EmailLookup implements Callable<Void> {
    @Nonnull
    private final ConfigSupplier configSupplier;
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final OAuth20ServiceSupplier oAuth20ServiceSupplier;
    @Nonnull
    private final String code;
    @Nonnull
    private final User user;

    public EmailLookup(
            @Nonnull final ConfigSupplier configSupplier, @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final OAuth20ServiceSupplier oAuth20ServiceSupplier, @Nonnull final String code,
            @Nonnull final User user) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.oAuth20ServiceSupplier = Objects.requireNonNull(oAuth20ServiceSupplier);
        this.code = Objects.requireNonNull(code);
        this.user = Objects.requireNonNull(user);
    }

    @Override
    public Void call() throws Exception {
        final Config config = this.configSupplier.get();
        final OAuth20Service oAuth20Service = this.oAuth20ServiceSupplier.get();
        final OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(this.code);

        final String userUrl = config.getString(ConfigKeys.AUTH_API_RESOURCE_EMAIL.getKey());
        final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, userUrl, oAuth20Service);
        oAuthRequest.addHeader("Accept", config.getString(ConfigKeys.AUTH_API_ACCEPT.getKey()));
        oAuth20Service.signRequest(accessToken, oAuthRequest);

        try {
            final Response response = oAuthRequest.send();
            if (response.isSuccessful()) {
                // Set the emails in our user account.
                final ObjectMapper objectMapper = this.objectMapperSupplier.get();
                final List<UserEmail> emails = objectMapper.readValue(
                        response.getBody(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, UserEmail.class));
                this.user.setEmails(emails.stream().filter(ue -> ue.getEmail().contains("@users.noreply.github.com"))
                        .collect(Collectors.toList()));
            } else {
                throw new IOException("Failed to fetch user emails from github, response was: " +
                        response.getCode() + " => " + response.getMessage());
            }
        } catch (final IOException ioException) {
            throw new RuntimeException("Failed to retrieve or parse user emails", ioException);
        }
        return null;
    }
}

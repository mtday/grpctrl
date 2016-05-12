package com.grpctrl.security.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.model.User;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.typesafe.config.Config;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

public class LoginLookup implements Callable<Void> {
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

    public LoginLookup(
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

        final String userUrl = config.getString(ConfigKeys.AUTH_API_RESOURCE_USER.getKey());
        final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, userUrl, oAuth20Service);
        oAuthRequest.addHeader("Accept", config.getString(ConfigKeys.AUTH_API_ACCEPT.getKey()));
        oAuth20Service.signRequest(accessToken, oAuthRequest);

        try {
            final Response response = oAuthRequest.send();
            if (response.isSuccessful()) {
                // Set the login value on our user account.
                final ObjectMapper objectMapper = this.objectMapperSupplier.get();
                this.user.setLogin(objectMapper.readValue(response.getBody(), User.class).getLogin());
            } else {
                throw new IOException("Failed to fetch user login from github, response was: " +
                        response.getCode() + " => " + response.getMessage());
            }
        } catch (final IOException ioException) {
            throw new RuntimeException("Failed to retrieve or parse user login", ioException);
        }
        return null;
    }
}

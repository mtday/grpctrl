package com.grpctrl.run;

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

import org.eclipse.jetty.jaas.spi.UserInfo;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.ServletRequest;

public class CustomCallbackHandler implements CallbackHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CustomCallbackHandler.class);

    @Nonnull
    private final ConfigSupplier configSupplier;
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final OAuth20ServiceSupplier oAuth20ServiceSupplier;
    @Nonnull
    private final ServletRequest servletRequest;

    public CustomCallbackHandler(
            @Nonnull final ConfigSupplier configSupplier, @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final OAuth20ServiceSupplier oAuth20ServiceSupplier,
            @Nonnull final ServletRequest servletRequest) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.oAuth20ServiceSupplier = Objects.requireNonNull(oAuth20ServiceSupplier);
        this.servletRequest = Objects.requireNonNull(servletRequest);
    }

    @Nonnull
    public ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    @Nonnull
    public ObjectMapperSupplier getObjectMapperSupplier() {
        return this.objectMapperSupplier;
    }

    @Nonnull
    public OAuth20ServiceSupplier getOAuth20ServiceSupplier() {
        return this.oAuth20ServiceSupplier;
    }

    @Nonnull
    public ServletRequest getServletRequest() {
        return this.servletRequest;
    }

    @Override
    public void handle(@Nonnull final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (final Callback callback : callbacks) {
            LOG.info("Handling callback: {}", callback);
            if (callback instanceof CustomCallback) {
                ((CustomCallback) callback)
                        .populate(getConfigSupplier(), getObjectMapperSupplier(), getOAuth20ServiceSupplier(),
                                getServletRequest());
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    public Optional<String> getUserName() {
        return Optional.ofNullable(getServletRequest().getParameter("code"));
    }

    public static class CustomCallback implements Callback {
        private String code;
        private String user;
        private List<String> roles = new ArrayList<>();

        public CustomCallback() {
        }

        public void populate(
                @Nonnull final ConfigSupplier configSupplier, @Nonnull final ObjectMapperSupplier objectMapperSupplier,
                @Nonnull final OAuth20ServiceSupplier oAuth20ServiceSupplier,
                @Nonnull final ServletRequest servletRequest) throws IOException {
            LOG.info("Populating callback");
            this.code = servletRequest.getParameter("code");

            if (this.code != null) {
                final OAuth20Service oAuth20Service = oAuth20ServiceSupplier.get();
                final OAuth2AccessToken accessToken = oAuth20Service.getAccessToken(this.code);

                final Config config = configSupplier.get();
                final ObjectMapper objectMapper = objectMapperSupplier.get();

                { // Fetch user information
                    final String userUrl = config.getString(ConfigKeys.AUTH_API_RESOURCE_USER.getKey());
                    final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, userUrl, oAuth20Service);
                    oAuthRequest.addHeader("Accept", config.getString(ConfigKeys.AUTH_API_ACCEPT.getKey()));
                    oAuth20Service.signRequest(accessToken, oAuthRequest);

                    final Response response = oAuthRequest.send();
                    LOG.info("  User Response code: {}", response.getCode());
                    LOG.info("  User Response body: {}", response.getBody());
                    LOG.info("  User Response headers: {}", response.getHeaders());
                    LOG.info("  User Response message: {}", response.getMessage());

                    final User user = objectMapper.readValue(response.getBody(), User.class);
                    LOG.info("User: {}", user);

                    this.user = user.getLogin();
                }

                { // Fetch user emails
                    final String emailUrl = config.getString(ConfigKeys.AUTH_API_RESOURCE_EMAIL.getKey());
                    final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, emailUrl, oAuth20Service);
                    oAuthRequest.addHeader("Accept", config.getString(ConfigKeys.AUTH_API_ACCEPT.getKey()));
                    oAuth20Service.signRequest(accessToken, oAuthRequest);

                    final Response response = oAuthRequest.send();
                    LOG.info("  Email Response code: {}", response.getCode());
                    LOG.info("  Email Response body: {}", response.getBody());
                    LOG.info("  Email Response headers: {}", response.getHeaders());
                    LOG.info("  Email Response message: {}", response.getMessage());

                    final List<UserEmail> userEmails = objectMapper.readValue(response.getBody(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, UserEmail.class));

                    LOG.info("User Emails: {}", userEmails);
                }
            }
        }

        @Nonnull
        public UserInfo getUserInfo() {
            return new UserInfo(this.user, new OpenCredential(), this.roles);
        }
    }

    private static class OpenCredential extends Credential {
        private static final long serialVersionUID = 1129878263L;

        @Override
        public boolean check(@Nonnull final Object credentials) {
            return true;
        }
    }
}

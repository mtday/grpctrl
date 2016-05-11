package com.grpctrl.common.supplier;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.grpctrl.common.config.ConfigKeys;
import com.typesafe.config.Config;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.net.URI;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to the OAuth access service.
 */
@Provider
public class OAuth20ServiceSupplier
        implements Supplier<OAuth20Service>, Factory<OAuth20Service>, ContextResolver<OAuth20Service> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nullable
    private volatile OAuth20Service singleton;

    @Inject
    public OAuth20ServiceSupplier(@Nonnull final ConfigSupplier configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    @Nonnull
    public ConfigSupplier getConfigSupplier() {
        return this.configSupplier;
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public OAuth20Service get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (OAuth20ServiceSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public OAuth20Service getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public OAuth20Service provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final OAuth20Service oAuth20Service) {
        // No need to do anything here.
    }

    @Nonnull
    private OAuth20Service create() {
        final Config config = getConfigSupplier().get();
        final String apiKey = config.getString(ConfigKeys.AUTH_API_KEY.getKey());
        final String apiSecret = config.getString(ConfigKeys.AUTH_API_SECRET.getKey());
        final String apiScope = config.getString(ConfigKeys.AUTH_API_SCOPE.getKey());
        final String baseUrl = config.getString(ConfigKeys.SYSTEM_BASE_URL.getKey());
        final URI callbackUri =
                UriBuilder.fromUri(baseUrl).path(config.getString(ConfigKeys.AUTH_API_CALLBACK.getKey())).build();

        return new ServiceBuilder().apiKey(apiKey).apiSecret(apiSecret).scope(apiScope).callback(callbackUri.toString())
                .build(GitHubApi.instance());
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(OAuth20ServiceSupplier.class).to(OAuth20ServiceSupplier.class).in(Singleton.class);
            bindFactory(OAuth20ServiceSupplier.class).to(OAuth20Service.class).in(Singleton.class);
        }
    }
}

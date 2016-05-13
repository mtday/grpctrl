package com.grpctrl.client;

import com.grpctrl.common.config.ConfigKeys;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.crypto.ssl.SslContextSupplier;

import okhttp3.OkHttpClient;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to an executor service.
 */
@Provider
public class OkHttpClientSupplier
        implements Supplier<OkHttpClient>, Factory<OkHttpClient>, ContextResolver<OkHttpClient> {
    @Nonnull
    private final ConfigSupplier configSupplier;

    @Nonnull
    private final SslContextSupplier sslContextSupplier;

    @Nullable
    private volatile OkHttpClient singleton = null;

    /**
     * @param configSupplier provides access to the static system configuration properties
     * @param sslContextSupplier the {@link SslContextSupplier} responsible for providing
     *     {@link javax.net.ssl.SSLContext} instances
     */
    @Inject
    public OkHttpClientSupplier(
            @Nonnull final ConfigSupplier configSupplier, @Nonnull final SslContextSupplier sslContextSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.sslContextSupplier = Objects.requireNonNull(sslContextSupplier);
    }

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public OkHttpClient get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (OkHttpClientSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public OkHttpClient getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public OkHttpClient provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final OkHttpClient httpClient) {
        // Nothing to do.
    }

    private long getTimeoutMillis(@Nonnull final ConfigKeys key) {
        return this.configSupplier.get().getDuration(key.getKey(), TimeUnit.MILLISECONDS);
    }

    @Nonnull
    private OkHttpClient create() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // Timeouts
        builder.connectTimeout(getTimeoutMillis(ConfigKeys.CLIENT_TIMEOUT_CONNECT), TimeUnit.MILLISECONDS);
        builder.readTimeout(getTimeoutMillis(ConfigKeys.CLIENT_TIMEOUT_READ), TimeUnit.MILLISECONDS);
        builder.writeTimeout(getTimeoutMillis(ConfigKeys.CLIENT_TIMEOUT_WRITE), TimeUnit.MILLISECONDS);

        // Retry
        builder.retryOnConnectionFailure(
                this.configSupplier.get().getBoolean(ConfigKeys.CLIENT_FAILURE_RETRY.getKey()));

        if (this.configSupplier.get().getBoolean(ConfigKeys.CRYPTO_SSL_ENABLED.getKey())) {
            // SSL using our crypto configuration
            builder.sslSocketFactory(this.sslContextSupplier.get().getSocketFactory());
            builder.hostnameVerifier((hostName, sslSession) -> true);
        }

        return builder.build();
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(OkHttpClientSupplier.class).to(OkHttpClientSupplier.class).in(Singleton.class);
            bindFactory(OkHttpClientSupplier.class).to(OkHttpClient.class).in(Singleton.class);
        }
    }
}


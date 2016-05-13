package com.grpctrl.common.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides singleton access to an object mapper for performing JSON serialization and deserialization.
 */
@Provider
public class ObjectMapperSupplier
        implements Supplier<ObjectMapper>, Factory<ObjectMapper>, ContextResolver<ObjectMapper> {
    @Nullable
    private volatile ObjectMapper singleton = null;

    @Override
    @Nonnull
    @SuppressWarnings("all")
    public ObjectMapper get() {
        // Use double-check locking (with volatile singleton).
        if (this.singleton == null) {
            synchronized (ObjectMapperSupplier.class) {
                if (this.singleton == null) {
                    this.singleton = create();
                }
            }
        }
        return this.singleton;
    }

    @Override
    @Nonnull
    public ObjectMapper getContext(@Nonnull final Class<?> type) {
        return get();
    }

    @Override
    @Nonnull
    public ObjectMapper provide() {
        return get();
    }

    @Override
    public void dispose(@Nonnull final ObjectMapper objectMapper) {
        // Nothing to do.
    }

    @Nonnull
    private ObjectMapper create() {
        final ObjectMapper objectMapper = new ObjectMapper();
        // Make sure things like Java 8 Optional and other classes are handled correctly.
        objectMapper.registerModule(new Jdk8Module());
        // Make sure Java 8's new date and time classes are handled correctly.
        objectMapper.registerModule(new JSR310Module());
        return objectMapper;
    }

    /**
     * Used to bind this supplier for dependency injection.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ObjectMapperSupplier.class).to(ObjectMapperSupplier.class).in(Singleton.class);
        }
    }
}

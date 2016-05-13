package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.core.JsonGenerator;
import com.grpctrl.common.model.Account;
import com.grpctrl.common.supplier.ObjectMapperSupplier;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

/**
 * Responsible for streaming account objects as JSON.
 */
public class SingleAccountStreamer implements StreamingOutput {
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final Consumer<Consumer<Account>> consumer;

    /**
     * @param objectMapperSupplier responsible for generating JSON data
     * @param consumer the consumer responsible for pushing account objects through this class
     */
    public SingleAccountStreamer(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final Consumer<Consumer<Account>> consumer) {
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.consumer = Objects.requireNonNull(consumer);
    }

    /**
     * @return the object mapper responsible for generating JSON data
     */
    @Nonnull
    public ObjectMapperSupplier getObjectMapperSupplier() {
        return this.objectMapperSupplier;
    }

    /**
     * @return the consumer that will accept our writing consumer as input when processing the account data
     */
    @Nonnull
    public Consumer<Consumer<Account>> getConsumer() {
        return this.consumer;
    }

    @Override
    public void write(@Nonnull final OutputStream output) throws IOException, WebApplicationException {
        try (final JsonGenerator generator = getObjectMapperSupplier().get().getFactory().createGenerator(output)) {
            generator.writeStartObject();
            generator.writeFieldName("success");
            generator.writeBoolean(true);
            generator.writeFieldName("account");
            getConsumer().accept(account -> {
                try {
                    generator.writeObject(account);
                } catch (final IOException ioException) {
                    throw new InternalServerErrorException("Failed to write JSON data to client", ioException);
                }
            });
            generator.writeEndObject();
        }
    }
}

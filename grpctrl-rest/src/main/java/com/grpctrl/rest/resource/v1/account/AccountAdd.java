package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.grpctrl.common.model.Account;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Add accounts to the backing data store.
 */
@Singleton
@Path("/v1/account/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class AccountAdd extends BaseAccountResource {
    /**
     * @param objectMapperSupplier the {@link ObjectMapperSupplier} used to generate JSON data
     * @param accountDaoSupplier the {@link AccountDaoSupplier} used to perform the account operation
     */
    @Inject
    public AccountAdd(
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final AccountDaoSupplier accountDaoSupplier) {
        super(objectMapperSupplier, accountDaoSupplier);
    }

    /**
     * Save the provided accounts into the backing data store.
     *
     * @param inputStream the {@link InputStream} from the client containing the list of accounts to add
     *
     * @return the response containing the updated account, including new unique identifiers
     */
    @POST
    public Response add(@Nonnull final InputStream inputStream) {
        final StreamingOutput streamingOutput = new MultipleAccountStreamer(getObjectMapperSupplier(), consumer -> {
            try {
                final JsonParser jsonParser = getObjectMapperSupplier().get().getFactory().createParser(inputStream);

                final JsonToken startObj = jsonParser.nextToken();
                if (startObj == JsonToken.START_OBJECT) {
                    // Only a single account provided.
                    final Account account = jsonParser.readValueAs(Account.class);
                    consumer.accept(account);
                } else if (startObj == JsonToken.START_ARRAY) {
                    // Multiple accounts provided in an array.
                    final JsonToken firstAccount = jsonParser.nextToken();
                    if (firstAccount == JsonToken.START_OBJECT) {
                        final Iterator<Account> iter = jsonParser.readValuesAs(Account.class);
                        getAccountDaoSupplier().get().add(iter, consumer);
                    }
                }
            } catch (final IOException ioException) {
                throw new InternalServerErrorException("Failed to read account JSON input data", ioException);
            }
        });

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}

package com.grpctrl.rest.resource.v1.account;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grpctrl.common.model.Account;
import com.grpctrl.db.dao.AccountDao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.annotation.Nonnull;
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
 * Add an account to the backing data store.
 */
@Singleton
@Path("/v1/account/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountAdd extends BaseAccountResource {
    /**
     * @param objectMapper the {@link ObjectMapper} used to generate JSON data
     * @param accountDao the {@link AccountDao} used to perform the account operation
     */
    @Inject
    public AccountAdd(@Nonnull final ObjectMapper objectMapper, @Nonnull final AccountDao accountDao) {
        super(objectMapper, accountDao);
    }

    /**
     * Save the provided account into the backing data store.
     *
     * @param inputStream the {@link InputStream} from the client containing the list of accounts to add
     *
     * @return the response containing the updated account, including new unique identifiers
     */
    @POST
    public Response add(@Nonnull final InputStream inputStream) {
        // TODO: Only admins should be able to add accounts.

        final StreamingOutput streamingOutput = new MultipleAccountStreamer(getObjectMapper(), consumer -> {
            try {
                final JsonParser jsonParser = getObjectMapper().getFactory().createParser(inputStream);

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
                        getAccountDao().add(iter, consumer);
                    }
                }
            } catch (final IOException ioException) {
                throw new InternalServerErrorException("Failed to read account JSON input data", ioException);
            }
        });

        return Response.ok().entity(streamingOutput).type(MediaType.APPLICATION_JSON).build();
    }
}

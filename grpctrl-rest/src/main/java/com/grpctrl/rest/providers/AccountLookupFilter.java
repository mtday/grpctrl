package com.grpctrl.rest.providers;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.ApiLogin;
import com.grpctrl.db.dao.supplier.AccountDaoSupplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 * Injects account information into the request based on input headers.
 */
@Provider
public class AccountLookupFilter implements ContainerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AccountLookupFilter.class);

    public static final String ACCOUNT_PROPERTY = "grpctrl.account";

    @Nonnull
    private final AccountDaoSupplier accountDaoSupplier;

    @Inject
    public AccountLookupFilter(@Nonnull final AccountDaoSupplier accountDaoSupplier) {
        this.accountDaoSupplier = Objects.requireNonNull(accountDaoSupplier);
    }

    @Override
    public void filter(@Nonnull final ContainerRequestContext requestContext) throws IOException {
        final Optional<ApiLogin> apiLogin = getApiLogin(requestContext);

        if (apiLogin.isPresent()) {
            final Optional<Account> account = this.accountDaoSupplier.get().get(apiLogin.get());
            if (!account.isPresent()) {
                throw new BadRequestException("Failed to find account corresponding to the specified api key");
            }
            requestContext.setProperty(ACCOUNT_PROPERTY, account.get());
        }
    }

    private Optional<ApiLogin> getApiLogin(@Nonnull final ContainerRequestContext requestContext) {
        final String header = requestContext.getHeaderString(ApiLogin.HEADER_KEY);
        if (header == null) {
            // No api key header provided, it may not be needed so we don't thrown an exception here.
            return Optional.empty();
        }

        if (!header.contains(":")) {
            throw new BadRequestException("The api key header was malformed, no colon delimiter: " + header);
        }

        final List<String> parts = Arrays.asList(header.split(":"));
        if (parts.size() != 2) {
            throw new BadRequestException("The api key header was malformed, need two parts: " + header);
        }

        final String key = StringUtils.trimToEmpty(parts.get(0));
        final String secret = StringUtils.trimToEmpty(parts.get(1));

        if (StringUtils.isEmpty(key)) {
            throw new BadRequestException("The api key header was malformed, key was empty: " + header);
        }
        if (StringUtils.isEmpty(secret)) {
            throw new BadRequestException("The api key header was malformed, secret was empty " + header);
        }

        final ApiLogin apiLogin = new ApiLogin(key, secret);
        LOG.debug("User-provided api login: {}", apiLogin);
        return Optional.of(apiLogin);
    }
}

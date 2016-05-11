package com.grpctrl.run;

import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.jaas.JAASUserPrincipal;
import org.eclipse.jetty.server.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletRequest;

public class CustomLoginService extends JAASLoginService {
    private static final Logger LOG = LoggerFactory.getLogger(CustomLoginService.class);
    @Nonnull
    private final ConfigSupplier configSupplier;
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final OAuth20ServiceSupplier oAuth20ServiceSupplier;

    public CustomLoginService(
            @Nonnull final ConfigSupplier configSupplier, @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final OAuth20ServiceSupplier oAuth20ServiceSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.oAuth20ServiceSupplier = Objects.requireNonNull(oAuth20ServiceSupplier);

        setName("custom");
        setLoginModuleName("custom");
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

    @Override
    public UserIdentity login(
            @Nonnull final String username, @Nonnull final Object credentials, @Nonnull final ServletRequest request) {
        LOG.info("login");
        LOG.info("  username: {}", username);
        LOG.info("  credentials: {}", credentials);
        LOG.info("  request: {}", request);
        try {
            final CustomCallbackHandler callbackHandler =
                    new CustomCallbackHandler(getConfigSupplier(), getObjectMapperSupplier(),
                            getOAuth20ServiceSupplier(), request);

            final Subject subject = new Subject();
            final LoginContext loginContext = new LoginContext(_loginModuleName, subject, callbackHandler);

            loginContext.login();

            final JAASUserPrincipal userPrincipal =
                    new JAASUserPrincipal(callbackHandler.getUserName().orElse(null), subject, loginContext);
            subject.getPrincipals().add(userPrincipal);

            for (final Principal principal : subject.getPrincipals()) {
                LOG.info("  principal: {} => {}", principal.getClass().getName(), principal.getName());
            }

            final Collection<String> roles =
                    subject.getPrincipals().stream().map(Principal::getName).filter(role -> role != null)
                            .collect(Collectors.toCollection(TreeSet::new));

            LOG.info("user principal: {}", userPrincipal);
            LOG.info("subject: {}", subject);
            LOG.info("roles: {}", roles);

            return _identityService.newUserIdentity(subject, userPrincipal, roles.toArray(new String[roles.size()]));
        } catch (final LoginException loginException) {
            LOG.error("Failed to login user", loginException);
        }
        return null;
    }
}

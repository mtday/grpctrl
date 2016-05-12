package com.grpctrl.security;

import com.grpctrl.common.model.User;
import com.grpctrl.common.model.UserRole;
import com.grpctrl.common.supplier.ConfigSupplier;
import com.grpctrl.common.supplier.OAuth20ServiceSupplier;
import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.db.dao.supplier.UserDaoSupplier;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletRequest;

public class CustomLoginService extends AbstractLifeCycle implements LoginService {
    private static final Logger LOG = LoggerFactory.getLogger(CustomLoginService.class);

    private static final String LOGIN_MODULE_NAME = "custom";

    @Nonnull
    private final ExecutorService executorService;
    @Nonnull
    private final ConfigSupplier configSupplier;
    @Nonnull
    private final ObjectMapperSupplier objectMapperSupplier;
    @Nonnull
    private final OAuth20ServiceSupplier oAuth20ServiceSupplier;
    /*
    @Nonnull
    private final UserDaoSupplier userDaoSupplier;
    */

    private IdentityService identityService = new DefaultIdentityService();

    public CustomLoginService(
            @Nonnull final ExecutorService executorService, @Nonnull final ConfigSupplier configSupplier,
            @Nonnull final ObjectMapperSupplier objectMapperSupplier,
            @Nonnull final OAuth20ServiceSupplier oAuth20ServiceSupplier,
            @Nonnull final UserDaoSupplier userDaoSupplier) {
        this.executorService = Objects.requireNonNull(executorService);
        this.configSupplier = Objects.requireNonNull(configSupplier);
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        this.oAuth20ServiceSupplier = Objects.requireNonNull(oAuth20ServiceSupplier);
        /*
        this.userDaoSupplier = Objects.requireNonNull(userDaoSupplier);
        */
    }

    @Override
    public String getName() {
        return LOGIN_MODULE_NAME;
    }

    @Override
    public void setIdentityService(@Nonnull final IdentityService identityService) {
        this.identityService = Objects.requireNonNull(identityService);
    }

    @Override
    public IdentityService getIdentityService() {
        return this.identityService;
    }

    @Override
    public UserIdentity login(
            @Nullable final String username, @Nullable final Object credentials,
            @Nonnull final ServletRequest request) {
        LOG.info("login");
        try {
            final CustomCallbackHandler callbackHandler =
                    new CustomCallbackHandler(this.executorService, this.configSupplier, this.objectMapperSupplier,
                            this.oAuth20ServiceSupplier, null, request);

            final Subject subject = new Subject();
            final LoginContext loginContext = new LoginContext(getName(), subject, callbackHandler);

            loginContext.login();

            final User user = callbackHandler.getUser();
            user.setLoginContext(loginContext);
            LOG.info("  user: {}", user);

            subject.getPrincipals().add(user);
            subject.getPrincipals().addAll(user.getRoles());
            LOG.info("  subject: {}", subject);

            final Collection<String> roles = user.getRoles().stream().map(UserRole::name).collect(Collectors.toList());
            return getIdentityService().newUserIdentity(subject, user, roles.toArray(new String[roles.size()]));
        } catch (final LoginException loginException) {
            LOG.error("Failed to login user", loginException);
        }
        return null;
    }

    @Override
    public void logout(@Nonnull final UserIdentity userIdentity) {
        Objects.requireNonNull(userIdentity);
        LOG.info("logout: {}", userIdentity);

        userIdentity.getSubject().getPrincipals(User.class).forEach(user -> {
            final Optional<LoginContext> loginContext = user.getLoginContext();
            if (loginContext.isPresent()) {
                try {
                    loginContext.get().logout();
                } catch (final LoginException logoutFailed) {
                    LOG.warn("Failed to invoke logout on user login context", logoutFailed);
                }
            }
        });
    }

    @Override
    public boolean validate(@Nonnull final UserIdentity userIdentity) {
        LOG.info("validating: {}", userIdentity);
        return true;
    }
}

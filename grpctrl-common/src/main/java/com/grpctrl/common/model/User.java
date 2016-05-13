package com.grpctrl.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grpctrl.common.util.CollectionComparator;
import com.grpctrl.common.util.OptionalComparator;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.login.LoginContext;
import javax.ws.rs.core.SecurityContext;

/**
 * Provides information about user email addresses as provided by the security authorization service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Comparable<User>, Principal, SecurityContext {
    @Nullable
    private Long id;

    @Nonnull
    private String login = "";

    @Nonnull
    private UserSource userSource = UserSource.GITHUB;

    @Nullable
    private LocalDateTime created;

    @Nullable
    private LocalDateTime lastLogin;

    @Nullable
    private UserAuth userAuth;

    @Nonnull
    private Set<UserEmail> emails = new TreeSet<>();

    @Nonnull
    private Set<UserRole> roles = new TreeSet<>();

    @Nonnull
    private Set<Account> accounts = new TreeSet<>();

    @Nullable
    private LoginContext loginContext;

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * @param login the user login
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public User(@Nonnull final String login) {
        this(login, UserSource.GITHUB);
    }

    /**
     * @param login the user login
     * @param userSource the source of the user account
     *
     * @throws NullPointerException if either of the provided parameters are {@code null}
     */
    public User(@Nonnull final String login, @Nonnull final UserSource userSource) {
        setLogin(login);
        setUserSource(userSource);
    }

    /**
     * @param other the user account to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public User(@Nonnull final User other) {
        setValues(other);
    }

    /**
     * @param other the user account to duplicate
     *
     * @throws NullPointerException if the provided parameter is {@code null}
     */
    public void setValues(@Nonnull final User other) {
        Objects.requireNonNull(other);
        setId(other.getId().orElse(null));
        setLogin(other.getLogin());
        setUserSource(other.getUserSource());
        setCreated(other.getCreated().orElse(null));
        setLastLogin(other.getLastLogin().orElse(null));
        setUserAuth(other.getUserAuth().orElse(null));
        setEmails(other.getEmails());
        setRoles(other.getRoles());
        setAccounts(other.getAccounts());
    }

    /**
     * @return the unique account identifier, if available
     */
    @Nonnull
    public Optional<Long> getId() {
        return Optional.ofNullable(this.id);
    }

    /**
     * @param id the new unique user identifier, possibly {@code null}
     *
     * @return {@code this} for fluent-style usage
     */
    public User setId(@Nullable final Long id) {
        this.id = id;
        return this;
    }

    @Override
    public String getName() {
        return getLogin();
    }

    /**
     * @return the user login
     */
    @Nonnull
    public String getLogin() {
        return this.login;
    }

    /**
     * @param login the new user login
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code login} parameter is {@code null}
     */
    @Nonnull
    public User setLogin(@Nonnull final String login) {
        this.login = Objects.requireNonNull(login);
        return this;
    }

    /**
     * @return the source of the user account
     */
    @Nonnull
    public UserSource getUserSource() {
        return this.userSource;
    }

    /**
     * @param userSource the new source of the user account
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code userSource} parameter is {@code null}
     */
    @Nonnull
    public User setUserSource(@Nonnull final UserSource userSource) {
        this.userSource = Objects.requireNonNull(userSource);
        return this;
    }

    /**
     * @return the timestamp when this user was created, if available
     */
    @Nonnull
    public Optional<LocalDateTime> getCreated() {
        return Optional.ofNullable(this.created);
    }

    /**
     * @param created the new timestamp when this user was created, possibly {@code null}
     *
     * @return {@code this} for fluent-style usage
     */
    public User setCreated(@Nullable final LocalDateTime created) {
        this.created = created;
        return this;
    }

    /**
     * @return the timestamp when this user last logged in, if available
     */
    @Nonnull
    public Optional<LocalDateTime> getLastLogin() {
        return Optional.ofNullable(this.lastLogin);
    }

    /**
     * @param lastLogin the new timestamp when this user last logged in, possibly {@code null}
     *
     * @return {@code this} for fluent-style usage
     */
    public User setLastLogin(@Nullable final LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
        return this;
    }

    /**
     * @return the user authorization information, if available
     */
    @Nonnull
    public Optional<UserAuth> getUserAuth() {
        return Optional.ofNullable(this.userAuth);
    }

    /**
     * @param userAuth the new user authorization information, possibly {@code null}
     *
     * @return {@code this} for fluent-style usage
     */
    public User setUserAuth(@Nullable final UserAuth userAuth) {
        this.userAuth = userAuth;
        return this;
    }

    /**
     * @return the email addresses associated with this account
     */
    @Nonnull
    public Set<UserEmail> getEmails() {
        return this.emails; // Not a copy.
    }

    /**
     * @param emails the new email addresses for this user account
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code emails} parameter is {@code null}
     */
    public User setEmails(@Nonnull final Collection<UserEmail> emails) {
        this.emails = new TreeSet<>(Objects.requireNonNull(emails));
        return this;
    }

    /**
     * @return the roles associated with this account
     */
    @Nonnull
    public Set<UserRole> getRoles() {
        return this.roles; // Not a copy.
    }

    /**
     * @param roles the new roles for this user account
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code roles} parameter is {@code null}
     */
    public User setRoles(@Nonnull final Collection<UserRole> roles) {
        this.roles = new TreeSet<>(Objects.requireNonNull(roles));
        return this;
    }

    /**
     * @return the accounts associated with this account
     */
    @Nonnull
    public Set<Account> getAccounts() {
        return this.accounts; // Not a copy.
    }

    /**
     * @param accounts the new accounts for this user account
     * @return {@code this} for fluent-style usage
     *
     * @throws NullPointerException if the provided {@code accounts} parameter is {@code null}
     */
    public User setAccounts(@Nonnull final Collection<Account> accounts) {
        this.accounts = new TreeSet<>();
        Objects.requireNonNull(accounts).stream().map(Account::new).forEach(this.accounts::add);
        return this;
    }

    @Nonnull
    public Optional<LoginContext> getLoginContext() {
        return Optional.ofNullable(this.loginContext);
    }

    /**
     * @param loginContext the new {@link LoginContext} for this user
     * @return {@code this} for fluent-style usage
     */
    public User setLoginContext(@Nullable final LoginContext loginContext) {
        this.loginContext = loginContext;
        return this;
    }

    @Override
    @Nonnull
    public Principal getUserPrincipal() {
        return this;
    }

    @Override
    public boolean isUserInRole(@Nonnull final String role) {
        return getRoles().stream().map(UserRole::name).anyMatch(r -> r.equals(role));
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }

    @Override
    public int compareTo(@Nullable final User other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getId(), other.getId(), new OptionalComparator<>());
        cmp.append(getLogin(), other.getLogin());
        cmp.append(getUserSource(), other.getUserSource());
        cmp.append(getCreated(), other.getCreated(), new OptionalComparator<>());
        cmp.append(getLastLogin(), other.getLastLogin(), new OptionalComparator<>());
        cmp.append(getUserAuth(), other.getUserAuth(), new OptionalComparator<>());
        cmp.append(getEmails(), other.getEmails(), new CollectionComparator<>());
        cmp.append(getRoles(), other.getRoles(), new CollectionComparator<>());
        cmp.append(getAccounts(), other.getAccounts(), new CollectionComparator<>());
        return cmp.toComparison();
    }

    @Override
    public boolean equals(@CheckForNull final Object other) {
        return other instanceof User && compareTo((User) other) == 0;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getId());
        hash.append(getLogin());
        hash.append(getUserSource());
        hash.append(getCreated());
        hash.append(getLastLogin());
        hash.append(getUserAuth());
        hash.append(getEmails());
        hash.append(getRoles());
        hash.append(getAccounts());
        return hash.toHashCode();
    }

    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("login", getLogin());
        str.append("userSource", getUserSource());
        str.append("created", getCreated());
        str.append("lastLogin", getLastLogin());
        str.append("userAuth", getUserAuth());
        str.append("emails", getEmails());
        str.append("roles", getRoles());
        str.append("accounts", getAccounts());
        return str.build();
    }
}

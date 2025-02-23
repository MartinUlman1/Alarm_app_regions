package ivr.alarmregions.authentication;

import com.genesyslab.platform.applicationblocks.com.ConfigException;
import com.genesyslab.platform.applicationblocks.com.IConfService;
import com.genesyslab.platform.applicationblocks.com.objects.CfgAccessGroup;
import com.genesyslab.platform.applicationblocks.com.objects.CfgGroup;
import com.genesyslab.platform.applicationblocks.com.objects.CfgPerson;
import com.genesyslab.platform.applicationblocks.com.queries.CfgAccessGroupQuery;
import com.genesyslab.platform.applicationblocks.com.queries.CfgPersonQuery;
import com.genesyslab.platform.commons.protocol.Message;
import com.genesyslab.platform.commons.protocol.ProtocolException;
import com.genesyslab.platform.configuration.protocol.confserver.events.EventAuthenticated;
import com.genesyslab.platform.configuration.protocol.confserver.requests.security.RequestAuthenticate;
import com.genesyslab.platform.configuration.protocol.types.CfgObjectState;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.annotation.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
public class GenesysUserDetailsProvider extends AbstractUserDetailsAuthenticationProvider {

    private final IConfService confService;

    public GenesysUserDetailsProvider(IConfService confService) {
        this.confService = confService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        if (authentication.getCredentials() == null) {
            log.warn("Authentication failed: no credentials provided");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        RequestAuthenticate requestAuthenticate = RequestAuthenticate.create(authentication.getName(), authentication.getCredentials().toString());

        @Nullable Message response;

        try {
            response = confService.getProtocol().request(requestAuthenticate);
        } catch (IllegalStateException e) {
            throw new AuthenticationServiceException(
                    "Not connected to Genesys configuration server", e);
        } catch (ProtocolException e) {
            throw new AuthenticationServiceException(
                    "Failed to send request to Genesys configuration server", e);
        }

        if (response == null) {
            throw new AuthenticationServiceException("Timeout while waiting for response from Genesys configuration server");
        }

        if (response instanceof EventAuthenticated) {
            return;
        }

        throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials",
                "Bad credentials"
        ));
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        @Nullable CfgPerson cfgPerson;

        try {
            CfgPersonQuery personQuery = new CfgPersonQuery();
            personQuery.setUserName(username);
            cfgPerson = confService.retrieveObject(personQuery);
        } catch (Exception exception) {
            throw new AuthenticationServiceException("Failed to retrieve user from Genesys", exception);
        }

        if (cfgPerson == null) {
            throw new UsernameNotFoundException(format("Person {0} not found in Genesys configuration}", username));
        }

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        try {
            CfgAccessGroupQuery cfgAccessGroupQuery = new CfgAccessGroupQuery();
            cfgAccessGroupQuery.setPersonDbid(cfgPerson.getObjectDbid());
            cfgAccessGroupQuery.setState(CfgObjectState.CFGEnabled);
            @Nullable Collection<CfgAccessGroup> accessGroups = confService.retrieveMultipleObjects(CfgAccessGroup.class, cfgAccessGroupQuery);
            if (accessGroups != null) {
                for (CfgAccessGroup accessGroup : accessGroups) {
                    CfgGroup group = accessGroup.getGroupInfo();

                    if (group != null && group.getName().startsWith("KH_")) {
                        log.trace("Getting authorities from access group {}", group.getName().replace("KH_", ""));

                        grantedAuthorities.add(new SimpleGrantedAuthority(group.getName().replace("KH_", "")));
                    }
                }
            }
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new User(username, authentication.getCredentials().toString(), grantedAuthorities);
    }
}

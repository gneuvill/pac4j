package org.pac4j.core.client.finder;

import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Find the right clients based on the query parameter for the {@link org.pac4j.core.engine.SecurityLogic}.
 *
 * @author Jerome Leleu
 * @since 1.8.0
 */
public class DefaultSecurityClientFinder implements ClientFinder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSecurityClientFinder.class);

    private String clientNameParameter = Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER;

    @Override
    public List<Client> find(final Clients clients, final WebContext context, final String clientNames) {
        final List<Client> result = new ArrayList<>();

        String securityClientNames = clientNames;
        // we don't have defined clients to secure the URL, use the general default security ones from the Clients if they exist
        // we check the nullity and not the blankness to allow the blank string to mean no client
        // so no clients parameter -> use the default security ones; clients=blank string -> no clients defined
        logger.debug("Provided clientNames: {}", securityClientNames);
        if (securityClientNames == null) {
            securityClientNames = clients.getDefaultSecurityClients();
            logger.debug("Default security clients: {}", securityClientNames);
            // still no clients defined and we only have one client, use it
            if (securityClientNames == null && clients.findAllClients().size() == 1) {
                securityClientNames = clients.getClients().get(0).getName();
                logger.debug("Only client: {}", securityClientNames);
            }
        }

        if (CommonHelper.isNotBlank(securityClientNames)) {
            final List<String> names = Arrays.asList(securityClientNames.split(Pac4jConstants.ELEMENT_SEPARATOR));
            // if a "client_name" parameter is provided on the request, get the client
            // and check if it is allowed (defined in the list of the clients)
            final Optional<String> clientNameOnRequest = context.getRequestParameter(clientNameParameter);
            logger.debug("clientNameOnRequest: {}", clientNameOnRequest);
            if (clientNameOnRequest.isPresent()) {
                // from the request
                final Optional<Client> client = clients.findClient(clientNameOnRequest.get());
                if (client.isPresent()) {
                    final String nameFound = client.get().getName();
                    // if allowed -> return it
                    for (final String name : names) {
                        if (CommonHelper.areEqualsIgnoreCaseAndTrim(name, nameFound)) {
                            result.add(client.get());
                            break;
                        }
                    }
                }
            } else {
                // no client provided, return all
                for (final String name : names) {
                    // from its name
                    final Optional<Client> client = clients.findClient(name);
                    if (client.isPresent()) {
                        result.add(client.get());
                    }
                }
            }
        }
        logger.debug("result: {}", result.stream().map(c -> c.getName()).collect(Collectors.toList()));
        return result;
    }

    public String getClientNameParameter() {
        return clientNameParameter;
    }

    public void setClientNameParameter(final String clientNameParameter) {
        this.clientNameParameter = clientNameParameter;
    }
}

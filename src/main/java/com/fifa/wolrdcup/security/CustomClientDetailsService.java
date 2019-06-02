package com.fifa.wolrdcup.security;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.util.HashMap;
import java.util.Map;

public class CustomClientDetailsService implements ClientDetailsService {
    private Map<String, ClientDetails> clientDetailsStore = new HashMap<String, ClientDetails>();

    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        ClientDetails details = clientDetailsStore.get(clientId);
        if (details == null) {
            throw new NoSuchClientException("No client with requested id: " + clientId);
        }
        return details;
    }

    public void addDefaultClientDetails(String clientId) {
        this.clientDetailsStore.put(clientId, getClientDetails(clientId));
    }

    private ClientDetails getClientDetails(String clientId) {
        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId(clientId);
        clientDetails.setAccessTokenValiditySeconds(Integer.MAX_VALUE);

        return clientDetails;
    }

}
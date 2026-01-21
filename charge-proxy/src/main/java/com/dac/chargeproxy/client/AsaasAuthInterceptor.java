package com.dac.chargeproxy.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Feign request interceptor that adds ASAAS authentication header.
 * 
 * This interceptor is applied to all requests made by the Feign client,
 * adding the access_token header required by ASAAS API.
 */
public class AsaasAuthInterceptor implements RequestInterceptor {

    private final String accessToken;

    /**
     * Creates an interceptor with the specified access token.
     *
     * @param accessToken the ASAAS API access token
     */
    public AsaasAuthInterceptor(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (accessToken != null && !accessToken.isEmpty()) {
            template.header("access_token", accessToken);
        }
    }
}

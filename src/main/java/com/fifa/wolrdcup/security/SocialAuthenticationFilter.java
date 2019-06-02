package com.fifa.wolrdcup.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SocialAuthenticationFilter extends OAuth2ClientAuthenticationProcessingFilter {
    private DefaultTokenServices defaultTokenServices;

    public SocialAuthenticationFilter(String defaultFilterProcessesUrl, DefaultTokenServices defaultTokenServices) {
        super(defaultFilterProcessesUrl);

        this.defaultTokenServices = defaultTokenServices;

        setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler() {
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                this.setRedirectStrategy((request1, response1, url) -> {});

                super.onAuthenticationSuccess(request, response, authentication);
            }
        });
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        restTemplate.getOAuth2ClientContext().setAccessToken(null);

        OAuth2Authentication authentication = (OAuth2Authentication) super.attemptAuthentication(request, response);

        OAuth2AccessToken token = defaultTokenServices.createAccessToken(authentication);

        response.getWriter().write("<script>window.opener.postMessage('" + token.getValue() +
                "', '*');</script><a href='javascript:window.close();'>Close this window</a>");
        response.setContentType("text/html");

        Map<String, String> details = new HashMap<>();
        details.put("token", token.getValue());

        authentication.setDetails(details);

        return authentication;
    }
}
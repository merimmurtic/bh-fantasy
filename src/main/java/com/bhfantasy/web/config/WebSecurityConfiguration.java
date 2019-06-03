package com.bhfantasy.web.config;

import com.bhfantasy.web.model.User;
import com.bhfantasy.web.repository.UserRepository;
import com.bhfantasy.web.security.ApiTokenAccessFilter;
import com.bhfantasy.web.security.CustomClientDetailsService;
import com.bhfantasy.web.security.SocialAuthenticationFilter;
import com.bhfantasy.web.security.UserTokenConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.time.LocalDateTime;
import java.util.*;

@EnableOAuth2Client
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static Logger logger = LoggerFactory.getLogger(WebSecurityConfiguration.class);

    private final OAuth2ClientContext oauth2ClientContext;

    private final UserRepository userRepository;

    @Autowired
    public WebSecurityConfiguration(
            @Qualifier("oauth2ClientContext") OAuth2ClientContext oauth2ClientContext,
            UserRepository userRepository) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.userRepository = userRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .antMatcher("/api/**")
                    .authorizeRequests()
                    .antMatchers("/api/**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll()
                .and().csrf().disable()
                //.and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new ApiTokenAccessFilter(customTokenServices()), AbstractPreAuthenticatedProcessingFilter.class);
    }

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();

        SocialAuthenticationFilter facebookFilter = new SocialAuthenticationFilter(
                "/api/login/facebook", customTokenServices());
        OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), oauth2ClientContext);

        facebookFilter.setRestTemplate(facebookTemplate);
        facebookFilter.setTokenServices(getFacebookTokenServices(facebookTemplate));

        filters.add(facebookFilter);

        SocialAuthenticationFilter googleFilter = new SocialAuthenticationFilter(
                "/api/login/google", customTokenServices());
        OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);

        googleFilter.setRestTemplate(googleTemplate);
        googleFilter.setTokenServices(getGoogleTokenServices(googleTemplate));

        filters.add(googleFilter);

        filter.setFilters(filters);

        return filter;
    }

    @Bean
    public DefaultTokenServices customTokenServices() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(new UserTokenConverter());

        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConverter);
        jwtAccessTokenConverter.setSigningKey("123");

        jwtAccessTokenConverter.setVerifier(new SignatureVerifier() {
            @Override
            public void verify(byte[] content, byte[] signature) {
            }

            @Override
            public String algorithm() {
                return null;
            }
        });

        DefaultTokenServices service = new DefaultTokenServices();
        service.setTokenStore(new JwtTokenStore(jwtAccessTokenConverter));
        service.setTokenEnhancer(jwtAccessTokenConverter);

        service.setClientDetailsService(clientDetails());

        return service;
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Bean
    @SuppressWarnings("unchecked")
    public FilterRegistrationBean oauth2ClientFilterRegistration(
            OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    @ConfigurationProperties("facebook.client")
    public AuthorizationCodeResourceDetails facebook() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("facebook.resource")
    public ResourceServerProperties facebookResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("google.client")
    public AuthorizationCodeResourceDetails google() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("google.resource")
    public ResourceServerProperties googleResource() {
        return new ResourceServerProperties();
    }

    @Bean
    public ClientDetailsService clientDetails() {
        CustomClientDetailsService clientDetailsService = new CustomClientDetailsService();

        clientDetailsService.addDefaultClientDetails(facebook().getClientId());
        clientDetailsService.addDefaultClientDetails(google().getClientId());

        return clientDetailsService;
    }

    private UserInfoTokenServices getGoogleTokenServices(OAuth2RestTemplate googleTemplate) {
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                googleResource().getUserInfoUri(), google().getClientId());
        tokenServices.setPrincipalExtractor(principalExtractor(User.UserLoginType.GOOGLE));
        tokenServices.setRestTemplate(googleTemplate);

        return tokenServices;
    }

    private UserInfoTokenServices getFacebookTokenServices(OAuth2RestTemplate facebookTemplate) {
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                facebookResource().getUserInfoUri(), facebook().getClientId());
        tokenServices.setPrincipalExtractor(principalExtractor(User.UserLoginType.FACEBOOK));
        tokenServices.setRestTemplate(facebookTemplate);

        return tokenServices;
    }

    private PrincipalExtractor principalExtractor(User.UserLoginType type) {
        return map -> {
            String principalId = (String) map.get("id");
            User user = userRepository.findByPrincipalId(principalId);
            if (user == null) {
                logger.info("No user found, generating profile for {}", principalId);
                user = new User();
                user.setPrincipalId(principalId);
                user.setCreated(LocalDateTime.now());
                user.setLoginType(type);
                user.setLastLogin(LocalDateTime.now());
                user.setAdmin(isAdmin(user.getPrincipalId()));
            } else {
                user.setLastLogin(LocalDateTime.now());
            }

            user.setEmail((String) map.get("email"));
            user.setFullName((String) map.get("name"));
            if(type == User.UserLoginType.FACEBOOK) {
                user.setPhoto("http://graph.facebook.com/" + user.getPrincipalId()+ "/picture?width=256&height=256");
            } else {
                user.setPhoto((String) map.get("picture"));
            }

            userRepository.save(user);
            return user;
        };
    }

    private boolean isAdmin(String principalId) {
        return Arrays.asList("114768242047881168319", "10157422100039443").contains(principalId);
    }
}

package com.fifa.wolrdcup.config;

import com.fifa.wolrdcup.model.User;
import com.fifa.wolrdcup.repository.UserRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@EnableOAuth2Client
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static Logger logger = LoggerFactory.getLogger(WebSecurityConfiguration.class);

    private final OAuth2ClientContext oauth2ClientContext;

    private final UserRepository userRepository;

    private static DefaultTokenServices TOKEN_SERVICES;

    @Autowired
    public WebSecurityConfiguration(
            @Qualifier("oauth2ClientContext") OAuth2ClientContext oauth2ClientContext,
            UserRepository userRepository) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.userRepository = userRepository;

        TOKEN_SERVICES = tokenServices();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                    .authorizeRequests()
                    .antMatchers("/**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
                .and().logout().logoutSuccessUrl("/").permitAll()
                .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new ApiTokenAccessFilter(TOKEN_SERVICES), AbstractPreAuthenticatedProcessingFilter.class);
    }

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();

        SocialAuthenticationFilter facebookFilter = new SocialAuthenticationFilter(
                "/login/facebook", TOKEN_SERVICES);
        OAuth2RestTemplate facebookTemplate = new OAuth2RestTemplate(facebook(), oauth2ClientContext);

        facebookFilter.setRestTemplate(facebookTemplate);
        facebookFilter.setTokenServices(getFacebookTokenServices(facebookTemplate));

        filters.add(facebookFilter);

        SocialAuthenticationFilter googleFilter = new SocialAuthenticationFilter(
                "/login/google", TOKEN_SERVICES);
        OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);

        googleFilter.setRestTemplate(googleTemplate);
        googleFilter.setTokenServices(getGoogleTokenServices(googleTemplate));

        filters.add(googleFilter);

        filter.setFilters(filters);

        return filter;
    }

    private DefaultTokenServices tokenServices() {
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

        return service;
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
}

class ApiTokenAccessFilter extends OAuth2AuthenticationProcessingFilter {

    ApiTokenAccessFilter(ResourceServerTokenServices resourceServerTokenServices) {

        super();
        setStateless(true);
        setAuthenticationManager(oauthAuthenticationManager(resourceServerTokenServices));
    }

    private AuthenticationManager oauthAuthenticationManager(ResourceServerTokenServices tokenServices) {

        OAuth2AuthenticationManager oauthAuthenticationManager = new OAuth2AuthenticationManager();

        oauthAuthenticationManager.setResourceId("oauth2-resource");
        oauthAuthenticationManager.setTokenServices(tokenServices);

        return oauthAuthenticationManager;
    }
}

class SocialAuthenticationFilter extends OAuth2ClientAuthenticationProcessingFilter {
    private DefaultTokenServices defaultTokenServices;

    SocialAuthenticationFilter(String defaultFilterProcessesUrl, DefaultTokenServices defaultTokenServices) {
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

class UserTokenConverter extends DefaultUserAuthenticationConverter {
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        User user = (User) authentication.getPrincipal();

        response.put(USERNAME, user.getPrincipalId());

        if(user.getEmail() != null) {
            response.put("email", user.getEmail());
        }

        if(user.getFullName() != null) {
            response.put("name", user.getFullName());
        }

        if(user.getPhoto() != null) {
            response.put("photo", user.getPhoto());
        }

        if(user.getId() != null) {
            response.put("userId", user.getId());
        }

        if(user.getLoginType() != null) {
            response.put("type", user.getLoginType());
        }

        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USERNAME)) {
            User user = new User();
            user.setEmail((String) map.get("email"));
            user.setFullName((String) map.get("name"));
            user.setPhoto((String) map.get("photo"));
            user.setPrincipalId((String) map.get(USERNAME));

            if(map.containsKey("userId")) {
                user.setId(Long.parseLong(map.get("userId").toString()));
            }

            user.setLoginType(User.UserLoginType.valueOf((String)map.get("type")));

            Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
            return new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
        }
        return null;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
        if (!map.containsKey(AUTHORITIES)) {
            return new ArrayList<>();
        }
        Object authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
                    .collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}
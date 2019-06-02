package com.bhfantasy.web.security;

import com.bhfantasy.web.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserTokenConverter extends DefaultUserAuthenticationConverter {
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

package damon.backend.dto.login;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Response oAuth2Response;

    private Map<String, Object> attributes;

    public CustomOAuth2User(OAuth2Response oAuth2Response) {
        this.oAuth2Response = oAuth2Response;
        attributes = new HashMap<>();
        attributes.put("providerName", getProviderName());
        attributes.put("name", oAuth2Response.getName());
        attributes.put("email", oAuth2Response.getEmail());
        attributes.put("profileImgUrl", oAuth2Response.getProfileImgUrl());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // 모든 사용자에게 'ROLE_USER' 권한을 부여
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 아이디를 강제로 커스텀 (유저네임 = 프로바이더 + 소셜에서 발급받은 코드)
    public String getProviderName() {

        return oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
    }

    @Override
    public String getName() {

        return oAuth2Response.getName();
    }


    public String getEmail() {

        return oAuth2Response.getEmail();
    }

    public String getProfileImgUrl() {

        return oAuth2Response.getProfileImgUrl();
    }

}

package damon.backend.dto.login;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> properties;

    // 고정된 카카오 응답 형태
    public KakaoResponse(Map<String, Object> attribute) {
        this.kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        this.properties = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return kakaoAccount.get("id").toString();
    }

    @Override
    public String getName() {
        return properties.containsKey("nickname") ? properties.get("nickname").toString() : null;
    }

    @Override
    public String getEmail() {
        return kakaoAccount.containsKey("email") ? kakaoAccount.get("email").toString() : null;
    }

    @Override
    public String getProfileImgUrl() {
        return properties != null ? properties.get("profile_image_url").toString() : null;
    }



}

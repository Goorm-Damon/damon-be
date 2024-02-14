package damon.backend.dto.login;

public interface OAuth2Response {

    //제공자 (naver, kakao)
    String getProvider();
    //제공자에서 발급해주는 아이디(번호)
    String getProviderId();

    //사용자 실명 (설정한 이름)
    String getName();
    //이메일
    String getEmail();

    String getProfileImgUrl();

    // 제공자 이름과 제공자 ID를 결합하여 providername 생성
    default String getProviderName() {
        return getProvider() + " " + getProviderId();
    }

}

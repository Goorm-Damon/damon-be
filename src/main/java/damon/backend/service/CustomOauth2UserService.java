package damon.backend.service;

import damon.backend.dto.login.CustomOAuth2User;
import damon.backend.dto.login.KakaoResponse;
import damon.backend.dto.login.NaverResponse;
import damon.backend.dto.login.OAuth2Response;
import damon.backend.entity.Member;
import damon.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    // 소셜 로그인 6 ~ 7번 과정입니다
    // oauth에서 데이터가 들어오면 나의 db의 유저와 비교합니다 (이때 나의 db는 멤버 엔티티를 말합니다)
    // 값이 있다면 업데이트 없다면 신규 저장합니다

    // 해당 요청은 액세스 토큰 요청입니다 (이미 시큐리티에서 자동으로 인가코드 확인 작업 완)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // userRequest 정보 중 일부를 로그로 출력
        log.info("엑세스 토큰: {}", userRequest.getAccessToken());

        // Spring Security는 인가 코드를 사용하여 액세스 토큰을 획득하고 사용자 정보를 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 네이버, 카카오 중 어느 인증 provider 인 지 판단
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 데이터 담을 바구니 객체 선언
        OAuth2Response oAuth2Response = null;

        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());

        } else if (registrationId.equals("kakao")) {

            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        } else {

            return null;
        }

        // 구현
        // 소셜로그인의 커스텀 아이디를 통해 내가 가진 db에 해당 유저가 있는지 조회
        String providername = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Member existData = memberRepository.findByProvidername(providername);

        if (existData == null) { // 신규회원 등록
            Member member = new Member();
            member.setProvidername(providername);
            member.setName(oAuth2Response.getName());
            member.setEmail(oAuth2Response.getEmail());
            member.setProfileImgUrl(oAuth2Response.getProfileImgUrl());

            memberRepository.save(member);
        } else { // 기존회원 업데이트

            existData.setName(oAuth2Response.getName());
            existData.setProfileImgUrl(oAuth2Response.getProfileImgUrl());

            memberRepository.save(existData);

        }


        return new CustomOAuth2User(oAuth2Response);

    }

}

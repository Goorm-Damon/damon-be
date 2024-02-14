package damon.backend.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@RequiredArgsConstructor
public class CustomClientRegistrationRepo {
    private final SocialClientRegistration socialClientRegistration;

    // 인메모리에 저장
    public ClientRegistrationRepository clientRegistrationRepository(){
        return  new InMemoryClientRegistrationRepository(socialClientRegistration.naverClientRegistration(), socialClientRegistration.kakaoClientRegistration());
    }
}

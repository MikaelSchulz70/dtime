package se.dtime.service.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.UserRepository;

@Service
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserPO resolveCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserExt userExt) {
            return userRepository.findById(userExt.getId()).orElseThrow(() -> new NotFoundException("user.not.found"));
        }

        if (principal instanceof OAuth2User oauth2User) {
            String sub = oauth2User.getAttribute("sub");
            if (sub != null && !sub.isBlank()) {
                UserPO userByExternalId = userRepository.findByExternalId(sub);
                if (userByExternalId != null) {
                    return userByExternalId;
                }
            }

            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                UserPO userByEmail = userRepository.findByEmail(email);
                if (userByEmail != null) {
                    return userByEmail;
                }
            }
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.isBlank()) {
            UserPO userByUsername = userRepository.findByEmail(username);
            if (userByUsername != null) {
                return userByUsername;
            }
        }

        return userRepository.findById(1L).orElseThrow(() -> new NotFoundException("user.not.found"));
    }
}

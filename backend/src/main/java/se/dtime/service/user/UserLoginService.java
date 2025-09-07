package se.dtime.service.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserExt;
import se.dtime.model.UserRole;
import se.dtime.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserLoginService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserLoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserPO user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException("invalid.username.or.password");
            }

            if (user.getActivationStatus() == ActivationStatus.INACTIVE) {
                throw new UsernameNotFoundException("invalid.username.or.password");
            }

            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));
            if (user.getUserRole() == UserRole.ADMIN) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()));
            }

            return new UserExt(user.getEmail(), user.getPassword(), authorities, user.getId(), user.getFirstName(), user.getLastName());
        } catch (Exception e) {
            throw new UsernameNotFoundException("invalid.username.or.password");
        }
    }
}

package se.dtime.service.user;

import org.springframework.beans.factory.annotation.Autowired;
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
import se.dtime.model.error.NotAuthorizedException;
import se.dtime.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserLoginService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserPO user = userRepository.findByUserName(userName);
        if (user == null || user.getActivationStatus() == ActivationStatus.INACTIVE){
            throw new UsernameNotFoundException("invalid.username.or.password");
        } else if (user.getUserRole() == UserRole.NONE) {
            throw new NotAuthorizedException("user.no.credential");
        }



        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));
        if (user.getUserRole() == UserRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()));
        }

        return new UserExt(user.getUserName(), user.getPassword(), authorities, user.getId(), user.getFirstName(), user.getLastName());
    }
}

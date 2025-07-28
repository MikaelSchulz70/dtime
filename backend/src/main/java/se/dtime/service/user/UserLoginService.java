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
import se.dtime.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserLoginService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== UserLoginService.loadUserByUsername called ===");
        System.out.println("DEBUG: Attempting to load user by email: " + email);
        
        try {
            UserPO user = userRepository.findByEmail(email);
            if (user == null) {
                System.out.println("DEBUG: User not found for email: " + email);
                throw new UsernameNotFoundException("invalid.username.or.password");
            }
            
            System.out.println("DEBUG: Found user: " + user.getEmail() + ", status: " + user.getActivationStatus());
            if (user.getActivationStatus() == ActivationStatus.INACTIVE) {
                System.out.println("DEBUG: User is inactive");
                throw new UsernameNotFoundException("invalid.username.or.password");
            }

            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));
            if (user.getUserRole() == UserRole.ADMIN) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + UserRole.USER.name()));
            }

            System.out.println("DEBUG: User loaded successfully with authorities: " + authorities);
            System.out.println("DEBUG: Password hash from DB: " + user.getPassword());
            return new UserExt(user.getEmail(), user.getPassword(), authorities, user.getId(), user.getFirstName(), user.getLastName());
        } catch (Exception e) {
            System.out.println("ERROR in UserLoginService: " + e.getMessage());
            e.printStackTrace();
            throw new UsernameNotFoundException("invalid.username.or.password");
        }
    }
}

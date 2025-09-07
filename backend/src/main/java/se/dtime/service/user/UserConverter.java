package se.dtime.service.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.User;
import se.dtime.service.BaseConverter;

import java.util.List;

@Service
public class UserConverter extends BaseConverter {

    private static final String DUMMY_PWD = "dummy1234";

    private final BCryptPasswordEncoder passwordEncoder;

    public UserConverter(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User toModel(UserPO userPO) {
        if (userPO == null) {
            return null;
        }

        return User.builder().id(userPO.getId()).
                firstName(userPO.getFirstName()).
                lastName(userPO.getLastName()).
                email(userPO.getEmail()).
                password(DUMMY_PWD).
                activationStatus(userPO.getActivationStatus()).
                userRole(userPO.getUserRole()).
                build();
    }

    public UserPO toPO(User user) {
        if (user == null) {
            return null;
        }

        UserPO userPO = new UserPO();
        userPO.setFirstName(user.getFirstName());
        userPO.setLastName(user.getLastName());
        userPO.setEmail(user.getEmail());
        userPO.setPassword(!StringUtils.isEmpty(user.getPassword()) ? passwordEncoder.encode(user.getPassword()) : null);
        userPO.setActivationStatus(user.getActivationStatus());
        userPO.setUserRole(user.getUserRole());
        updateBaseData(userPO);

        return userPO;
    }

    public UserPO toPO(User user, UserPO currentUserPO) {
        UserPO updatedUserPO = new UserPO();
        // Only set ID if it's not 0 (for updates)
        // For new entities (ID = 0), let Hibernate generate the ID via sequence
        if (user.getId() != null && user.getId() != 0) {
            updatedUserPO.setId(user.getId());
        }
        updatedUserPO.setFirstName(user.getFirstName());
        updatedUserPO.setLastName(user.getLastName());
        updatedUserPO.setEmail(user.getEmail());
        updatedUserPO.setActivationStatus(user.getActivationStatus());
        updatedUserPO.setUserRole(user.getUserRole());
        updateBaseData(updatedUserPO);

        if (!DUMMY_PWD.equals(user.getPassword())) {
            updatedUserPO.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            updatedUserPO.setPassword(currentUserPO.getPassword());
        }

        return updatedUserPO;
    }

    private String removeSpaces(String value) {
        return value != null ? value.replace(" ", "") : value;
    }

    public User[] toModel(List<UserPO> userPOList) {
        List<User> users = userPOList.stream()
                .map(this::toModel)
                .toList();
        return users.toArray(new User[0]);
    }
}

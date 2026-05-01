package se.dtime.service.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.User;
import se.dtime.service.BaseConverter;

import java.util.List;
import java.util.UUID;

@Service
public class UserConverter extends BaseConverter {

    private static final String DUMMY_PWD = "dummy1234";

    public UserConverter() {
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
        String externalId = StringUtils.isNotBlank(user.getPassword())
                ? user.getPassword()
                : "manual-" + UUID.randomUUID();
        userPO.setExternalId(externalId);
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
        updatedUserPO.setExternalId(currentUserPO.getExternalId());

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

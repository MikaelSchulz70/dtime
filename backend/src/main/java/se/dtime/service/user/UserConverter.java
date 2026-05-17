package se.dtime.service.user;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.User;
import se.dtime.service.BaseConverter;

import java.util.List;

@Service
public class UserConverter extends BaseConverter {

    public User toModel(UserPO userPO) {
        if (userPO == null) {
            return null;
        }

        return User.builder().id(userPO.getId()).
                firstName(userPO.getFirstName()).
                lastName(userPO.getLastName()).
                email(userPO.getEmail()).
                activationStatus(userPO.getActivationStatus()).
                userRole(userPO.getUserRole()).
                build();
    }

    public User[] toModel(List<UserPO> userPOList) {
        List<User> users = userPOList.stream()
                .map(this::toModel)
                .toList();
        return users.toArray(new User[0]);
    }
}

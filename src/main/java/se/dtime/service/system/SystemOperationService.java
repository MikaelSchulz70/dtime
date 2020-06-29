package se.dtime.service.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
public class SystemOperationService {
    @Autowired
    private EmailSender emailSender;
    @Autowired
    private UserRepository userRepository;

    public void sendMailReminder() {
        List<UserPO> userPOList = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
        userPOList.forEach(u -> emailSender.sendReminderEmail(u.getEmail()));
    }
}

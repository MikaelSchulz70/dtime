package se.dtime.service.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.ActivityPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.Activity;
import se.dtime.model.UserExt;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.ActivityRepository;
import se.dtime.repository.UserRepository;
import se.dtime.utils.UserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private ActivityConverter activityConverter;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActivityValidator activityValidator;

    public long addOrUpdate(Activity activity) {
        ActivityPO activityPO = activityConverter.toPO(activity);
        activityRepository.save(activityPO);
        return activityPO.getId();
    }

    public Activity[] getAll() {
        List<ActivityPO> activityPOS = activityRepository.findAll();
        activityPOS.sort((o1, o2) -> {
            if (o1.getVoters() == null && o2.getVoters() == null) {
                return 0;
            }

            if (o1.getVoters() == null) {
                return 1;
            }

            if (o2.getVoters() == null) {
                return -1;
            }

            return Integer.compare(o2.getVoters().size(), o1.getVoters().size());
        });

        return activityConverter.toModel(activityPOS);
    }

    public void reset() {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        if (!isAdmin) {
            throw new ValidationException("activity.reset.allowed");
        }

        List<ActivityPO> activityPOS = activityRepository.findAll();
        activityPOS.forEach(a -> a.setVoters(new ArrayList<>()));
        activityRepository.saveAll(activityPOS);
    }

    public void delete(long idActivity) {
        ActivityPO activityPO = activityRepository.findById(idActivity).orElse(null);
        if (activityPO == null) {
            return;
        }

        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        if (!isAdmin) {
            if (activityPO.getCreatedBy() != userExt.getId()) {
                throw new ValidationException("activity.delete.not.allowed");
            }
        }

        activityPO.setVoters(null);
        activityRepository.save(activityPO);
        activityRepository.deleteById(idActivity);
    }

    public void voteOrUnVote(long id) {
        ActivityPO activityPO = activityRepository.findById(id).orElse(null);
        if (activityPO == null) {
            return;
        }

        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<UserPO> userPOS = activityPO.getVoters();
        boolean isVoting = userPOS.stream().noneMatch(u -> u.getId() == userExt.getId());
        if (isVoting) {
            activityValidator.checkNumberOfVotes(userExt.getId());
            UserPO userPO = userRepository.findById(userExt.getId()).orElse(null);
            if (userPO != null) {
                userPOS.add(userPO);
            }
        } else {
            userPOS = userPOS.stream().filter(u -> u.getId() != userExt.getId()).collect(Collectors.toList());
        }

        activityPO.setVoters(userPOS);
        activityRepository.save(activityPO);
    }

}

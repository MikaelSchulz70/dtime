package se.dtime.service.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.ActivityPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.Activity;
import se.dtime.model.UserExt;
import se.dtime.repository.UserRepository;
import se.dtime.service.BaseConverter;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityConverter extends BaseConverter {
    @Autowired
    private UserRepository userRepository;

    public Activity toModel(ActivityPO activityPO) {
        if (activityPO == null) {
            return null;
        }

        UserPO userPO = userRepository.findById(activityPO.getCreatedBy()).orElse(null);
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasVoted = activityPO.getVoters() != null ?
                (activityPO.getVoters().stream().anyMatch(u -> u.getId() == userExt.getId())) : false;

        return Activity.builder().id(activityPO.getId()).
                description(activityPO.getDescription()).
                addedBy(userPO != null ? userPO.getFullName() : "").
                noOfVotes(activityPO.getVoters().size()).
                voted(hasVoted).
                build();
    }

    public ActivityPO toPO(Activity activity) {
        if (activity == null) {
            return null;
        }

        ActivityPO activityPO = new ActivityPO();
        activityPO.setId(activity.getId());
        activityPO.setDescription(activity.getDescription());
        updateBaseData(activityPO);

        return activityPO;
    }

    public Activity[] toModel(List<ActivityPO> activityPOS) {
        List<Activity> activities = activityPOS.stream().map(c -> toModel(c)).collect(Collectors.toList());
        return activities.toArray(new Activity[activities.size()]);
    }
}

package se.dtime.service.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.ActivityPO;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.ActivityRepository;
import se.dtime.repository.SystemPropertyRepository;

import java.util.List;

@Slf4j
@Service
public class ActivityValidator {
    private final static int DEFAULT_NO_OF_VOTES = 5;
    private final static String NO_OF_VOTES_PROP = "Number of activity votes per user";

    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private SystemPropertyRepository systemPropertyRepository;


    public void checkNumberOfVotes(long userId) {
        SystemPropertyPO systemPropertyPO = systemPropertyRepository.findByName(NO_OF_VOTES_PROP);
        int noOfVotes = DEFAULT_NO_OF_VOTES;
        try {
            noOfVotes = Integer.parseInt(systemPropertyPO.getValue());
        } catch (NumberFormatException e) {
            log.error("Failed to get system property " + NO_OF_VOTES_PROP, e);
        }

        List<ActivityPO> activityPOS = activityRepository.findByVoters_Id(userId);

        if (activityPOS.size() >= noOfVotes) {
            throw new ValidationException("activity.max.number.of.votes");
        }
    }
}

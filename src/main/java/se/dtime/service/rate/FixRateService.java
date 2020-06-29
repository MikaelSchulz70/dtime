package se.dtime.service.rate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.FixRatePO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.FixRate;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.FixRateRepository;
import se.dtime.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FixRateService {

    @Autowired
    private FixRateRepository fixRateRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FixRateConverter fixRateConverter;
    @Autowired
    private FixRateValidator fixRateValidator;

    public void add(FixRate fixRate) {
        fixRateValidator.validateAdd(fixRate);
        FixRatePO fixRatePO = fixRateConverter.toPO(fixRate);
        fixRateRepository.save(fixRatePO);
    }

    public void update(FixRate fixRate) {
        fixRateValidator.validateUpdate(fixRate);
        FixRatePO updatedRate = fixRateConverter.toPO(fixRate);
        fixRateRepository.save(updatedRate);
    }

    public FixRate[] getCurrentFixRates() {
        List<ProjectPO> projectPOS = projectRepository.findByActivationStatus(ActivationStatus.ACTIVE).
                stream().filter(p -> !p.isInternal() && p.isFixRate()).collect(Collectors.toList());
        List<FixRatePO> fixRatePOS = fixRateRepository.findCurrentFixRates();
        return fixRateConverter.toModel(projectPOS, fixRatePOS);
    }

    public FixRate[] getRatesForProject(long idProject) {
        ProjectPO projectPO = projectRepository.findById(idProject).orElseThrow(() -> new NotFoundException("project.not.found"));
        List<FixRatePO> fixRatePOS = fixRateRepository.findByProjectOrderByFromDateDesc(projectPO);

        // Add empty rate for possible new rate
        FixRatePO fixRatePO = new FixRatePO(0L);
        fixRatePO.setProject(projectPO);
        fixRatePOS.add(fixRatePO);

        return fixRateConverter.toModel(fixRatePOS);
    }

    public FixRate get(long id) {
        FixRatePO fixRatePO = fixRateRepository.findById(id).orElseThrow(() -> new NotFoundException("rate.not.found"));
        return fixRateConverter.toModel(fixRatePO);
    }


    public void delete(long id) {
        fixRateValidator.validateDelete(id);
        fixRateRepository.deleteById(id);
    }
}

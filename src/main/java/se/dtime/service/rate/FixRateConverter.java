package se.dtime.service.rate;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.FixRatePO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.FixRate;
import se.dtime.service.BaseConverter;

import java.util.Comparator;
import java.util.List;

@Service
public class FixRateConverter extends BaseConverter {

    private static final Comparator<FixRate> RATE_COMPARATOR = Comparator.comparing(FixRate::getCompanyName).
            thenComparing(FixRate::getProjectName);

    private static final Comparator<FixRate> RATE_DATE_COMPARATOR = Comparator.comparing(FixRate::getFromDate);


    public FixRate toModel(FixRatePO fixRatePO) {
        if (fixRatePO == null) {
            return null;
        }

        return FixRate.builder().id(fixRatePO.getId()).
                companyName(fixRatePO.getProject().getCompany().getName()).
                projectName(fixRatePO.getProject().getName()).
                fromDate(fixRatePO.getFromDate()).
                toDate(fixRatePO.getToDate()).
                rate(fixRatePO.getCustomerRate()).
                comment(fixRatePO.getComment())
                .build();
    }

    public FixRatePO toPO(FixRate fixRate) {
        if (fixRate == null) {
            return null;
        }

        FixRatePO fixRatePO = new FixRatePO();
        fixRatePO.setId(fixRate.getId());
        fixRatePO.setProject(new ProjectPO(fixRate.getIdProject()));
        fixRatePO.setFromDate(fixRate.getFromDate());
        fixRatePO.setToDate(fixRate.getToDate());
        fixRatePO.setCustomerRate(fixRate.getRate());
        fixRatePO.setComment(fixRate.getComment());
        updateBaseData(fixRatePO);
        return fixRatePO;
    }

    private FixRate toModel(ProjectPO projectPO, FixRatePO fixRatePO) {
        return FixRate.builder().
                companyName(projectPO.getCompany().getName()).
                projectName(projectPO.getName()).
                idProject(projectPO.getId()).
                fromDate(fixRatePO != null ? fixRatePO.getFromDate() : null).
                toDate(fixRatePO != null ? fixRatePO.getToDate() : null).
                rate(fixRatePO != null ? fixRatePO.getCustomerRate() : null).
                comment(fixRatePO != null ? fixRatePO.getComment() : null).
                build();
    }

    private FixRate toModelPO(FixRatePO fixRatePO) {
        return FixRate.builder().
                id(fixRatePO.getId()).
                companyName(fixRatePO.getProject().getCompany().getName()).
                projectName(fixRatePO.getProject().getName()).
                idProject(fixRatePO.getProject().getId()).
                fromDate(fixRatePO.getFromDate()).
                toDate(fixRatePO.getToDate()).
                rate(fixRatePO.getCustomerRate()).
                comment(fixRatePO.getComment()).
                build();
    }

    public FixRate[] toModel(List<ProjectPO> projectPOS, List<FixRatePO> currentFixRatePOS) {
        return projectPOS.
                stream().
                map(p -> toModel(p, currentFixRatePOS.
                        stream().
                        filter(r -> r.getProject().getId().equals(p.getId())).
                        findFirst().
                        orElse(null))).
                sorted(RATE_COMPARATOR).
                toArray(FixRate[]::new);
    }

    public FixRate[] toModel(List<FixRatePO> fixRatePOS) {
        return fixRatePOS.
                stream().
                map(r -> toModelPO(r)).
                sorted((d1, d2) -> {
                    if (d1.getFromDate() == null) {
                        return -1;
                    }

                    return d2.getFromDate().compareTo(d1.getFromDate());
                }).
                toArray(FixRate[]::new);
    }

}

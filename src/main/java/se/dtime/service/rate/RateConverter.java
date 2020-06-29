package se.dtime.service.rate;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.RatePO;
import se.dtime.model.Rate;
import se.dtime.model.UserCategory;
import se.dtime.service.BaseConverter;

import java.util.Comparator;
import java.util.List;

@Service
public class RateConverter extends BaseConverter {

    private static final Comparator<Rate> RATE_COMPARATOR = Comparator.comparing(Rate::getCompanyName).
            thenComparing(Rate::getProjectName).
            thenComparing(Rate::getUserName);

    private static final Comparator<Rate> RATE_DATE_COMPARATOR = Comparator.comparing(Rate::getFromDate);


    public Rate toModel(RatePO ratePO) {
        if (ratePO == null) {
            return null;
        }

        return Rate.builder().id(ratePO.getId()).
                companyName(ratePO.getAssignment().getProject().getCompany().getName()).
                projectName(ratePO.getAssignment().getProject().getName()).
                userName(ratePO.getAssignment().getUser().getFullName()).
                fromDate(ratePO.getFromDate()).
                toDate(ratePO.getToDate()).
                customerRate(ratePO.getCustomerRate()).
                subcontractorRate(ratePO.getSubcontractorRate()).
                comment(ratePO.getComment())
                .build();
    }

    public RatePO toPO(Rate rate) {
        if (rate == null) {
            return null;
        }

        RatePO ratePO = new RatePO();
        ratePO.setId(rate.getId());
        ratePO.setAssignment(new AssignmentPO(rate.getIdAssignment()));
        ratePO.setFromDate(rate.getFromDate());
        ratePO.setToDate(rate.getToDate());
        ratePO.setCustomerRate(rate.getCustomerRate());
        ratePO.setSubcontractorRate(rate.getSubcontractorRate());
        ratePO.setComment(rate.getComment());
        updateBaseData(ratePO);
        return ratePO;
    }

    private Rate toModel(AssignmentPO assignmentPO, RatePO ratePO) {
        return Rate.builder().
                userName(assignmentPO.getUser().getFullName()).
                companyName(assignmentPO.getProject().getCompany().getName()).
                projectName(assignmentPO.getProject().getName()).
                idAssignment(assignmentPO.getId()).
                idUser(assignmentPO.getUser().getId()).
                isSubcontractor(assignmentPO.getUser().getUserCategory() == UserCategory.SUBCONTRACTOR).
                fromDate(ratePO != null ? ratePO.getFromDate() : null).
                toDate(ratePO != null ? ratePO.getToDate() : null).
                customerRate(ratePO != null ? ratePO.getCustomerRate() : null).
                subcontractorRate(ratePO != null ? ratePO.getSubcontractorRate() : null).
                comment(ratePO != null ? ratePO.getComment() : null).
                build();
    }

    private Rate toModelPO(RatePO ratePO) {
        return Rate.builder().
                id(ratePO.getId()).
                userName(ratePO.getAssignment().getUser().getFullName()).
                companyName(ratePO.getAssignment().getProject().getCompany().getName()).
                projectName(ratePO.getAssignment().getProject().getName()).
                idAssignment(ratePO.getAssignment().getId()).
                idUser(ratePO.getAssignment().getUser().getId()).
                isSubcontractor(ratePO.getAssignment().getUser().getUserCategory() == UserCategory.SUBCONTRACTOR).
                fromDate(ratePO.getFromDate()).
                toDate(ratePO.getToDate()).
                customerRate(ratePO.getCustomerRate()).
                subcontractorRate(ratePO.getSubcontractorRate()).
                comment(ratePO.getComment()).
                build();
    }

    public Rate[] toModel(List<AssignmentPO> assignmentPOS, List<RatePO> currentRatePOS) {
        return assignmentPOS.
                stream().
                map(a -> toModel(a, currentRatePOS.
                        stream().
                        filter(r -> r.getAssignment().getId().equals(a.getId())).
                        findFirst().
                        orElse(null))).
                sorted(RATE_COMPARATOR).
                toArray(Rate[]::new);
    }

    public Rate[] toModel(List<RatePO> ratePOS) {
        return ratePOS.
                stream().
                map(r -> toModelPO(r)).
                sorted((d1, d2) -> {
                    if (d1.getFromDate() == null) {
                        return -1;
                    }

                    return d2.getFromDate().compareTo(d1.getFromDate());
                }).
                toArray(Rate[]::new);
    }

}

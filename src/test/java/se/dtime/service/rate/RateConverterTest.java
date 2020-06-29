package se.dtime.service.rate;

import org.junit.Test;
import se.dtime.dbmodel.*;
import se.dtime.dbmodel.*;
import se.dtime.model.Rate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RateConverterTest {

    private RateConverter rateConverter = new RateConverter();

    @Test
    public void toModelTest() {
        RatePO ratePO1 = createRate(1, LocalDate.now(), LocalDate.now().plusDays(4));
        RatePO ratePO2 = createRate(2, LocalDate.now().plusDays(5), null);
        RatePO ratePO3 = createRate(3, LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
        RatePO ratePO4 = createRate(4, null, null);
        List<RatePO> ratePOList = new ArrayList<>();
        ratePOList.add(ratePO1);
        ratePOList.add(ratePO2);
        ratePOList.add(ratePO3);
        ratePOList.add(ratePO4);

        Rate[] rates = rateConverter.toModel(ratePOList);
        assertEquals(4, (long) rates[0].getId());
        assertEquals(2, (long) rates[1].getId());
        assertEquals(1, (long) rates[2].getId());
        assertEquals(3, (long) rates[3].getId());
    }

    private RatePO createRate(long id, LocalDate fromDate, LocalDate toDate) {
        RatePO ratePO = new RatePO(id);
        ratePO.setFromDate(fromDate);
        ratePO.setToDate(toDate);
        AssignmentPO assignmentPO = new AssignmentPO(1);
        assignmentPO.setUser(new UserPO(1L));
        CompanyPO companyPO = new CompanyPO();
        ProjectPO projectPO = new ProjectPO();
        projectPO.setCompany(companyPO);
        assignmentPO.setProject(projectPO);
        ratePO.setAssignment(assignmentPO);
        return ratePO;
    }
}
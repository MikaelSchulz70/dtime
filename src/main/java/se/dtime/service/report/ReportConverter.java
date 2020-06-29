package se.dtime.service.report;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.timereport.CloseDate;
import se.dtime.service.BaseConverter;

import java.time.LocalDate;

@Service
public class ReportConverter extends BaseConverter {

    public CloseDatePO toPO(CloseDate closeDate) {
        LocalDate date = LocalDate.of(closeDate.getCloseDate().getYear(), closeDate.getCloseDate().getMonth(), 1);
        CloseDatePO closeDatePO = new CloseDatePO();
        closeDatePO.setUser(new UserPO(closeDate.getIdUser()));
        closeDatePO.setDate(date);
        updateBaseData(closeDatePO);
        return closeDatePO;
    }
}

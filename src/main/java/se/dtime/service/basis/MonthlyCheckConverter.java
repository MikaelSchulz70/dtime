package se.dtime.service.basis;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.MonthlyCheckPO;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.service.BaseConverter;

@Service
public class MonthlyCheckConverter extends BaseConverter {

    public MonthlyCheck toModel(MonthlyCheckPO monthlyCheckPO) {
        if (monthlyCheckPO == null) {
            return null;
        }

        return MonthlyCheck.builder().id(monthlyCheckPO.getId()).
                companyId(monthlyCheckPO.getCompany().getId()).
                date(monthlyCheckPO.getDate()).
                invoiceSent(monthlyCheckPO.isInvoiceSent()).
                invoiceVerified(monthlyCheckPO.isInvoiceVerified()).
                build();
    }

    public MonthlyCheckPO toPO(MonthlyCheck monthlyCheck) {
        if (monthlyCheck == null) {
            return null;
        }

        MonthlyCheckPO monthlyCheckPO = new MonthlyCheckPO();
        monthlyCheckPO.setId(monthlyCheck.getId());
        monthlyCheckPO.setCompany(new CompanyPO(monthlyCheck.getCompanyId()));
        monthlyCheckPO.setDate(monthlyCheck.getDate());
        monthlyCheckPO.setInvoiceSent(monthlyCheck.isInvoiceSent());
        monthlyCheckPO.setInvoiceVerified(monthlyCheck.isInvoiceVerified());
        updateBaseData(monthlyCheckPO);
        return monthlyCheckPO;
    }
}

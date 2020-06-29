package se.dtime.service.company;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.model.Company;
import se.dtime.service.BaseConverter;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyConverter extends BaseConverter {

    public Company toModel(CompanyPO companyPO) {
        if (companyPO == null) {
            return null;
        }

        return Company.builder().id(companyPO.getId()).
                name(companyPO.getName()).
                activationStatus(companyPO.getActivationStatus()).
                build();
    }

    public CompanyPO toPO(Company company) {
        if (company == null) {
            return null;
        }

        CompanyPO companyPO = new CompanyPO();
        companyPO.setId(company.getId());
        companyPO.setName(company.getName());
        companyPO.setActivationStatus(company.getActivationStatus());
        updateBaseData(companyPO);

        return companyPO;
    }

    public Company[] toModel(List<CompanyPO> companyPOList) {
        List<Company> companies = companyPOList.stream().map(c -> toModel(c)).collect(Collectors.toList());
        return companies.toArray(new Company[companies.size()]);
    }
}

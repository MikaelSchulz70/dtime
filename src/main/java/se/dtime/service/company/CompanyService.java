package se.dtime.service.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Company;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.CompanyRepository;

import java.util.List;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CompanyConverter companyConverter;
    @Autowired
    private CompanyValidator companyValidator;

    public Company add(Company company) {
        companyValidator.validateAdd(company);
        CompanyPO companyPO = companyConverter.toPO(company);
        CompanyPO savedPO = companyRepository.save(companyPO);
        return companyConverter.toModel(savedPO);
    }

    public void update(Company company) {
        companyValidator.validateUpdate(company);
        CompanyPO companyPO = companyConverter.toPO(company);
        companyRepository.save(companyPO);
    }

    public Company[] getAll(Boolean active) {
        List<CompanyPO> companyPOS;

        if (active == null) {
            companyPOS = companyRepository.findAll(Sort.by("name").ascending());
        } else if (active) {
            companyPOS = companyRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.ACTIVE);
        } else {
            companyPOS = companyRepository.findByActivationStatusOrderByNameAsc(ActivationStatus.INACTIVE);
        }

        return companyConverter.toModel(companyPOS);
    }

    public Company get(long id) {
        CompanyPO companyPO = companyRepository.findById(id).orElseThrow(() -> new NotFoundException("company.not.found"));
        return companyConverter.toModel(companyPO);
    }

    public void delete(long id) {
        companyValidator.validateDelete(id);
        companyRepository.deleteById(id);
    }
}

package se.dtime.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.Project;
import se.dtime.service.BaseConverter;
import se.dtime.service.company.CompanyConverter;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectConverter extends BaseConverter {
    @Autowired
    private CompanyConverter companyConverter;

    public Project toModel(ProjectPO projectPO) {
        if (projectPO == null) {
            return null;
        }

        return Project.builder().id(projectPO.getId()).
                name(projectPO.getName()).
                activationStatus(projectPO.getActivationStatus()).
                provision(projectPO.isProvision()).
                company(companyConverter.toModel(projectPO.getCompany())).
                internal(projectPO.isInternal()).
                onCall(projectPO.isOnCall()).
                fixRate(projectPO.isFixRate()).
                projectCategory(projectPO.getProjectCategory()).
                build();
    }

    public ProjectPO toPO(Project project) {
        if (project == null) {
            return null;
        }

        ProjectPO projectPO = new ProjectPO();
        projectPO.setId(project.getId());
        projectPO.setName(project.getName());
        projectPO.setProvision(project.isProvision());
        projectPO.setInternal(project.isInternal());
        projectPO.setOnCall(project.isOnCall());
        projectPO.setFixRate(project.isFixRate());
        projectPO.setProjectCategory(project.getProjectCategory());
        projectPO.setActivationStatus(project.getActivationStatus());
        updateBaseData(projectPO);

        CompanyPO companyPO = new CompanyPO();
        companyPO.setId(project.getCompany().getId());
        projectPO.setCompany(companyPO);

        return projectPO;
    }

    public Project[] toModel(List<ProjectPO> projectPOList) {
        List<Project> projects = projectPOList.stream().map(c -> toModel(c)).collect(Collectors.toList());
        return projects.toArray(new Project[projects.size()]);
    }
}

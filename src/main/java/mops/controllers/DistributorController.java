package mops.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import mops.model.Account;
import mops.model.classes.Applicant;
import mops.model.classes.Application;
import mops.model.classes.Distribution;
import mops.model.classes.Evaluation;
import mops.model.classes.webclasses.WebDistribution;
import mops.model.classes.webclasses.WebDistributorApplicant;
import mops.model.classes.webclasses.WebDistributorApplication;
import mops.services.ApplicationService;
import mops.services.DistributionService;
import mops.services.EvaluationService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SessionScope
@Controller
@RequestMapping("/bewerbung2/verteiler")
public class DistributorController {

    private final DistributionService distributionService;
    private final ApplicationService applicationService;
    private final EvaluationService evaluationService;


    /**
     * Constructor
     * @param distributionService
     * @param applicationService
     * @param evaluationService
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public DistributorController(final DistributionService distributionService,
                                 final ApplicationService applicationService,
                                 final EvaluationService evaluationService) {
        this.distributionService = distributionService;
        this.applicationService = applicationService;
        this.evaluationService = evaluationService;
    }

    private Account createAccountFromPrincipal(final KeycloakAuthenticationToken token) {
        KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
        return new Account(
                principal.getName(),
                principal.getKeycloakSecurityContext().getIdToken().getEmail(),
                null,
                token.getAccount().getRoles());
    }


    /**
     * The GepMapping for the main page
     *
     * @param token The KeycloakAuthentication
     * @param model The Website model
     * @return The HTML file rendered as a String
     */
    @GetMapping("/")
    @Secured("ROLE_verteiler")
    public String index1(final KeycloakAuthenticationToken token, final Model model) throws JsonProcessingException {
        if (token != null) {
            model.addAttribute("account", createAccountFromPrincipal(token));
            List<WebDistribution> webDistributionList = new ArrayList<>();
            List<Distribution> distributionList = distributionService.findAll();
            for (Distribution distribution : distributionList) {
                List<WebDistributorApplicant> webDistributorApplicantList = new ArrayList<>();
                List<Applicant> applicantList = distribution.getEmployees();
                for (Applicant applicant : applicantList) {
                    List<WebDistributorApplication> webDistributorApplicationList = new ArrayList<>();
                    Set<Application> applicationList = applicant.getApplications();
                    for (Application value : applicationList) {
                        Evaluation evaluation = evaluationService.findByApplication(value);
                        WebDistributorApplication webDistributorApplication = WebDistributorApplication.builder()
                                .applicantPriority(value.getPriority() + "")
                                .minHours(value.getMinHours() + "")
                                .maxHours(value.getMaxHours() + "")
                                .module(value.getModule())
                                .organizerHours(evaluation.getHours() + "")
                                .organizerPriority(evaluation.getPriority() + "")
                                .build();
                        webDistributorApplicationList.add(webDistributorApplication);
                    }
                    WebDistributorApplicant webDistributorApplicant = WebDistributorApplicant.builder()
                            .username(applicant.getUniserial())
                            .webDistributorApplications(webDistributorApplicationList)
                            .build();
                    webDistributorApplicantList.add(webDistributorApplicant);
                }
                WebDistribution webDistribution = WebDistribution.builder()
                        .module(distribution.getModule())
                        .webDistributorApplicants(webDistributorApplicantList)
                        .build();
                webDistributionList.add(webDistribution);
            }
            model.addAttribute("distributions", webDistributionList);
        }
        return "distributor/distributorMain";
    }
}

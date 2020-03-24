package mops.services;

import mops.model.classes.Address;
import mops.model.classes.Applicant;
import mops.model.classes.Applicant.ApplicantBuilder;
import mops.model.classes.Application;
import mops.model.classes.Certificate;
import mops.model.classes.Module;
import mops.model.classes.webclasses.WebAddress;
import mops.model.classes.webclasses.WebApplicant;
import mops.model.classes.webclasses.WebCertificate;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@SuppressWarnings("checkstyle:HiddenField")
public class StudentService {

    private ApplicantService applicantService;


    /**
     * Setup the applicantserives
     *
     * @param applicantService   applicant.
     * @param applicationService application
     */
    public StudentService(final ApplicantService applicantService, final ApplicationService applicationService) {
        this.applicantService = applicantService;
    }

    /**
     * builds Address from webAddress
     *
     * @param webAddress Address Information
     * @return address
     */
    public Address buildAddress(final WebAddress webAddress) {
        return Address.builder()
                .street(webAddress.getStreet())
                .houseNumber(webAddress.getNumber())
                .city(webAddress.getCity())
                .zipcode(webAddress.getZipcode())
                .build();
    }

    /**
     * builds Certificate from webCertificate
     *
     * @param webCertificate informatons
     * @return fully build Certificate
     */
    public Certificate buildCertificate(final WebCertificate webCertificate) {
        return Certificate.builder()
                .name(webCertificate.getGraduation())
                .course(webCertificate.getCourse())
                .build();
    }

    /**
     * builds Applicant from webApplicant with Address and uniserial as ID
     *
     * @param uniserial    the ID (Name)
     * @param webApplicant Applicant Information
     * @param address      builded Address
     * @param certificate  certificate (highest)
     * @param givenName    first name.
     * @param familyName   surname.
     * @return fully functional Applicant
     */
    public Applicant buildApplicant(final String uniserial, final WebApplicant webApplicant,
                                    final Address address, final Certificate certificate, final String givenName,
                                    final String familyName) {

        Applicant applicant = applicantService.findByUniserial(uniserial);

        if (applicant == null) {

            return Applicant.builder()
                    .uniserial(uniserial)
                    .firstName(givenName)
                    .surname(familyName)
                    .address(address)
                    .birthday(webApplicant.getBirthday())
                    .birthplace(webApplicant.getBirthplace())
                    .gender(webApplicant.getGender())
                    .nationality(webApplicant.getNationality())
                    .course(webApplicant.getCourse())
                    .status(webApplicant.getStatus())
                    .certs(certificate)
                    .comment(webApplicant.getComment())
                    .build();
        }
        ApplicantBuilder applicantBuilder = applicant.toBuilder();
        return applicantBuilder.birthday(webApplicant.getBirthday())
                .birthplace(webApplicant.getBirthplace())
                .gender(webApplicant.getGender())
                .nationality(webApplicant.getNationality())
                .course(webApplicant.getCourse())
                .status(webApplicant.getStatus())
                .certs(certificate)
                .comment(webApplicant.getComment())
                .build();
    }

    /**
     * Updates Applicant without changing his applications
     *
     * @param newApplicant The Object containing the new information
     */
    public void updateApplicantWithoutChangingApplications(final Applicant newApplicant) {
        Applicant oldApplicant = applicantService.findByUniserial(newApplicant.getUniserial());
        applicantService.saveApplicant(Applicant.builder()
                .applications(oldApplicant.getApplications())
                .uniserial(newApplicant.getUniserial())
                .certs(newApplicant.getCerts())
                .status(newApplicant.getStatus())
                .course(newApplicant.getCourse())
                .nationality(newApplicant.getComment())
                .birthday(newApplicant.getBirthday())
                .address(newApplicant.getAddress())
                .birthplace(newApplicant.getBirthplace())
                .comment(newApplicant.getComment())
                .surname(newApplicant.getSurname())
                .firstName(newApplicant.getFirstName())
                .gender(newApplicant.getGender())
                .build());
    }

    /**
     * Returns WepApplicant for Applicant.
     *
     * @param applicant applicant.
     * @return WebApplicant.
     */
    public WebApplicant getExsistingApplicant(final Applicant applicant) {
        return WebApplicant.builder()
                .birthday(applicant.getBirthday())
                .birthplace(applicant.getBirthplace())
                .comment(applicant.getComment())
                .course(applicant.getCourse())
                .gender(applicant.getGender())
                .nationality(applicant.getNationality())
                .status(applicant.getStatus())
                .build();
    }

    /**
     * Returns web version of Address.
     *
     * @param address address.
     * @return webaddress.
     */
    public WebAddress getExsistingAddress(final Address address) {
        return WebAddress.builder()
                .city(address.getCity())
                .number(address.getHouseNumber())
                .street(address.getStreet())
                .zipcode(address.getZipcode())
                .build();
    }

    /**
     * Returns web version of certiuficate
     *
     * @param certificate certificate
     * @return webcertificate
     */
    public WebCertificate getExsistingCertificate(final Certificate certificate) {
        return WebCertificate.builder()
                .course(certificate.getCourse())
                .graduation(certificate.getName())
                .build();
    }

    /**
     * Returns a Set of all Modules the Applicant has not submitted an application yet.
     *
     * @param applicant Applicant.
     * @param modules   all Modules
     * @return Set of Modules.
     */
    public List<Module> getAllNotfilledModules(final Applicant applicant, final List<Module> modules) {
        for (Application app : applicant.getApplications()) {
            modules.remove(app.getModule());
        }
        return modules;
    }
}

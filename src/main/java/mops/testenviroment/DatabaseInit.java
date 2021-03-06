package mops.testenviroment;

import mops.model.classes.Address;
import mops.model.classes.Applicant;
import com.github.javafaker.Faker;
import mops.model.classes.Application;
import mops.model.classes.Certificate;
import mops.model.classes.Distribution;
import mops.model.classes.Evaluation;
import mops.model.classes.Module;
import mops.model.classes.Priority;
import mops.model.classes.Role;
import mops.repositories.ApplicantRepository;
import mops.repositories.ApplicationRepository;
import mops.repositories.DistributionRepository;
import mops.repositories.EvaluationRepository;
import mops.repositories.ModuleRepository;
import mops.services.CSVService;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("checkstyle:MagicNumber")
@Component
public class DatabaseInit implements ServletContextInitializer {

    private static final boolean DUMMY_DATA_CREATION = false;
    private static final int ENTRYNUMBER = 100;
    private transient Random random = new Random();
    private transient ApplicantRepository applicantRepository;
    private transient ApplicationRepository applicationRepository;
    private transient DistributionRepository distributionRepository;
    private transient EvaluationRepository evaluationRepository;
    private transient ModuleRepository moduleRepository;


    /**
     * Inits.
     *
     * @param applicantRepository    a
     * @param applicationRepository  b
     * @param distributionRepository c
     * @param evaluationRepository   d
     * @param moduleRepository       e
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public DatabaseInit(final ApplicantRepository applicantRepository,
                        final ApplicationRepository applicationRepository,
                        final DistributionRepository distributionRepository,
                        final EvaluationRepository evaluationRepository,
                        final ModuleRepository moduleRepository) {
        this.applicantRepository = applicantRepository;
        this.applicationRepository = applicationRepository;
        this.distributionRepository = distributionRepository;
        this.evaluationRepository = evaluationRepository;
        this.moduleRepository = moduleRepository;
    }


    /**
     * On statup function.
     *
     * @param servletContext context.
     */
    public void onStartup(final ServletContext servletContext) {
        if (DUMMY_DATA_CREATION) {
            Faker faker = new Faker(Locale.GERMAN);
            fakeModules(faker);
            fakeApplicants(faker);
            fakeEvaluations(faker);
            fakeDistribution();
        }
    }

    /**
     * Fakes Applicant entrys.
     *
     * @param faker faker.
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public void fakeApplicants(final Faker faker) {
        for (int i = 0; i < ENTRYNUMBER; i++) {
            String country = CSVService.getCountries().get(random.nextInt(255));
            Address address = Address.builder()
                    .street(faker.address().streetName())
                    .houseNumber(faker.address().buildingNumber())
                    .city(faker.address().city())
                    .country(country)
                    .zipcode(faker.address().zipCode())
                    .build();

            Certificate certificate = Certificate.builder()
                    .course(faker.job().field())
                    .name(getCertName())
                    .build();
            Module[] modules = nextModules();
            int[] hours = nextHours();

            Application application1 = Application.builder()
                    .module(modules[0])
                    .minHours(hours[0])
                    .maxHours(hours[1])
                    .finalHours(nextFinalHour())
                    .lecturer(faker.name().fullName())
                    .grade(nextGrade())
                    .semester("SS2020")
                    .comment(truncate(faker.rickAndMorty().quote(), 255))
                    .role(getRole())
                    .priority(nextPriorityApplicant())
                    .build();

            Application application2 = Application.builder()
                    .module(modules[1])
                    .minHours(hours[0])
                    .maxHours(hours[1])
                    .finalHours(nextFinalHour())
                    .lecturer(faker.name().fullName())
                    .grade(nextGrade())
                    .semester("SS2020")
                    .comment(truncate(faker.rickAndMorty().quote(), 255))
                    .role(getRole())
                    .priority(nextPriorityApplicant())
                    .build();
            String surename = faker.name().lastName();
            String firstname = faker.name().firstName();

            Applicant applicant = Applicant.builder()
                    .uniserial(truncate(firstname, 2) + truncate(surename, 3) + faker.number().digits(3))
                    .firstName(firstname)
                    .surname(surename)
                    .comment(truncate(faker.yoda().quote(), 255))
                    .course(faker.educator().course())
                    .nationality(faker.nation().nationality())
                    .birthday(new SimpleDateFormat("yyyy-mm-dd").format(faker.date().birthday()))
                    .status(getStatus())
                    .application(application1)
                    .application(application2)
                    .birthplace(faker.address().country())
                    .gender(nextGender())
                    .certs(certificate)
                    .address(address)
                    .checked(false)
                    .build();
            applicantRepository.save(applicant);
        }
        applicantRepository.save(createMainRole("studentin", faker));

    }

    private Module[] nextModules() {
        long value = random.nextInt(5) + 1;
        long value2 = random.nextInt(5) + 1;
        while (value == value2) {
            value2 = random.nextInt(5) + 1;
        }
        Module[] modules = new Module[2];
        modules[0] = moduleRepository.findById(value).get();
        modules[1] = moduleRepository.findById(value2).get();
        return modules;
    }

    private String nextGender() {
        if (random.nextBoolean()) {
            return "männlich";
        }
        return "weiblich";
    }

    private Priority nextPriority() {
        Priority prio;
        switch (random.nextInt(4)) {
            case 0:
                prio = Priority.VERYHIGH;
                break;
            case 1:
                prio = Priority.HIGH;
                break;
            case 2:
                prio = Priority.NEUTRAL;
                break;
            default:
                prio = Priority.NEGATIVE;
                break;
        }
        return prio;
    }

    private Priority nextPriorityApplicant() {
        Priority prio;
        switch (random.nextInt(3)) {
            case 0:
                prio = Priority.VERYHIGH;
                break;
            case 1:
                prio = Priority.HIGH;
                break;
            default:
                prio = Priority.NEUTRAL;
                break;
        }
        return prio;
    }

    private int nextFinalHour() {
        int[] hours = {7, 9, 17};
        return hours[random.nextInt(3)];
    }

    private int[] nextHours() {
        int[] hours = {7, 9, 17};
        int[] ret = new int[2];
        int x = random.nextInt(3);
        ret[0] = hours[x];
        int y = x + random.nextInt(3 - x);
        ret[1] = hours[y];
        return ret;
    }

    private double nextGrade() {
        double[] grades = {1.0, 1.3, 1.7, 2.0, 2.3, 2.7, 3.0, 3.3, 3.7, 4.0, 5.0};
        return grades[random.nextInt(11)];
    }

    private String getStatus() {
        String ret;
        if (random.nextInt(3) == 0) {
            ret = "Einstellung";
        } else {
            ret = "Weiterbeschäftigung";
        }
        return ret;
    }

    private String getCertName() {
        String[] arr = {"Keins", "Bachelor", "Master", "Diplom", "Staatsexamen", "Anderes"};
        if (random.nextInt(5) > 1) {
            return arr[0];
        }
        return arr[random.nextInt(5) + 1];
    }

    private String truncate(final String s, final int num) {
        return s.substring(0, Math.min(s.length(), num));
    }

    private String getDate(final Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return simpleDateFormat.format(date);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private Role getRole() {
        Role ret;
        switch (random.nextInt(3)) {
            case 0:
                ret = Role.PROOFREADER;
                break;
            case 1:
                ret = Role.TUTOR;
                break;
            default:
                ret = Role.BOTH;
                break;
        }
        return ret;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private Applicant createMainRole(final String role, final Faker faker) {
        String country = CSVService.getCountries().get(random.nextInt(255));
        Address address = Address.builder()
                .street(faker.address().streetName())
                .houseNumber(faker.address().buildingNumber())
                .city(faker.address().city())
                .country(country)
                .zipcode(faker.address().zipCode())
                .build();

        Certificate certificate = Certificate.builder()
                .course(faker.job().field())
                .name(getCertName())
                .build();

        Module[] modules = nextModules();
        int[] hours = nextHours();

        Application application1 = Application.builder()
                .module(modules[0])
                .finalHours(nextFinalHour())
                .minHours(hours[0])
                .maxHours(hours[1])
                .lecturer(faker.name().fullName())
                .grade(nextGrade())
                .semester("SS2020")
                .comment(truncate(faker.rickAndMorty().quote(), 255))
                .role(getRole())
                .priority(nextPriorityApplicant())
                .build();

        Application application2 = Application.builder()
                .module(modules[1])
                .finalHours(nextFinalHour())
                .minHours(hours[0])
                .maxHours(hours[1])
                .lecturer(faker.name().fullName())
                .grade(nextGrade())
                .semester("SS2020")
                .comment(truncate(faker.rickAndMorty().quote(), 255))
                .role(getRole())
                .priority(nextPriorityApplicant())
                .build();

        return Applicant.builder()
                .uniserial(role)
                .firstName(role)
                .surname(role)
                .comment(truncate(faker.yoda().quote(), 255))
                .course("Informatik")
                .nationality(faker.nation().nationality())
                .birthday(new SimpleDateFormat("yyyy-mm-dd").format(faker.date().birthday()))
                .status("Einstellung")
                .application(application1)
                .application(application2)
                .birthplace(faker.address().country())
                .gender(nextGender())
                .certs(certificate)
                .address(address)
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void fakeDistribution() {
        int i = 0;
        List<Applicant> applications = applicantRepository.findAll();
        List<Distribution> all = new ArrayList<>();
        List<Module> modules = moduleRepository.findAll();
        int size = applications.size();
        int msize = modules.size();
        int part = size / msize;
        for (int x = 0; x < msize; x++) {
            Collection<Applicant> apps = new ArrayList<>();
            for (int j = 0; j < part; j++) {
                apps.add(applications.get(j + (x * part)));
            }
            Distribution distribution = Distribution.builder()
                    .module(modules.get(x))
                    .employees(apps)
                    .build();
            all.add(distribution);
        }
        distributionRepository.saveAll(all);

    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private void fakeEvaluations(final Faker faker) {
        applicationRepository.findAll().forEach(application -> {
                    Evaluation evaluation = Evaluation.builder()
                            .hours(nextFinalHour())
                            .priority(nextPriority())
                            .application(application)
                            .build();
                    evaluationRepository.save(evaluation);
                }
        );
    }

    @SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:HiddenField"})
    private void fakeModules(final Faker faker) {
        Random random = new Random();
        String[] modulenames = {"Programmier Praktikum 1", "Programmier Praktikum 2", "RDB",
                "Algorithmen und Datenstrukturen", "Theoretische Informatik"};
        String[] shortNames = {"ProPra1", "Propra2", "RDB", "Aldat", "Theo"};
        String[] profSerial = {"orga", "bewerbung2_all_roles", "bewerbung2_studentin_orga",
                "bewerbung2_verteiler_orga", "Stefan"};
        for (int i = 0; i < modulenames.length; i++) {
            SimpleDateFormat deadlineDatePattern = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat deadlineTimePattern = new SimpleDateFormat("hh:mm");
            String applicantDeadlineDate =
                    deadlineDatePattern.format(faker.date().future(300, 30, TimeUnit.DAYS));
            String applicantDeadlineTime =
                    deadlineTimePattern.format(faker.date().future(300, 30, TimeUnit.DAYS));
            String orgaDeadlineDate =
                    deadlineDatePattern.format(faker.date().future(300, 30, TimeUnit.DAYS));
            String orgaDeadlineTime =
                    deadlineTimePattern.format(faker.date().future(300, 30, TimeUnit.DAYS));
            LocalDateTime applicantDate =
                    LocalDateTime.parse(applicantDeadlineDate + "T" + applicantDeadlineTime + ":00");
            LocalDateTime orgaDate = LocalDateTime.parse(orgaDeadlineDate + "T" + orgaDeadlineTime + ":00");
            if (orgaDate.isBefore(applicantDate)) {
                LocalDateTime help = applicantDate;
                String helpTime = applicantDeadlineTime;
                String helpDate = applicantDeadlineDate;
                applicantDeadlineTime = orgaDeadlineTime;
                applicantDeadlineDate = orgaDeadlineDate;
                applicantDate = orgaDate;
                orgaDeadlineTime = helpTime;
                orgaDeadlineDate = helpDate;
                orgaDate = help;
            }
            Module module = Module.builder()
                    .name(modulenames[i])
                    .shortName(shortNames[i])
                    .profSerial(profSerial[i])
                    .applicantDeadlineDate(applicantDeadlineDate)
                    .applicantDeadlineTime(applicantDeadlineTime)
                    .orgaDeadlineDate(orgaDeadlineDate)
                    .orgaDeadlineTime(orgaDeadlineTime)
                    .applicantDeadline(applicantDate)
                    .orgaDeadline(orgaDate)
                    .sevenHourLimit((1 + random.nextInt(5)) + "")
                    .nineHourLimit((1 + random.nextInt(5)) + "")
                    .seventeenHourLimit((1 + random.nextInt(5)) + "")
                    .build();
            moduleRepository.save(module);
        }
    }
}

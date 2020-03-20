package mops.services;

import mops.model.classes.Application;
import mops.model.classes.Evaluation;
import mops.repositories.EvaluationRepository;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    /**
     * Injects repository
     * @param evaluationRepository the injected repository
     */
    @SuppressWarnings("checkstyle:HiddenField")
    public EvaluationService(final EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    /**
     * Finds Application by evaluation
     * @param application the application
     * @return the evaluation found
     */
    public Evaluation findByApplication(final Application application) {
        return evaluationRepository.findByApplication(application);
    }
}

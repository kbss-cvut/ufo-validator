package cz.cvut.kbss.ufo.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.progress.SimpleProgressMonitor;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.validation.ResourceValidationReport;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationUtil;

@Slf4j
public class Validator {

    private static Set<String> rules = new HashSet<>();

    static {
        for (int i = 1; i <= 6; i++) {
            rules.add("r" + i + ".ttl");
        }
    }

    public static Set<String> getRules() {
        return rules;
    }

    private Model getRulesModel(final Collection<String> rules) {
        final Model shapesModel = JenaUtil.createMemoryModel();
        rules.forEach(r -> shapesModel
            .read(Validator.class.getResourceAsStream("/" + r), null, FileUtils.langTurtle));
        return shapesModel;
    }

    /**
     * Validates the given model with vocabulary data (glossaries, models) against the given
     * ruleset.
     *
     * @param dataModel model with data to validate
     * @param ruleSet   set of rules (see 'resources') used for validation
     * @return validation report
     */
    public ValidationReport validate(final Model dataModel, final Set<String> ruleSet) {
        log.trace("Validating model {}", dataModel);
        final Model shapesModel = getRulesModel(ruleSet);

        Model inferredModel = RuleUtil
            .executeRules(dataModel, shapesModel, null, new SimpleProgressMonitor("inference"));
        dataModel.add(inferredModel);

        final Resource report = ValidationUtil.validateModel(dataModel, shapesModel, true);

        return new ResourceValidationReport(report);
    }
}

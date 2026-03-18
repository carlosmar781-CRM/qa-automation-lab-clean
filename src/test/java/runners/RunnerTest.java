package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {
                "pretty",
                "html:target/cucumber/cucumber-report.html",
                "json:target/cucumber/cucumber.json"
        },
        stepNotifications = true,
        features = "src/test/resources/features",
        glue = "steps"
)
public class RunnerTest {
}

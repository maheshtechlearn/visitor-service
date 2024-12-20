package com.mylogo.visitors.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.mylogo.visitors.cucumber",
        plugin = {"pretty", "json:target/cucumber.json", "html:target/cucumber.html"}
)
@ContextConfiguration(classes = {com.mylogo.visitors.config.RedisConfig.class, com.mylogo.visitors.config.JacksonConfig.class, com.mylogo.visitors.service.VisitorService.class})

public class CucumberTestRunner {
}

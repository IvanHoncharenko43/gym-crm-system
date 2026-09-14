package org.example.crm.user.steps;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@Getter
@Setter
@Component
@ScenarioScope
public class TestContext {
    private RequestPostProcessor securityProcessor;
    private ResultActions resultActions;
}

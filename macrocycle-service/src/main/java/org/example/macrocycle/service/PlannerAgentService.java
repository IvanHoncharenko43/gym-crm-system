package org.example.macrocycle.service;

import lombok.extern.slf4j.Slf4j;
import org.example.macrocycle.plan.MacrocyclePlan;
import org.example.macrocycle.plan.TraineeContextSnapshot;
import org.example.macrocycle.plan.VerifierIssue;
import org.example.macrocycle.repository.MacrocycleJobDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PlannerAgentService {

    private final ChatClient plannerChatClient;

    public PlannerAgentService(@Qualifier("plannerChatClient") ChatClient plannerChatClient) {
        this.plannerChatClient = plannerChatClient;
    }

    public MacrocyclePlan generate(MacrocycleJobDocument job, List<VerifierIssue> priorIssues) {
        log.debug("Requesting macrocycle plan draft for trainee {}", job.getTraineeUsername());
        String prompt = buildPrompt(job, priorIssues);
        MacrocyclePlan plan = plannerChatClient.prompt()
                .user(prompt)
                .call()
                .entity(MacrocyclePlan.class);
        log.debug("Received macrocycle plan draft with {} weeks", plan != null ? plan.weeks().size() : 0);
        return plan;
    }

    private String buildPrompt(MacrocycleJobDocument job, List<VerifierIssue> priorIssues) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Goal: ").append(job.getGoal()).append('\n');
        prompt.append("Target event date: ").append(job.getTargetEventDate()).append('\n');
        prompt.append("Weekly availability (days the trainee can train): ").append(job.getWeeklyAvailability()).append('\n');
        prompt.append("Preferred session duration (minutes): ").append(job.getPreferredSessionDurationMinutes()).append('\n');
        prompt.append("Injuries and constraints: ")
                .append(job.getInjuriesAndConstraints() == null || job.getInjuriesAndConstraints().isBlank()
                        ? "None reported" : job.getInjuriesAndConstraints())
                .append('\n');

        TraineeContextSnapshot context = job.getContextSnapshot();
        if (context != null && !context.recentTrainings().isEmpty()) {
            prompt.append("Recent training history:\n");
            context.recentTrainings().forEach(item -> prompt.append("- ")
                    .append(item.date()).append(": ")
                    .append(item.trainingTypeName()).append(", ")
                    .append(item.durationMinutes()).append(" min, \"")
                    .append(item.trainingName()).append("\"\n"));
        } else {
            prompt.append("Recent training history: none available\n");
        }

        prompt.append("Produce a full week-by-week plan spanning the number of weeks between now and the target event date.\n");

        if (priorIssues != null && !priorIssues.isEmpty()) {
            prompt.append("The previous draft had the following issues; revise the plan to resolve every one of them:\n");
            priorIssues.forEach(issue -> prompt.append("- [")
                    .append(issue.severity()).append("] week ")
                    .append(issue.weekNumber()).append(": ")
                    .append(issue.description()).append('\n'));
        }

        return prompt.toString();
    }
}

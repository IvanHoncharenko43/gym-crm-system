package org.example.macrocycle.service;

import lombok.extern.slf4j.Slf4j;
import org.example.macrocycle.plan.MacrocyclePlan;
import org.example.macrocycle.plan.PlannedSession;
import org.example.macrocycle.plan.PlannedWeek;
import org.example.macrocycle.plan.VerifierVerdict;
import org.example.macrocycle.repository.MacrocycleJobDocument;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VerifierAgentService {

    private final ChatClient verifierChatClient;

    public VerifierAgentService(@Qualifier("verifierChatClient") ChatClient verifierChatClient) {
        this.verifierChatClient = verifierChatClient;
    }

    public VerifierVerdict verify(MacrocycleJobDocument job, MacrocyclePlan plan) {
        log.debug("Verifying macrocycle plan draft for trainee {}", job.getTraineeUsername());
        String prompt = buildPrompt(job, plan);
        VerifierVerdict verdict = verifierChatClient.prompt()
                .user(prompt)
                .call()
                .entity(VerifierVerdict.class);
        log.debug("Verifier verdict approved={}", verdict != null && verdict.approved());
        return verdict;
    }

    private String buildPrompt(MacrocycleJobDocument job, MacrocyclePlan plan) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Trainee weekly availability: ").append(job.getWeeklyAvailability()).append('\n');
        prompt.append("Trainee preferred session duration (minutes): ").append(job.getPreferredSessionDurationMinutes()).append('\n');
        prompt.append("Trainee injuries and constraints: ")
                .append(job.getInjuriesAndConstraints() == null || job.getInjuriesAndConstraints().isBlank()
                        ? "None reported" : job.getInjuriesAndConstraints())
                .append('\n');
        prompt.append("Proposed plan:\n");
        for (PlannedWeek week : plan.weeks()) {
            prompt.append("Week ").append(week.weekNumber()).append(" (").append(week.focus()).append("):\n");
            for (PlannedSession session : week.sessions()) {
                prompt.append("  - ").append(session.dayOfWeek()).append(": ")
                        .append(session.trainingType()).append(" \"")
                        .append(session.title()).append("\", ")
                        .append(session.durationMinutes()).append(" min\n");
            }
        }
        prompt.append("Review this plan against the trainee's constraints and return your verdict.");
        return prompt.toString();
    }
}

package org.example.macrocycle.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String PLANNER_SYSTEM_PROMPT = """
            You are an expert strength & conditioning and endurance coach who designs long-form training macrocycles.
            Given a trainee's goal, target event date, weekly availability, injuries/constraints, and recent training
            history, produce a structured week-by-week training plan. Only use the training types STRENGTH, CARDIO,
            FLEXIBILITY, or YOGA for each session's type. Only schedule sessions on days present in the trainee's
            weekly availability. Respect the stated injuries/constraints by avoiding or modifying exercises that
            would aggravate them. Keep each session's duration close to the trainee's preferred session duration.
            If given prior feedback about issues in an earlier draft, revise the plan to resolve every issue listed.
            """;

    private static final String VERIFIER_SYSTEM_PROMPT = """
            You are a strict reviewer of AI-generated training macrocycles. You will be given a trainee's
            constraints (weekly availability, injuries, preferred session duration) and a proposed week-by-week
            plan. Check every session against the constraints: flag any session scheduled on a day outside the
            trainee's weekly availability, any session that plausibly aggravates a stated injury, and any session
            whose duration is wildly inconsistent with the trainee's preference. Be strict but fair: only raise an
            issue when there is a concrete, specific violation, not a stylistic preference. Return whether the plan
            is approved and, if not, a precise list of issues referencing the affected week number.
            """;

    @Bean
    public ChatClient plannerChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(PLANNER_SYSTEM_PROMPT)
                .build();
    }

    @Bean
    public ChatClient verifierChatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(VERIFIER_SYSTEM_PROMPT)
                .build();
    }
}

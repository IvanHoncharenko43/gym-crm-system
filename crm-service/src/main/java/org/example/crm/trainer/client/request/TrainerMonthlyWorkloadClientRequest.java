package org.example.crm.trainer.client.request;

public record TrainerMonthlyWorkloadClientRequest(
        String username,
        int year,
        int month
) {
}

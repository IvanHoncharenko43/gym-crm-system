package org.example.monitoring;

import org.example.trainingType.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingTypesHealthIndicatorTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypesHealthIndicator trainingTypesHealthIndicator;

    @Test
    void health_ReturnHealthUp_TrainingTypesExist() {
        long trainingTypesCount = 10L;
        when(trainingTypeRepository.count()).thenReturn(trainingTypesCount);

        Health result = trainingTypesHealthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.UP);
        assertThat(result.getDetails().get("message"))
                .isEqualTo("Training types are populated");
        assertThat(result.getDetails().get("count"))
                .isEqualTo(trainingTypesCount);
    }

    @Test
    void health_ReturnHealthDown_TableIsEmpty() {
        when(trainingTypeRepository.count()).thenReturn(0L);

        Health result = trainingTypesHealthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails().get("reason"))
                .isEqualTo("Missing static database constants - Training types");
    }

    @Test
    void health_ReturnHealthDown_DatabaseThrowsException() {
        String errorMessage = "Database connection pool timeout";
        when(trainingTypeRepository.count()).thenThrow(new RuntimeException(errorMessage));

        Health result = trainingTypesHealthIndicator.health();

        assertThat(result.getStatus()).isEqualTo(Status.DOWN);
        assertThat(result.getDetails().get("reason"))
                .isEqualTo("Database unreachable during check");
        assertThat(result.getDetails().get("error"))
                .isEqualTo(errorMessage);
    }
}

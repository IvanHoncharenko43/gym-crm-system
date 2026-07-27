package org.example.trainingType;

import org.example.trainingType.dto.TrainingType;
import org.example.trainingType.dto.TrainingTypes;
import org.example.trainingType.repository.TrainingTypeEntity;
import org.example.trainingType.repository.TrainingTypeRepository;
import org.example.trainingType.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void getAllTrainingTypes_ReturnMappedNames_DataIsPresent() {
        TrainingTypeEntity trainingType1 = new TrainingTypeEntity();
        trainingType1.setTrainingTypeName(TrainingType.YOGA);
        TrainingTypeEntity trainingType2 = new TrainingTypeEntity();
        trainingType2.setTrainingTypeName(TrainingType.CARDIO);
        List<TrainingTypeEntity> trainingTypes = List.of(trainingType1, trainingType2);

        when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);

        TrainingTypes result = trainingTypeService.getAllTrainingTypes();
        assertEquals(2, result.trainingTypes().size());
        verify(trainingTypeRepository).findAll();
    }
}

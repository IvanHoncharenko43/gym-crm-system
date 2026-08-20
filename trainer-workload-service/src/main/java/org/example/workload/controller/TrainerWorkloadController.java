package org.example.workload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workload.controller.dto.TrainerWorkloadRequest;
import org.example.workload.service.TrainerWorkloadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@Tag(name = "TrainerWorkload", description = "Operations related to trainers' workloads")
@RequestMapping(TrainerWorkloadController.BASE_PATH)
@RestController
@RequiredArgsConstructor
public class TrainerWorkloadController {

    public static final String BASE_PATH = "/api/v1/trainers/workloads";

    private final TrainerWorkloadService trainerWorkloadService;

    @Operation(summary = "Update a trainer's workload", description = "Updates a trainer's workload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated a trainer's workload"),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(
                    implementation = ProblemDetail.class))),
    })
    @PreAuthorize("hasAnyRole('TRAINER', 'ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateWorkload(@Valid @RequestBody TrainerWorkloadRequest request){
        switch (request.actionType()){
            case ADD -> trainerWorkloadService.addWorkload(request);
            case DELETE -> trainerWorkloadService.deleteWorkload(request);
        }
    }
}

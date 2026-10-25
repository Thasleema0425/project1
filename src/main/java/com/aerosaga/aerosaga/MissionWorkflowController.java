
package com.aerosaga.aerosaga;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowExecutionAlreadyStarted;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class MissionWorkflowController {

    private final WorkflowClient workflowClient;
    private final MissionRepository missionRepository;
    private final DroneRepository droneRepository;

    public MissionWorkflowController(
            WorkflowClient workflowClient,
            MissionRepository missionRepository,
            DroneRepository droneRepository) {
        this.workflowClient = workflowClient;
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
    }

    @PostMapping("/missions/{missionId}/start")
    public Map<String, String> startMission(
            @PathVariable Long missionId) {

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mission not found"));

        if (mission.getDroneId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mission has no assigned drone");
        }

        Drone drone = droneRepository.findById(mission.getDroneId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Mission has no valid assigned drone"));

        if (!"AVAILABLE".equalsIgnoreCase(drone.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Drone is not available for a mission");
        }

        if (drone.getBatteryLevel() < 30) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Drone battery must be at least 30%");
        }

        String workflowId = "drone-mission-" + missionId;

        DroneMissionWorkflow workflow =
                workflowClient.newWorkflowStub(
                        DroneMissionWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(workflowId)
                                .setTaskQueue(
                                        TemporalWorkerConfig.TASK_QUEUE)
                                .setWorkflowIdReusePolicy(
                                        WorkflowIdReusePolicy
                                                .WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                                .build());

        try {
            WorkflowClient.start(workflow::executeMission, missionId);
        } catch (WorkflowExecutionAlreadyStarted exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A workflow for this mission has already been started");
        }

        return Map.of(
                "workflowId", workflowId,
                "message", "Drone mission workflow started",
                "missionId", missionId.toString());
    }
}

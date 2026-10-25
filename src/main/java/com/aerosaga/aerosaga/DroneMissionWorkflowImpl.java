
package com.aerosaga.aerosaga;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class DroneMissionWorkflowImpl implements DroneMissionWorkflow {

    private final MissionActivities activities =
            Workflow.newActivityStub(
                    MissionActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofSeconds(10))
                            .build());

    @Override
    public String executeMission(Long missionId) {

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Starting drone mission: " + missionId);

        activities.updateMissionStatus(missionId, "IN_PROGRESS");

        Workflow.sleep(Duration.ofSeconds(5));

        activities.updateMissionStatus(missionId, "COMPLETED");

        Workflow.getLogger(DroneMissionWorkflowImpl.class)
                .info("Drone mission completed: " + missionId);

        return "Mission " + missionId + " completed successfully";
    }
}

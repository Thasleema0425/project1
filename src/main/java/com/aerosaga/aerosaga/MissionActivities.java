
package com.aerosaga.aerosaga;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface MissionActivities {

    @ActivityMethod
    void updateMissionStatus(Long missionId, String status);
}

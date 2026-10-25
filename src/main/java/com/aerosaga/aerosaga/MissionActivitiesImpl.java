

package com.aerosaga.aerosaga;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MissionActivitiesImpl implements MissionActivities {

    private final MissionRepository missionRepository;
    private final DroneRepository droneRepository;

    public MissionActivitiesImpl(
            MissionRepository missionRepository,
            DroneRepository droneRepository) {
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
    }

    @Override
    @Transactional
    public void updateMissionStatus(Long missionId, String status) {

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mission not found: " + missionId));

        mission.setStatus(status);
        missionRepository.save(mission);

        if (mission.getDroneId() == null) {
            return;
        }

        Drone drone = droneRepository.findById(mission.getDroneId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Drone not found: " + mission.getDroneId()));

        if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            drone.setStatus("IN_MISSION");
        } else if ("COMPLETED".equalsIgnoreCase(status)
                || "FAILED".equalsIgnoreCase(status)
                || "CANCELLED".equalsIgnoreCase(status)) {
            drone.setStatus("AVAILABLE");
        }

        droneRepository.save(drone);
    }
}


package com.aerosaga.aerosaga;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionRepository missionRepository;
    private final DroneRepository droneRepository;

    public MissionController(
            MissionRepository missionRepository,
            DroneRepository droneRepository) {
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
    }

    @GetMapping
    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> getMissionById(
            @PathVariable Long id) {
        return missionRepository.findById(id)
                .map(mission -> ResponseEntity.ok(mission))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMission(
            @Valid @RequestBody Mission mission) {

        if (!droneRepository.existsById(mission.getDroneId())) {
            return ResponseEntity.badRequest()
                    .body("Drone ID does not exist");
        }

        if (mission.getStatus() == null ||
                mission.getStatus().isBlank()) {
            mission.setStatus("PLANNED");
        }

        Mission savedMission = missionRepository.save(mission);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedMission);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Mission> updateMissionStatus(
            @PathVariable Long id,
            @RequestBody Mission update) {

        return missionRepository.findById(id)
                .map(mission -> {
                    mission.setStatus(update.getStatus());
                    return ResponseEntity.ok(
                            missionRepository.save(mission));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(
            @PathVariable Long id) {
        if (!missionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        missionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

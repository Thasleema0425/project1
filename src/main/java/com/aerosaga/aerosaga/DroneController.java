

package com.aerosaga.aerosaga;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drones")
public class DroneController {

    private final DroneRepository droneRepository;

    public DroneController(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    @GetMapping
    public List<Drone> getAllDrones() {
        return droneRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Drone> getDroneById(@PathVariable Long id) {
        return droneRepository.findById(id)
                .map(drone -> ResponseEntity.ok(drone))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Drone> createDrone(
            @Valid @RequestBody Drone drone) {

        if (drone.getStatus() == null ||
                drone.getStatus().isBlank()) {
            drone.setStatus("AVAILABLE");
        }

        Drone savedDrone = droneRepository.save(drone);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedDrone);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Drone> updateDrone(
            @PathVariable Long id,
            @Valid @RequestBody Drone updatedDrone) {

        return droneRepository.findById(id)
                .map(drone -> {
                    drone.setDroneName(updatedDrone.getDroneName());
                    drone.setBatteryLevel(updatedDrone.getBatteryLevel());
                    drone.setStatus(updatedDrone.getStatus());

                    return ResponseEntity.ok(
                            droneRepository.save(drone));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrone(
            @PathVariable Long id) {

        if (!droneRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        droneRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

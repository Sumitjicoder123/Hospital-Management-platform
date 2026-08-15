package com.hospital.platform.controller;

import com.hospital.platform.dto.OperationDTOs.*;
import com.hospital.platform.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final SimulationService simulationService;

    public AnalyticsController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/what-if")
    public ResponseEntity<WhatIfSimulationResponse> runWhatIfSimulation(@RequestBody WhatIfSimulationRequest request) {
        return ResponseEntity.ok(simulationService.runSimulation(request));
    }
}

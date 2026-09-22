package com.algoworks.leavemanagement.controller;

import com.algoworks.leavemanagement.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
@Tag(name = "System Health", description = "Endpoints to check server and database status")
public class HealthController {

    private final DataSource dataSource;
    private final Environment env;
    private final com.algoworks.leavemanagement.config.DataSeeder dataSeeder;

    @Autowired
    public HealthController(DataSource dataSource, Environment env, com.algoworks.leavemanagement.config.DataSeeder dataSeeder) {
        this.dataSource = dataSource;
        this.env = env;
        this.dataSeeder = dataSeeder;
    }

    private static final long SERVER_START_TIME = System.currentTimeMillis();

    @GetMapping
    @Operation(summary = "Get system health status", description = "Returns health of the server, database connectivity, and JVM telemetry.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> details = new HashMap<>();
        details.put("status", "UP");

        // Determine active profile
        String[] activeProfiles = env.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "dev";
        details.put("activeProfile", profile);

        // Probe database connection
        String dbName = "Unknown";
        String dbStatus = "CONNECTED";
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();
            details.put("databaseVersion", dbVersion);
        } catch (Exception e) {
            dbName = "Offline";
            dbStatus = "DISCONNECTED: " + e.getMessage();
        }
        details.put("database", dbName);
        details.put("databaseStatus", dbStatus);

        // JVM & System Telemetry
        Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory() / (1024 * 1024);
        long freeMem = rt.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;
        long maxMem = rt.maxMemory() / (1024 * 1024);

        Map<String, Object> memory = new HashMap<>();
        memory.put("usedMb", usedMem);
        memory.put("freeMb", freeMem);
        memory.put("totalMb", totalMem);
        memory.put("maxMb", maxMem);
        details.put("memory", memory);

        details.put("availableProcessors", rt.availableProcessors());
        details.put("uptimeSeconds", (System.currentTimeMillis() - SERVER_START_TIME) / 1000);

        return ResponseEntity.ok(ApiResponse.success("System is running correctly", details));
    }

    @GetMapping("/ping")
    @Operation(summary = "Quick ping endpoint", description = "Returns lightweight PONG response to measure latency.")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("PONG", "HEALTHY"));
    }

    @org.springframework.web.bind.annotation.PostMapping("/reset-demo")
    @Operation(summary = "Reset and reload demo scenario data", description = "Clears and re-seeds realistic sample employees, leave requests, and activity logs.")
    public ResponseEntity<ApiResponse<String>> resetDemoData() {
        dataSeeder.seedData(true);
        return ResponseEntity.ok(ApiResponse.success("Demo scenario data successfully reset and reloaded!", "READY"));
    }
}

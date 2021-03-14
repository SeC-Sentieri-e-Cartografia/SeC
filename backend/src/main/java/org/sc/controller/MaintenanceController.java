package org.sc.controller;

import org.sc.common.rest.CountDto;
import org.sc.common.rest.MaintenanceCreationDto;
import org.sc.common.rest.MaintenanceDto;
import org.sc.common.rest.Status;
import org.sc.common.rest.response.CountResponse;
import org.sc.common.rest.response.MaintenanceResponse;
import org.sc.data.validator.MaintenanceValidator;
import org.sc.manager.MaintenanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.Collections.*;
import static org.sc.configuration.AppBoundaries.MAX_DOCS_ON_READ;
import static org.sc.configuration.AppBoundaries.MIN_DOCS_ON_READ;

@RestController
@RequestMapping(MaintenanceController.PREFIX)
public class MaintenanceController {

    public final static String PREFIX = "/maintenance";

    private final static Logger LOGGER = Logger
            .getLogger(MaintenanceController.class.getName());

    private final MaintenanceValidator maintenanceValidator;
    private final MaintenanceManager maintenanceManager;

    @Autowired
    public MaintenanceController(final MaintenanceManager maintenanceManager,
                                 final MaintenanceValidator maintenanceValidator) {
        this.maintenanceManager = maintenanceManager;
        this.maintenanceValidator = maintenanceValidator;
    }

    @GetMapping("/count")
    public CountResponse getCount() {
        final long count = maintenanceManager.countMaintenance();
        return new CountResponse(Status.OK, Collections.emptySet(), new CountDto(count));
    }

    @GetMapping("/future")
    public MaintenanceResponse getFutureMaintenance(
            @RequestParam(required = false, defaultValue = MIN_DOCS_ON_READ) int page,
            @RequestParam(required = false, defaultValue = MAX_DOCS_ON_READ) int count) {
        return new MaintenanceResponse(Status.OK, emptySet(), maintenanceManager.getFuture(page, count));
    }

    @GetMapping("/past")
    public MaintenanceResponse getPastMaintenance(
            @RequestParam(required = false, defaultValue = MIN_DOCS_ON_READ) int page,
            @RequestParam(required = false, defaultValue = MAX_DOCS_ON_READ) int count) {
        List<MaintenanceDto> past = maintenanceManager.getPast(page, count);
        return new MaintenanceResponse(Status.OK, emptySet(), past);
    }

    @GetMapping("/past/{id}")
    public MaintenanceResponse getPastMaintenanceById(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = MIN_DOCS_ON_READ) int page,
            @RequestParam(required = false, defaultValue = MAX_DOCS_ON_READ) int count) {
        List<MaintenanceDto> past = maintenanceManager.getPastMaintenanceForTrailId(id, page, count);
        return new MaintenanceResponse(Status.OK, emptySet(), past);
    }

    @PutMapping
    public MaintenanceResponse create(
            @RequestBody MaintenanceCreationDto request) {
        final Set<String> errors = maintenanceValidator.validate(request);
        if(errors.isEmpty()) {
            List<MaintenanceDto> maintenanceDtos = maintenanceManager.upsert(request);
            return new MaintenanceResponse(Status.OK, emptySet(), maintenanceDtos);
        }
        return new MaintenanceResponse(Status.OK, errors, emptyList());
    }

    @DeleteMapping("/{id}")
    public MaintenanceResponse deleteMaintenance(
            @PathVariable String id) {
        List<MaintenanceDto> deleted = maintenanceManager.delete(id);
        if (deleted.isEmpty()) {
            LOGGER.warning(format("Could not delete maintenance with id '%s'", id));
            return new MaintenanceResponse(Status.ERROR,
                    new HashSet<>(singletonList(
                            format("No maintenance was found with id '%s'", id))), deleted);
        }
        return new MaintenanceResponse(Status.OK, emptySet(), deleted);
    }

    @GetMapping("/past/count")
    public CountResponse getCountPast() {
        final long count = maintenanceManager.countPastMaintenance();
        return new CountResponse(Status.OK, Collections.emptySet(), new CountDto(count));
    }

    @GetMapping("/future/count")
    public CountResponse getCountFuture() {
        final long count = maintenanceManager.countFutureMaintenance();
        return new CountResponse(Status.OK, Collections.emptySet(), new CountDto(count));
    }
}

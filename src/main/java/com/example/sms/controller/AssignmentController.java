package com.example.sms.controller;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.request.FeedBackRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.dto.response.FeedBackResponse;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("assignments")
@Tag(name = "Assignment Management")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final AssignmentRepository assignmentRepository;

    public AssignmentController(
            AssignmentService assignmentService,
            AssignmentRepository assignmentRepository) {
        this.assignmentService = assignmentService;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/all")
    @Operation(summary = "Get all assignments")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Create an assignment")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @Valid @RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.ok(assignmentService.create(assignmentRequest));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update an assignment")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.ok(assignmentService.update(id, assignmentRequest));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete an assignment")
    public void deleteAssignment(@PathVariable Long id) {
        assignmentService.delete(id);
    }

    @PostMapping("/{id}/addFeedBack")
    @Operation(summary = "Add feedback to assignment")
    public ResponseEntity<AssignmentResponse> addFeedBack(
            @PathVariable Long id,
            @Valid @RequestBody FeedBackRequest feedBackRequest) {
        return ResponseEntity.ok(assignmentService.addFeedBack(id, feedBackRequest));
    }

    @DeleteMapping("/{assignmentId}/feedbacks/{feedBackId}")
    @Operation(summary = "Delete feedback from assignment")
    public ResponseEntity<AssignmentResponse> deleteFeedBack(
            @PathVariable Long assignmentId, @PathVariable Long feedBackId) {
        return ResponseEntity.ok(assignmentService.deleteFeedBack(assignmentId, feedBackId));
    }

    @PatchMapping("/{assignmentId}/feedbacks/{feedBackId}")
    @Operation(summary = "Update feedback in assignment")
    public ResponseEntity<FeedBackResponse> updateFeedBack(
            @PathVariable Long assignmentId,
            @PathVariable Long feedBackId,
            @Valid @RequestBody FeedBackRequest feedBackRequest) {
        return ResponseEntity.ok(assignmentService.updateFeedBack(
                assignmentId,
                feedBackId,
                feedBackRequest));
    }
}
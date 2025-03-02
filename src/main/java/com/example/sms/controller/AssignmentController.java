package com.example.sms.controller;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.request.FeedBackRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.service.AssignmentService;
import com.example.sms.service.GenericService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final GenericService<AssignmentResponse, AssignmentRequest, Long> genericService;
    private final AssignmentRepository assignmentRepository;

    public AssignmentController(
            AssignmentService assignmentService,
            GenericService<AssignmentResponse, AssignmentRequest, Long> genericService,
            AssignmentRepository assignmentRepository) {

        this.assignmentService = assignmentService;
        this.genericService = genericService;
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(genericService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(genericService.getById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.ok(genericService.create(assignmentRequest));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id, @RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.ok(genericService.update(id, assignmentRequest));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteAssignment(@PathVariable Long id) {
        genericService.delete(id);
    }

    @PostMapping("/addFeedBack/{id}")
    public ResponseEntity<AssignmentResponse> addFeedBack(
            @PathVariable Long id, @RequestBody FeedBackRequest feedBackRequest) {
        return ResponseEntity.ok(assignmentService.addFeedBack(id, feedBackRequest));
    }

    @DeleteMapping("/deleteFeedBack/{assignmentId}/{feedBackId}")
    public ResponseEntity<AssignmentResponse> deleteFeedBack(
            @PathVariable Long assignmentId, @PathVariable Long feedBackId) {
        return ResponseEntity.ok(assignmentService.deleteFeedBack(assignmentId, feedBackId));
    }
}

package com.example.sms.controller;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.ok(assignmentService.createAssignment(assignmentRequest));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id, @RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, assignmentRequest));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
    }
}

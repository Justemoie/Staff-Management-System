package com.example.sms.service.implementation;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.request.FeedBackRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.dto.response.FeedBackResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.entity.FeedBack;
import com.example.sms.mapper.AssignmentMapper;
import com.example.sms.mapper.FeedBackMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.repository.FeedBackRepository;
import com.example.sms.service.AssignmentService;
import com.example.sms.service.GenericService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssignmentServiceImpl implements
        GenericService<AssignmentResponse, AssignmentRequest, Long>, AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final EmployeeRepository employeeRepository;
    private final FeedBackMapper feedBackMapper;
    private final FeedBackRepository feedBackRepository;

    public AssignmentServiceImpl(
            AssignmentRepository assignmentRepository,
            AssignmentMapper assignmentMapper,
            EmployeeRepository employeeRepository,
            FeedBackMapper feedBackMapper,
            FeedBackRepository feedBackRepository) {

        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
        this.employeeRepository = employeeRepository;
        this.feedBackMapper = feedBackMapper;
        this.feedBackRepository = feedBackRepository;
    }

    @Override
    public List<AssignmentResponse> getAll() {
        return assignmentMapper.toAssignmentResponseList(assignmentRepository.findAll());
    }

    @Override
    public AssignmentResponse getById(Long id) {
        return assignmentMapper.toAssignmentResponse(assignmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found")));
    }

    @Override
    public AssignmentResponse create(AssignmentRequest assignmentRequest) {
        return assignmentMapper.toAssignmentResponse(
                assignmentRepository.save(assignmentMapper.toAssignment(assignmentRequest)));
    }

    @Override
    public AssignmentResponse update(Long id, AssignmentRequest assignmentRequest) {
        Assignment assignment = assignmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found with such id"));

        Assignment updatedAssignment = assignmentRepository.save(
                assignmentMapper.partialUpdate(assignmentRequest, assignment));

        return assignmentMapper.toAssignmentResponse(updatedAssignment);
    }

    @Override
    public void delete(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + assignmentId));

        for (Employee employee : employeeRepository.findAll()) {
            employee.getAssignments().remove(assignment);
        }

        assignmentRepository.deleteById(assignmentId);
    }

    public AssignmentResponse addFeedBack(Long assignmentId, FeedBackRequest feedBackRequest) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + assignmentId));


        FeedBack feedBack = feedBackMapper.toFeedBack(feedBackRequest);
        feedBack.setAssignment(assignment);

        assignment.getFeedBacks().add(feedBack);

        return assignmentMapper.toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Override
    public AssignmentResponse deleteFeedBack(Long assignmentId, Long feedBackId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assignment not found with such id = " + assignmentId));

        FeedBack feedBack = feedBackRepository.findById(feedBackId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Feedback not found with such id = " + feedBackId));

        if (!assignment.getFeedBacks().contains(feedBack)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Assignment with id = " + assignmentId
                            + " does not contain feedback with id = " + feedBackId);
        }

        assignment.getFeedBacks().remove(feedBack);
        feedBackRepository.delete(feedBack);

        return assignmentMapper.toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Override
    public FeedBackResponse updateFeedBack(
            Long assignmentId, Long feedBackId, FeedBackRequest feedBackRequest) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found"));

        FeedBack feedBack = feedBackRepository.findById(feedBackId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Feedback not found"));

        if (!feedBack.getAssignment().equals(assignment)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Feedback does not belong to the given assignment");
        }

        feedBackMapper.partialUpdate(feedBackRequest, feedBack);

        return feedBackMapper.toFeedBackResponse(feedBackRepository.save(feedBack));
    }

}

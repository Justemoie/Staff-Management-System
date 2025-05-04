package com.example.sms.service.implementation;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.request.FeedBackRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.dto.response.FeedBackResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.entity.FeedBack;
import com.example.sms.exception.ConflictException;
import com.example.sms.mapper.AssignmentMapper;
import com.example.sms.mapper.FeedBackMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.repository.FeedBackRepository;
import com.example.sms.service.AssignmentService;
import com.example.sms.service.GenericService;
import com.example.sms.utils.cache.Cache;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final EmployeeRepository employeeRepository;
    private final FeedBackMapper feedBackMapper;
    private final FeedBackRepository feedBackRepository;
    private final Cache<Long, AssignmentResponse> cache;

    public AssignmentServiceImpl(Cache<Long, AssignmentResponse> cache,
            AssignmentRepository assignmentRepository,
            AssignmentMapper assignmentMapper,
            EmployeeRepository employeeRepository,
            FeedBackMapper feedBackMapper,
            FeedBackRepository feedBackRepository) {

        this.cache = cache;
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

    public AssignmentResponse getById(Long id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        Assignment assignmentEntity = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + id));

        AssignmentResponse assignment = assignmentMapper.toAssignmentResponse(assignmentEntity);
        cache.put(id, assignment);
        return assignment;
    }

    @Override
    public AssignmentResponse create(AssignmentRequest assignmentRequest) {
        if (assignmentRepository.existsByTitle(assignmentRequest.title())) {
            throw new ConflictException(
                    "Title " + assignmentRequest.title() + " is already in use");
        }
        return assignmentMapper.toAssignmentResponse(
                assignmentRepository.save(assignmentMapper.toAssignment(assignmentRequest)));
    }

    @Override
    public AssignmentResponse update(Long id, AssignmentRequest assignmentRequest) {
        if (assignmentRepository.existsByTitle(assignmentRequest.title())) {
            throw new ConflictException(
                    "Title " + assignmentRequest.title() + " is already in use");
        }
        Assignment assignmentToUpdate = assignmentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with such id"));

        Assignment assignment = assignmentMapper
                .partialUpdate(assignmentRequest, assignmentToUpdate);
        Assignment updatedAssignment = saveUpdates(assignment);

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

        cache.remove(assignmentId);

        assignmentRepository.deleteById(assignmentId);
    }

    public AssignmentResponse addFeedBack(Long assignmentId, FeedBackRequest feedBackRequest) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + assignmentId));

        FeedBack feedBack = feedBackMapper.toFeedBack(feedBackRequest);
        feedBack.setAssignment(assignment);

        assignment.getFeedBacks().add(feedBack);

        return assignmentMapper.toAssignmentResponse(saveUpdates(assignment));
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

        return assignmentMapper.toAssignmentResponse(saveUpdates(assignment));
    }

    @Override
    public FeedBackResponse updateFeedBack(
            Long assignmentId, Long feedBackId, FeedBackRequest feedBackRequest) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found"));

        FeedBack feedBackToUpdate = feedBackRepository.findById(feedBackId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Feedback not found"));

        if (!feedBackToUpdate.getAssignment().equals(assignment)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Feedback does not belong to the given assignment");
        }

        FeedBack feedBack = feedBackMapper.partialUpdate(feedBackRequest, feedBackToUpdate);
        saveUpdates(assignment);

        return feedBackMapper.toFeedBackResponse(feedBackRepository.save(feedBack));
    }

    private Assignment saveUpdates(Assignment assignment) {
        var assignmentToSave = assignmentRepository.save(assignment);
        if (cache.containsKey(assignment.getId())) {
            cache.put(assignment.getId(), assignmentMapper.toAssignmentResponse(assignment));
        }
        return assignmentToSave;
    }
}

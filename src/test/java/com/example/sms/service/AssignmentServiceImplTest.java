package com.example.sms.service;

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
import com.example.sms.service.implementation.AssignmentServiceImpl;
import com.example.sms.utils.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssignmentServiceImplTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private AssignmentMapper assignmentMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private FeedBackMapper feedBackMapper;

    @Mock
    private FeedBackRepository feedBackRepository;

    @Mock
    private Cache<Long, AssignmentResponse> cache;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    private Assignment assignment;
    private AssignmentRequest assignmentRequest;
    private AssignmentResponse assignmentResponse;
    private FeedBack feedBack;
    private FeedBackRequest feedBackRequest;
    private FeedBackResponse feedBackResponse;
    private Employee employee;

    @BeforeEach
    void setUp() {
        // Инициализация тестовых данных
        assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Test Description");
        assignment.setFeedBacks(new ArrayList<>());

        assignmentRequest = new AssignmentRequest("Test Assignment", "Test Description");

        assignmentResponse = new AssignmentResponse(1L, "Test Assignment", "Test Description", List.of());

        feedBack = new FeedBack();
        feedBack.setId(1L);
        feedBack.setComment("Great work!");
        feedBack.setCreatedAt(LocalDateTime.now());
        feedBack.setAssignment(assignment);

        feedBackRequest = new FeedBackRequest("Great work!", LocalDateTime.now());

        feedBackResponse = new FeedBackResponse(1L, "Great work!", LocalDateTime.now());

        employee = new Employee();
        employee.setId(1L);
        employee.setAssignments(new ArrayList<>());

        // Сбрасываем моки перед каждым тестом
        reset(assignmentRepository, assignmentMapper, employeeRepository, feedBackMapper, feedBackRepository, cache);
    }

    // Тесты для метода getAll
    @Test
    void getAll_WhenAssignmentsExist_ShouldReturnList() {
        // Arrange
        List<Assignment> assignments = List.of(assignment);
        List<AssignmentResponse> responses = List.of(assignmentResponse);

        when(assignmentRepository.findAll()).thenReturn(assignments);
        when(assignmentMapper.toAssignmentResponseList(assignments)).thenReturn(responses);

        // Act
        List<AssignmentResponse> result = assignmentService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(assignmentResponse, result.get(0));
        verify(assignmentRepository).findAll();
        verify(assignmentMapper).toAssignmentResponseList(assignments);
    }

    // Тесты для метода getById
    @Test
    void getById_WhenInCache_ShouldReturnFromCache() {
        // Arrange
        when(cache.containsKey(1L)).thenReturn(true);
        when(cache.get(1L)).thenReturn(assignmentResponse);

        // Act
        AssignmentResponse result = assignmentService.getById(1L);

        // Assert
        assertEquals(assignmentResponse, result);
        verify(cache).containsKey(1L);
        verify(cache).get(1L);
        verify(assignmentRepository, never()).findById(anyLong());
    }

    @Test
    void getById_WhenNotInCacheAndExists_ShouldReturnAndCache() {
        // Arrange
        when(cache.containsKey(1L)).thenReturn(false);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentMapper.toAssignmentResponse(assignment)).thenReturn(assignmentResponse);

        // Act
        AssignmentResponse result = assignmentService.getById(1L);

        // Assert
        assertEquals(assignmentResponse, result);
        verify(cache).containsKey(1L);
        verify(assignmentRepository).findById(1L);
        verify(assignmentMapper).toAssignmentResponse(assignment);
        verify(cache).put(1L, assignmentResponse);
    }

    @Test
    void getById_WhenNotFound_ShouldThrowNotFound() {
        // Arrange
        when(cache.containsKey(1L)).thenReturn(false);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.getById(1L));
        assertEquals("404 NOT_FOUND \"Assignment not found\"", exception.getMessage());
        verify(cache).containsKey(1L);
        verify(assignmentRepository).findById(1L);
        verify(assignmentMapper, never()).toAssignmentResponse(any());
        verify(cache, never()).put(anyLong(), any());
    }

    // Тесты для метода create
    @Test
    void create_WhenTitleNotExists_ShouldCreate() {
        // Arrange
        when(assignmentRepository.existsByTitle("Test Assignment")).thenReturn(false);
        when(assignmentMapper.toAssignment(assignmentRequest)).thenReturn(assignment);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(assignmentMapper.toAssignmentResponse(assignment)).thenReturn(assignmentResponse);

        // Act
        AssignmentResponse result = assignmentService.create(assignmentRequest);

        // Assert
        assertEquals(assignmentResponse, result);
        verify(assignmentRepository).existsByTitle("Test Assignment");
        verify(assignmentMapper).toAssignment(assignmentRequest);
        verify(assignmentRepository).save(assignment);
        verify(assignmentMapper).toAssignmentResponse(assignment);
    }

    @Test
    void create_WhenTitleExists_ShouldThrowConflict() {
        // Arrange
        when(assignmentRepository.existsByTitle("Test Assignment")).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class,
                () -> assignmentService.create(assignmentRequest));
        assertEquals("Title Test Assignment is already in use", exception.getMessage());
        verify(assignmentRepository).existsByTitle("Test Assignment");
        verify(assignmentMapper, never()).toAssignment(any());
        verify(assignmentRepository, never()).save(any());
    }

    // Тесты для метода update
    @Test
    void update_WhenAssignmentExistsAndTitleNotUsed_ShouldUpdate() {
        // Arrange
        when(assignmentRepository.existsByTitle("Test Assignment")).thenReturn(false);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentMapper.partialUpdate(assignmentRequest, assignment)).thenReturn(assignment);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(assignmentMapper.toAssignmentResponse(assignment)).thenReturn(assignmentResponse);
        when(cache.containsKey(1L)).thenReturn(true);

        // Act
        AssignmentResponse result = assignmentService.update(1L, assignmentRequest);

        // Assert
        assertEquals(assignmentResponse, result);
        verify(assignmentRepository).existsByTitle("Test Assignment");
        verify(assignmentRepository).findById(1L);
        verify(assignmentMapper).partialUpdate(assignmentRequest, assignment);
        verify(assignmentRepository).save(assignment);
        verify(assignmentMapper, times(2)).toAssignmentResponse(assignment);
        verify(cache).containsKey(1L);
        verify(cache).put(1L, assignmentResponse);
    }

    @Test
    void update_WhenTitleExists_ShouldThrowConflict() {
        // Arrange
        when(assignmentRepository.existsByTitle("Test Assignment")).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class,
                () -> assignmentService.update(1L, assignmentRequest));
        assertEquals("Title Test Assignment is already in use", exception.getMessage());
        verify(assignmentRepository).existsByTitle("Test Assignment");
        verify(assignmentRepository, never()).findById(anyLong());
    }

    @Test
    void update_WhenAssignmentNotFound_ShouldThrowNotFound() {
        // Arrange
        when(assignmentRepository.existsByTitle("Test Assignment")).thenReturn(false);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.update(1L, assignmentRequest));
        assertEquals("404 NOT_FOUND \"Assignment not found with such id\"", exception.getMessage());
        verify(assignmentRepository).existsByTitle("Test Assignment");
        verify(assignmentRepository).findById(1L);
        verify(assignmentMapper, never()).partialUpdate(any(), any());
    }

    // Тесты для метода delete
    @Test
    void delete_WhenAssignmentExists_ShouldDelete() {
        // Arrange
        Long assignmentId = 1L;
        employee.getAssignments().add(assignment); // Добавляем задание в список сотрудника
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        // Act
        assignmentService.delete(assignmentId);

        // Assert
        assertTrue(employee.getAssignments().isEmpty()); // Теперь список должен быть пуст
        verify(assignmentRepository).findById(assignmentId);
        verify(employeeRepository).findAll();
        verify(cache).remove(assignmentId);
        verify(assignmentRepository).deleteById(assignmentId);
    }

    @Test
    void delete_WhenAssignmentNotFound_ShouldThrowNotFound() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.delete(1L));
        assertEquals("404 NOT_FOUND \"Assignment not found with id = 1\"", exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(employeeRepository, never()).findAll();
        verify(cache, never()).remove(anyLong());
        verify(assignmentRepository, never()).deleteById(anyLong());
    }

    // Тесты для метода addFeedBack
    @Test
    void addFeedBack_WhenAssignmentExists_ShouldAddFeedBack() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(feedBackMapper.toFeedBack(feedBackRequest)).thenReturn(feedBack);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(assignmentMapper.toAssignmentResponse(assignment)).thenReturn(assignmentResponse);
        when(cache.containsKey(1L)).thenReturn(true);

        // Act
        AssignmentResponse result = assignmentService.addFeedBack(1L, feedBackRequest);

        // Assert
        assertEquals(assignmentResponse, result);
        assertEquals(1, assignment.getFeedBacks().size());
        assertEquals(feedBack, assignment.getFeedBacks().get(0));
        verify(assignmentRepository).findById(1L);
        verify(feedBackMapper).toFeedBack(feedBackRequest);
        verify(assignmentRepository).save(assignment);
        verify(assignmentMapper, times(2)).toAssignmentResponse(assignment);
        verify(cache).containsKey(1L);
        verify(cache).put(1L, assignmentResponse);
    }

    @Test
    void addFeedBack_WhenAssignmentNotFound_ShouldThrowNotFound() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.addFeedBack(1L, feedBackRequest));
        assertEquals("404 NOT_FOUND \"Assignment not found with id = 1\"", exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(feedBackMapper, never()).toFeedBack(any());
    }

    // Тесты для метода deleteFeedBack
    @Test
    void deleteFeedBack_WhenValid_ShouldDeleteFeedBack() {
        // Arrange
        long assignmentId = 1L;
        long feedBackId = 1L;
        assignment.getFeedBacks().add(feedBack);
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(feedBackRepository.findById(feedBackId)).thenReturn(Optional.of(feedBack));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(assignmentMapper.toAssignmentResponse(assignment)).thenReturn(assignmentResponse);
        when(cache.containsKey(assignmentId)).thenReturn(true);

        // Act
        AssignmentResponse result = assignmentService.deleteFeedBack(assignmentId, feedBackId);

        // Assert
        assertEquals(assignmentResponse, result);
        assertTrue(assignment.getFeedBacks().isEmpty());
        verify(assignmentRepository).findById(assignmentId);
        verify(feedBackRepository).findById(feedBackId);
        verify(feedBackRepository).delete(feedBack);
        verify(assignmentRepository).save(assignment);
        verify(assignmentMapper, times(2)).toAssignmentResponse(assignment);
        verify(cache).containsKey(assignmentId);
        verify(cache).put(assignmentId, assignmentResponse);
    }

    @Test
    void deleteFeedBack_WhenAssignmentNotFound_ShouldThrowNotFound() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.deleteFeedBack(1L, 1L));
        assertEquals("404 NOT_FOUND \"Assignment not found with such id = 1\"", exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(feedBackRepository, never()).findById(anyLong());
    }

    @Test
    void deleteFeedBack_WhenFeedBackNotFound_ShouldThrowNotFound() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(feedBackRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.deleteFeedBack(1L, 1L));
        assertEquals("404 NOT_FOUND \"Feedback not found with such id = 1\"", exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(feedBackRepository).findById(1L);
    }

    @Test
    void deleteFeedBack_WhenFeedBackNotInAssignment_ShouldThrowBadRequest() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(feedBackRepository.findById(1L)).thenReturn(Optional.of(feedBack));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.deleteFeedBack(1L, 1L));
        assertEquals("400 BAD_REQUEST \"Assignment with id = 1 does not contain feedback with id = 1\"",
                exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(feedBackRepository).findById(1L);
        verify(feedBackRepository, never()).delete(any());
    }

    // Тесты для метода updateFeedBack
    @Test
    void updateFeedBack_WhenValid_ShouldUpdateFeedBack() {
        // Arrange
        feedBack.setAssignment(assignment);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(feedBackRepository.findById(1L)).thenReturn(Optional.of(feedBack));
        when(feedBackMapper.partialUpdate(feedBackRequest, feedBack)).thenReturn(feedBack);
        when(feedBackRepository.save(feedBack)).thenReturn(feedBack);
        when(feedBackMapper.toFeedBackResponse(feedBack)).thenReturn(feedBackResponse);
        when(assignmentRepository.save(assignment)).thenReturn(assignment);
        when(assignmentMapper.toAssignmentResponse(assignment)).thenReturn(assignmentResponse);
        when(cache.containsKey(1L)).thenReturn(true);

        // Act
        FeedBackResponse result = assignmentService.updateFeedBack(1L, 1L, feedBackRequest);

        // Assert
        assertEquals(feedBackResponse, result);
        verify(assignmentRepository).findById(1L);
        verify(feedBackRepository).findById(1L);
        verify(feedBackMapper).partialUpdate(feedBackRequest, feedBack);
        verify(assignmentRepository).save(assignment);
        verify(feedBackRepository).save(feedBack);
        verify(feedBackMapper).toFeedBackResponse(feedBack);
        verify(cache).containsKey(1L);
        verify(cache).put(1L, assignmentResponse);
    }

    @Test
    void updateFeedBack_WhenAssignmentNotFound_ShouldThrowNotFound() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.updateFeedBack(1L, 1L, feedBackRequest));
        assertEquals("404 NOT_FOUND \"Assignment not found\"", exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(feedBackRepository, never()).findById(anyLong());
    }

    @Test
    void updateFeedBack_WhenFeedBackNotFound_ShouldThrowNotFound() {
        // Arrange
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(feedBackRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.updateFeedBack(1L, 1L, feedBackRequest));
        assertEquals("404 NOT_FOUND \"Feedback not found\"", exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(feedBackRepository).findById(1L);
    }

    @Test
    void updateFeedBack_WhenFeedBackNotInAssignment_ShouldThrowBadRequest() {
        // Arrange
        Assignment otherAssignment = new Assignment();
        otherAssignment.setId(2L);
        feedBack.setAssignment(otherAssignment);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(feedBackRepository.findById(1L)).thenReturn(Optional.of(feedBack));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> assignmentService.updateFeedBack(1L, 1L, feedBackRequest));
        assertEquals("400 BAD_REQUEST \"Feedback does not belong to the given assignment\"",
                exception.getMessage());
        verify(assignmentRepository).findById(1L);
        verify(feedBackRepository).findById(1L);
        verify(feedBackMapper, never()).partialUpdate(any(), any());
    }
}
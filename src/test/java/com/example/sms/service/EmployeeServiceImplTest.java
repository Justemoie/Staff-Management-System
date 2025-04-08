package com.example.sms.service;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.exception.ConflictException;
import com.example.sms.mapper.EmployeeMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.service.implementation.EmployeeServiceImpl;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeServiceImplTest {

    @Mock
    private Cache<Long, EmployeeResponse> cache;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeRequest employeeRequest;
    private EmployeeResponse employeeResponse;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setPhoneNumber("+375291234567");
        employee.setEmail("john.doe@example.com");

        employeeRequest = new EmployeeRequest("John", "Doe", "+375291234567", "john.doe@example.com");

        employeeResponse = new EmployeeResponse(1L, "John", "Doe", "+375291234567", "john.doe@example.com", List.of());

        assignment = new Assignment();
        assignment.setId(2L);
    }

    @Test
    void getAll_ShouldReturnListOfEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        when(employeeMapper.toEmployeeResponseList(List.of(employee))).thenReturn(List.of(employeeResponse));

        List<EmployeeResponse> responses = employeeService.getAll();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(employeeRepository).findAll();
        verify(employeeMapper).toEmployeeResponseList(List.of(employee));
    }

    @Test
    void getById_WhenEmployeeExistsInCache_ShouldReturnEmployee() {
        when(cache.containsKey(1L)).thenReturn(true);
        when(cache.get(1L)).thenReturn(employeeResponse);

        EmployeeResponse response = employeeService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(cache).containsKey(1L);
        verify(cache).get(1L);
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    void getById_WhenEmployeeExistsNotInCache_ShouldFetchAndCache() {
        when(cache.containsKey(1L)).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse response = employeeService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        verify(cache).containsKey(1L);
        verify(employeeRepository).findById(1L);
        verify(employeeMapper, times(2)).toEmployeeResponse(employee);
        verify(cache).put(1L, employeeResponse);
    }

    @Test
    void getById_WhenEmployeeNotFound_ShouldThrowException() {
        when(cache.containsKey(1L)).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.getById(1L));

        assertEquals("404 NOT_FOUND \"Employee not found with such id = 1\"", exception.getMessage());
        verify(employeeRepository).findById(1L);
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void create_WhenPhoneNumberExists_ShouldThrowConflictException() {
        when(employeeRepository.existsByPhoneNumber("+375291234567")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> employeeService.create(employeeRequest));

        assertEquals("Phone number +375291234567 is already in use", exception.getMessage());
        verify(employeeRepository).existsByPhoneNumber("+375291234567");
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void create_WhenEmailExists_ShouldThrowConflictException() {
        when(employeeRepository.existsByPhoneNumber("+375291234567")).thenReturn(false);
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> employeeService.create(employeeRequest));

        assertEquals("Email john.doe@example.com is already in use", exception.getMessage());
        verify(employeeRepository).existsByPhoneNumber("+375291234567");
        verify(employeeRepository).existsByEmail("john.doe@example.com");
        verifyNoInteractions(employeeMapper);
    }

    @Test
    void create_WhenNoConflicts_ShouldSaveAndReturnEmployee() {
        when(employeeRepository.existsByPhoneNumber("+375291234567")).thenReturn(false);
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(employeeMapper.toEmployee(employeeRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse response = employeeService.create(employeeRequest);

        assertNotNull(response);
        assertEquals("John", response.firstName());
        verify(employeeRepository).existsByPhoneNumber("+375291234567");
        verify(employeeRepository).existsByEmail("john.doe@example.com");
        verify(employeeRepository).save(employee);
        verify(employeeMapper).toEmployee(employeeRequest);
        verify(employeeMapper).toEmployeeResponse(employee);
    }

    @Test
    void update_WhenEmployeeExistsAndNoConflicts_ShouldUpdateAndReturnEmployee() {
        EmployeeRequest updateRequest = new EmployeeRequest("Jane", "Smith", "+375299876543", "jane.smith@example.com");
        when(employeeRepository.existsByPhoneNumber(updateRequest.phoneNumber())).thenReturn(false);
        when(employeeRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.partialUpdate(updateRequest, employee)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        EmployeeResponse response = employeeService.update(1L, updateRequest);

        assertNotNull(response);
        assertEquals("John", response.firstName());
        verify(employeeRepository).existsByPhoneNumber(updateRequest.phoneNumber());
        verify(employeeRepository).existsByEmail(updateRequest.email());
        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employee);
        verify(employeeMapper).partialUpdate(updateRequest, employee);
        verify(employeeMapper).toEmployeeResponse(employee);
    }

    @Test
    void update_WhenPhoneNumberExists_ShouldThrowConflictException() {
        EmployeeRequest updateRequest = new EmployeeRequest("Jane", "Smith", "+375299876543", "jane.smith@example.com");
        when(employeeRepository.existsByPhoneNumber(updateRequest.phoneNumber())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> employeeService.update(1L, updateRequest));

        assertEquals("Phone number +375299876543 is already in use", exception.getMessage());
        verify(employeeRepository).existsByPhoneNumber(updateRequest.phoneNumber());
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    void update_WhenEmailExists_ShouldThrowConflictException() {
        EmployeeRequest updateRequest = new EmployeeRequest("Jane", "Smith", "+375299876543", "jane.smith@example.com");
        when(employeeRepository.existsByPhoneNumber(updateRequest.phoneNumber())).thenReturn(false);
        when(employeeRepository.existsByEmail(updateRequest.email())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> employeeService.update(1L, updateRequest));

        assertEquals("Email jane.smith@example.com is already in use", exception.getMessage());
        verify(employeeRepository).existsByPhoneNumber(updateRequest.phoneNumber());
        verify(employeeRepository).existsByEmail(updateRequest.email());
        verify(employeeRepository, never()).findById(anyLong());
    }

    @Test
    void update_WhenEmployeeNotFound_ShouldThrowException() {
        EmployeeRequest updateRequest = new EmployeeRequest("Jane", "Smith", "+375299876543", "jane.smith@example.com");
        when(employeeRepository.existsByPhoneNumber(updateRequest.phoneNumber())).thenReturn(false);
        when(employeeRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.update(1L, updateRequest));

        assertEquals("404 NOT_FOUND \"Employee not found with id = 1\"", exception.getMessage());
        verify(employeeRepository).existsByPhoneNumber(updateRequest.phoneNumber());
        verify(employeeRepository).existsByEmail(updateRequest.email());
        verify(employeeRepository).findById(1L);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_WhenEmployeeInCache_ShouldUpdateCache() {
        EmployeeRequest updateRequest = new EmployeeRequest("Jane", "Smith", "+375299876543", "jane.smith@example.com");
        when(employeeRepository.existsByPhoneNumber(updateRequest.phoneNumber())).thenReturn(false);
        when(employeeRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeMapper.partialUpdate(updateRequest, employee)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);
        when(cache.containsKey(1L)).thenReturn(true);

        EmployeeResponse response = employeeService.update(1L, updateRequest);

        assertNotNull(response);
        verify(cache).containsKey(1L);
        verify(cache).put(1L, employeeResponse);
        verify(employeeRepository).save(employee);
    }

    @Test
    void delete_WhenEmployeeExists_ShouldDeleteEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        employeeService.delete(1L);

        verify(employeeRepository).existsById(1L);
        verify(employeeRepository).deleteById(1L);
        verify(cache).remove(1L);
    }

    @Test
    void delete_WhenEmployeeNotFound_ShouldThrowException() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.delete(1L));

        assertEquals("404 NOT_FOUND \"Employee not found with id = 1\"", exception.getMessage());
        verify(employeeRepository).existsById(1L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchEmployeesByFirstName_WhenFound_ShouldReturnEmployees() {
        when(employeeRepository.findByFirstName("John")).thenReturn(List.of(employee));
        when(employeeMapper.toEmployeeResponseList(List.of(employee))).thenReturn(List.of(employeeResponse));
        when(cache.get(1L)).thenReturn(null); // Cache miss

        List<EmployeeResponse> responses = employeeService.searchEmployeesByFirstName("John");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(employeeResponse, responses.get(0));
        verify(employeeRepository).findByFirstName("John");
        verify(cache).put(1L, employeeResponse);
    }

    @Test
    void searchEmployeesByFirstName_WhenFoundInCache_ShouldReturnCachedEmployees() {
        when(employeeRepository.findByFirstName("John")).thenReturn(List.of(employee));
        when(employeeMapper.toEmployeeResponseList(List.of(employee))).thenReturn(List.of(employeeResponse));
        when(cache.get(1L)).thenReturn(employeeResponse); // Cache hit

        List<EmployeeResponse> responses = employeeService.searchEmployeesByFirstName("John");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(employeeResponse, responses.get(0));
        verify(employeeRepository).findByFirstName("John");
        verify(cache, never()).put(anyLong(), any());
    }

    @Test
    void searchEmployeesByFirstName_WhenNotFound_ShouldThrowException() {
        when(employeeRepository.findByFirstName("John")).thenReturn(Collections.emptyList());
        when(employeeMapper.toEmployeeResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.searchEmployeesByFirstName("John"));

        assertEquals("404 NOT_FOUND \"Nothing found\"", exception.getMessage());
        verify(employeeRepository).findByFirstName("John");
    }

    @Test
    void searchEmployeesByLastName_WhenFound_ShouldReturnEmployees() {
        when(employeeRepository.findByLastName("Doe")).thenReturn(List.of(employee));
        when(employeeMapper.toEmployeeResponseList(List.of(employee))).thenReturn(List.of(employeeResponse));
        when(cache.get(1L)).thenReturn(null); // Cache miss

        List<EmployeeResponse> responses = employeeService.searchEmployeesByLastName("Doe");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(employeeResponse, responses.get(0));
        verify(employeeRepository).findByLastName("Doe");
        verify(cache).put(1L, employeeResponse);
    }

    @Test
    void searchEmployeesByLastName_WhenNotFound_ShouldThrowException() {
        when(employeeRepository.findByLastName("Doe")).thenReturn(Collections.emptyList());
        when(employeeRepository.findByLastName("Doe")).thenReturn(Collections.emptyList());
        when(employeeMapper.toEmployeeResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.searchEmployeesByLastName("Doe"));

        assertEquals("404 NOT_FOUND \"Nothing found\"", exception.getMessage());
        verify(employeeRepository).findByLastName("Doe");
    }

    @Test
    void searchEmployeesByAssignmentId_WhenFound_ShouldReturnEmployees() {
        when(employeeRepository.findEmployeesByAssignmentId(2L)).thenReturn(List.of(employee));
        when(employeeMapper.toEmployeeResponseList(List.of(employee))).thenReturn(List.of(employeeResponse));
        when(cache.get(1L)).thenReturn(null); // Cache miss

        List<EmployeeResponse> responses = employeeService.searchEmployeesByAssignmentId(2L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(employeeResponse, responses.get(0));
        verify(employeeRepository).findEmployeesByAssignmentId(2L);
        verify(cache).put(1L, employeeResponse);
    }

    @Test
    void searchEmployeesByAssignmentId_WhenNotFound_ShouldThrowException() {
        when(employeeRepository.findEmployeesByAssignmentId(2L)).thenReturn(Collections.emptyList());
        when(employeeMapper.toEmployeeResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.searchEmployeesByAssignmentId(2L));

        assertEquals("404 NOT_FOUND \"Nothing found\"", exception.getMessage());
        verify(employeeRepository).findEmployeesByAssignmentId(2L);
    }

    @Test
    void addAssignmentToEmployee_WhenEmployeeNotFound_ShouldThrowException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.addAssignmentToEmployee(1L, 2L));

        assertEquals("404 NOT_FOUND \"Employee not found with such id = 1\"", exception.getMessage());
        verify(employeeRepository).findById(1L);
        verify(assignmentRepository, never()).findById(anyLong());
    }

    @Test
    void deleteAssignmentFromEmployee_WhenEmployeeNotFound_ShouldThrowException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.deleteAssignmentFromEmployee(1L, 2L));

        assertEquals("404 NOT_FOUND \"Employee not found with Id = 1\"", exception.getMessage());
        verify(employeeRepository).findById(1L);
        verify(assignmentRepository, never()).findById(anyLong());
    }

    @Test
    void bulkUpsertEmployees_WhenNewEmployee_ShouldCreate() {
        when(employeeRepository.existsByPhoneNumber("+375291234567")).thenReturn(false);
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(employeeMapper.toEmployee(employeeRequest)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toEmployeeResponse(employee)).thenReturn(employeeResponse);

        List<EmployeeResponse> responses = employeeService.bulkUpsertEmployees(List.of(employeeRequest));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(employeeResponse, responses.get(0));
        verify(employeeRepository, times(2)).existsByPhoneNumber("+375291234567");
        verify(employeeRepository, times(2)).existsByEmail("john.doe@example.com"); // Ожидаем 2 вызова
        verify(employeeRepository).save(employee);
    }

    @Test
    void bulkUpsertEmployees_WhenExistingEmployeeByPhone_ShouldUpdate() {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);
        existingEmployee.setPhoneNumber("+375291234567");
        existingEmployee.setEmail("old@example.com");
        when(employeeRepository.existsByPhoneNumber("+375291234567")).thenReturn(true);
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(employeeRepository.findAll()).thenReturn(List.of(existingEmployee));
        when(employeeMapper.partialUpdate(employeeRequest, existingEmployee)).thenReturn(existingEmployee);
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);
        when(employeeMapper.toEmployeeResponse(existingEmployee)).thenReturn(employeeResponse);

        List<EmployeeResponse> responses = employeeService.bulkUpsertEmployees(List.of(employeeRequest));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(employeeResponse, responses.get(0));
        verify(employeeRepository).existsByPhoneNumber("+375291234567");
        verify(employeeRepository, times(2)).existsByEmail("john.doe@example.com");
        verify(employeeRepository).findAll();
        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void bulkUpsertEmployees_WhenExistingEmployeeByEmail_ShouldUpdate() {
        Employee existingEmployee = new Employee();
        existingEmployee.setId(1L);
        existingEmployee.setPhoneNumber("old+375291234567");
        existingEmployee.setEmail("john.doe@example.com");
        when(employeeRepository.existsByPhoneNumber("+375291234567")).thenReturn(false);
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(true);
        when(employeeRepository.findAll()).thenReturn(List.of(existingEmployee));
        when(employeeMapper.partialUpdate(employeeRequest, existingEmployee)).thenReturn(existingEmployee);
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);
        when(employeeMapper.toEmployeeResponse(existingEmployee)).thenReturn(employeeResponse);

        List<EmployeeResponse> responses = employeeService.bulkUpsertEmployees(List.of(employeeRequest));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(employeeResponse, responses.get(0));
        verify(employeeRepository, times(2)).existsByPhoneNumber("+375291234567");
        verify(employeeRepository).existsByEmail("john.doe@example.com");
        verify(employeeRepository).findAll();
        verify(employeeRepository).save(existingEmployee);
    }
}
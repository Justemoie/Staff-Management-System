package com.example.sms.service.implementation;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.exception.ConflictException;
import com.example.sms.mapper.EmployeeMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.service.EmployeeService;
import com.example.sms.service.GenericService;
import com.example.sms.utils.cache.Cache;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class EmployeeServiceImpl implements
        GenericService<EmployeeResponse, EmployeeRequest, Long>, EmployeeService {

    private final Cache<Long, EmployeeResponse> cache;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AssignmentRepository assignmentRepository;

    public EmployeeServiceImpl(Cache<Long, EmployeeResponse> cache,
            EmployeeRepository employeesRepository,
            EmployeeMapper employeeMapper,
            AssignmentRepository assignmentRepository) {

        this.cache = cache;
        this.employeeRepository = employeesRepository;
        this.employeeMapper = employeeMapper;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<EmployeeResponse> getAll() {
        return employeeMapper.toEmployeeResponseList(employeeRepository.findAll());
    }

    @Override
    public EmployeeResponse getById(Long id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with such id = " + id));

        cache.put(id, employeeMapper.toEmployeeResponse(employee));
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    public EmployeeResponse create(EmployeeRequest employeeRequest) {
        if (employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber())) {
            throw new ConflictException("Phone number " + employeeRequest.phoneNumber()
                    + " is already in use");
        } else if (employeeRepository.existsByEmail(employeeRequest.email())) {
            throw new ConflictException("Email " + employeeRequest.email() + " is already in use");
        }
        return employeeMapper.toEmployeeResponse(
                employeeRepository.save(employeeMapper.toEmployee(employeeRequest)));
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest employeeRequest) {
        if (employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber())) {
            throw new ConflictException("Phone number "
                    + employeeRequest.phoneNumber() + " is already in use");
        }  else if (employeeRepository.existsByEmail(employeeRequest.email())) {
            throw new ConflictException("Email " + employeeRequest.email() + " is already in use");
        }

        Employee employeeToUpdate = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with id = " + id));

        Employee employee = employeeMapper.partialUpdate(employeeRequest, employeeToUpdate);
        Employee updatedEmployee = saveUpdates(employee);

        var employeeResponse = employeeMapper.toEmployeeResponse(updatedEmployee);

        return employeeResponse;
    }

    @Override
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Employee not found with id = " + id);
        }
        cache.remove(id);
        employeeRepository.deleteById(id);
    }

    @Override
    public List<EmployeeResponse> searchEmployeesByFirstName(String firstName) {
        var employees = employeeMapper
                .toEmployeeResponseList(employeeRepository.findByFirstName(firstName));

        return getEmployeeResponses(employees);
    }

    @Override
    public List<EmployeeResponse> searchEmployeesByAssignmentId(Long id) {
        var employees = employeeMapper.toEmployeeResponseList(
                employeeRepository.findEmployeesByAssignmentId(id));

        return getEmployeeResponses(employees);
    }

    @Override
    public EmployeeResponse addAssignmentToEmployee(Long employeeId, Long assignmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with such id = " + employeeId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + assignmentId));

        if (employee.getAssignments().contains(assignment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee with id = " + employeeId
                            + " already has assignment with id = " + assignmentId);
        }

        employee.getAssignments().add(assignment);

        return employeeMapper.toEmployeeResponse(saveUpdates(employee));
    }

    @Override
    public List<EmployeeResponse> searchEmployeesByLastName(String lastName) {
        var employees = employeeMapper.toEmployeeResponseList(
                employeeRepository.findByLastName(lastName));

        return getEmployeeResponses(employees);
    }

    private List<EmployeeResponse> getEmployeeResponses(List<EmployeeResponse> employees) {
        if (employees.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nothing found");
        }

        List<EmployeeResponse> cachedEmployees = employees.stream()
                .map(employee -> cache.get(employee.id()))
                .filter(Objects::nonNull)
                .toList();

        if (cachedEmployees.size() == employees.size()) {
            return cachedEmployees;
        }

        employees.forEach(employee -> cache.put(employee.id(), employee));


        return employees;
    }

    @Override
    public EmployeeResponse deleteAssignmentFromEmployee(Long employeeId, Long assignmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with Id = " + employeeId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with Id = " + assignmentId));

        if (!employee.getAssignments().contains(assignment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee with id = " + employeeId
                            + " does not have assignment with id = " + assignmentId);
        }

        employee.getAssignments().remove(assignment);

        return employeeMapper.toEmployeeResponse(saveUpdates(employee));
    }

    @Override
    public List<EmployeeResponse> bulkUpsertEmployees(List<EmployeeRequest> employeeRequests) {
        return employeeRequests.stream()
                .map(this::processEmployeeRequest)
                .collect(Collectors.toList());
    }

    private EmployeeResponse processEmployeeRequest(EmployeeRequest employeeRequest) {
        Employee existingEmployee = findExistingEmployee(employeeRequest);
        if (existingEmployee != null) {
            return updateExistingEmployee(employeeRequest, existingEmployee);
        } else {
            return createNewEmployee(employeeRequest);
        }
    }

    private Employee findExistingEmployee(EmployeeRequest employeeRequest) {
        boolean existsByPhone = employeeRepository
                .existsByPhoneNumber(employeeRequest.phoneNumber());
        boolean existsByEmail = employeeRepository
                .existsByEmail(employeeRequest.email());

        if (existsByPhone || existsByEmail) {
            return employeeRepository.findAll().stream()
                    .filter(emp -> emp.getPhoneNumber().equals(employeeRequest.phoneNumber())
                            || emp.getEmail().equals(employeeRequest.email()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private EmployeeResponse updateExistingEmployee(
            EmployeeRequest employeeRequest, Employee existingEmployee) {
        if (!existingEmployee.getPhoneNumber().equals(employeeRequest.phoneNumber())
                && employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber())) {
            throw new ConflictException(
                    "Phone number " + employeeRequest.phoneNumber()
                            + " is already in use by another employee");
        }
        if (!existingEmployee.getEmail().equals(employeeRequest.email())
                && employeeRepository.existsByEmail(employeeRequest.email())) {
            throw new ConflictException(
                    "Email " + employeeRequest.email() + " is already in use by another employee");
        }

        Employee employeeToUpdate = employeeMapper.partialUpdate(employeeRequest, existingEmployee);
        Employee updatedEmployee = saveUpdates(employeeToUpdate);
        return employeeMapper.toEmployeeResponse(updatedEmployee);
    }

    private EmployeeResponse createNewEmployee(EmployeeRequest employeeRequest) {
        if (employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber())) {
            throw new ConflictException(
                    "Phone number " + employeeRequest.phoneNumber() + " is already in use");
        }
        if (employeeRepository.existsByEmail(employeeRequest.email())) {
            throw new ConflictException(
                    "Email " + employeeRequest.email() + " is already in use");
        }

        Employee newEmployee = employeeMapper.toEmployee(employeeRequest);
        Employee savedEmployee = employeeRepository.save(newEmployee);
        return employeeMapper.toEmployeeResponse(savedEmployee);
    }

    private Employee saveUpdates(Employee employee) {
        if (cache.containsKey(employee.getId())) {
            cache.put(employee.getId(), employeeMapper.toEmployeeResponse(employee));
        }
        return employeeRepository.save(employee);
    }
}

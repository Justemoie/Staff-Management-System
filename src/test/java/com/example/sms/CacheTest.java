package com.example.sms;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.entity.Employee;
import com.example.sms.mapper.EmployeeMapper;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.service.EmployeeService;
import com.example.sms.utils.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

class EmployeeServiceImplTest {

    @Mock
    private Cache<String, List<EmployeeResponse>> cache;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchEmployeesByFirstName_WhenCacheHit() {
        // Готовим данные
        String firstName = "John";
        List<EmployeeResponse> cachedEmployees = List.of(
                new EmployeeResponse(1L, "John", "Doe", "123-456", "john.doe@example.com", List.of()),
                new EmployeeResponse(2L, "John", "Smith", "789-012", "john.smith@example.com", List.of()),
                new EmployeeResponse(2L, "Bob", "Smith", "789-012", "john.smith@example.com", List.of())
        );

        // Имитация поведения кэша
        when(cache.get(firstName)).thenReturn(cachedEmployees);

        // Вызов тестируемого метода
        List<EmployeeResponse> result = employeeService.searchEmployeesByFirstName(firstName);

        // Проверка
        assertEquals(cachedEmployees, result);
        verify(cache, times(1)).get(firstName);
        verifyNoInteractions(employeeRepository); // База данных не должна быть вызвана
    }


    @Test
    void testSearchEmployeesByFirstName_WhenNotFound() {
        // Готовим данные
        String firstName = "John";

        // Имитация поведения кэша и репозитория
        when(cache.get(firstName)).thenReturn(null);
        when(employeeRepository.findByFirstName(firstName)).thenReturn(List.of()); // Пустой результат из БД

        // Проверяем, что выбрасывается исключение
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> employeeService.searchEmployeesByFirstName(firstName));

        // Проверка сообщения об ошибке
        assertEquals("404 NOT_FOUND \"Nothing found\"", exception.getMessage());
        verify(cache, times(1)).get(firstName);
        verify(employeeRepository, times(1)).findByFirstName(firstName); // База данных вызывается
        verifyNoMoreInteractions(cache); // Данные не сохраняются в кэше
    }
}

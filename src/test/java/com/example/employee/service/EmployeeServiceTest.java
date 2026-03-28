package com.example.employee.service;

import com.example.employee.entity.Employee;
import com.example.employee.entity.EmployeeStatus;
import com.example.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
    }

    @Test
    void testCreateEmployeeByAdminShouldBeApproved() {
        Employee employee = new Employee();
        employee.setName("John Doe");

        org.springframework.security.core.GrantedAuthority authority = () -> "ROLE_ADMIN";
        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.Collections.singletonList(authority);
        doReturn(authorities).when(authentication).getAuthorities();
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArguments()[0]);

        Employee saved = employeeService.createEmployee(employee);

        assertEquals(EmployeeStatus.APPROVED, saved.getStatus());
        verify(auditLogService).logAction(eq("CREATE_EMPLOYEE"), anyString());
    }

    @Test
    void testRestoreEmployee() {
        Long id = 1L;
        Employee employee = new Employee();
        employee.setId(id);
        employee.setDeleted(true);

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee restored = employeeService.restoreEmployee(id);

        assertFalse(restored.isDeleted());
        verify(employeeRepository).save(employee);
    }
}

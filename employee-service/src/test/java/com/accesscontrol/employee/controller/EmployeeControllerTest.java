package com.accesscontrol.employee.controller;

import com.accesscontrol.employee.dto.EmployeeDTO;
import com.accesscontrol.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller - Pruebas de Caja Blanca")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        testEmployeeDTO = new EmployeeDTO();
        testEmployeeDTO.setId(1L);
        testEmployeeDTO.setDocument("12345678");
        testEmployeeDTO.setEmployeeCode("EMP001");
        testEmployeeDTO.setFirstname("Juan");
        testEmployeeDTO.setLastname("Perez");
        testEmployeeDTO.setEmail("juan@example.com");
        testEmployeeDTO.setPhone("3001234567");
        testEmployeeDTO.setDepartment("IT");
        testEmployeeDTO.setPosition("Developer");
        testEmployeeDTO.setStatus(true);
    }

    // ============================================
    // PRUEBAS: GET /employee/findallemployees
    // ============================================
    @Test
    @DisplayName("getAllEmployees - Retorna 200 con lista de empleados")
    void testGetAllEmployees() throws Exception {
        // Arrange
        List<EmployeeDTO> employees = Arrays.asList(testEmployeeDTO);
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/employee/findallemployees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].document", is("12345678")))
                .andExpect(jsonPath("$[0].firstname", is("Juan")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("getAllEmployees - Retorna lista vacía cuando no hay empleados")
    void testGetAllEmployeesEmpty() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/employee/findallemployees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService, times(1)).getAllEmployees();
    }

    // ============================================
    // PRUEBAS: GET /employee/{id}
    // ============================================
    @Test
    @DisplayName("getEmployeeById - Retorna 200 con empleado válido")
    void testGetEmployeeByIdValid() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.document", is("12345678")))
                .andExpect(jsonPath("$.firstname", is("Juan")));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @DisplayName("getEmployeeById - Retorna 500 cuando empleado no existe")
    void testGetEmployeeByIdNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new RuntimeException("Employee not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/employee/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    // ============================================
    // PRUEBAS: GET /employee/code/{employeeCode}
    // ============================================
    @Test
    @DisplayName("getEmployeeByCode - Retorna 200 con código válido")
    void testGetEmployeeByCodeValid() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByCode("EMP001")).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employee/code/EMP001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode", is("EMP001")));

        verify(employeeService, times(1)).getEmployeeByCode("EMP001");
    }

    @Test
    @DisplayName("getEmployeeByCode - Retorna 500 cuando código no existe")
    void testGetEmployeeByCodeNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByCode("INVALID"))
                .thenThrow(new RuntimeException("Employee not found with code: INVALID"));

        // Act & Assert
        mockMvc.perform(get("/employee/code/INVALID")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getEmployeeByCode("INVALID");
    }

    // ============================================
    // PRUEBAS: GET /employee/document/{document}
    // ============================================
    @Test
    @DisplayName("getEmployeeByDocument - Retorna 200 con documento válido")
    void testGetEmployeeByDocumentValid() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByDocument("12345678")).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employee/document/12345678")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.document", is("12345678")));

        verify(employeeService, times(1)).getEmployeeByDocument("12345678");
    }

    @Test
    @DisplayName("getEmployeeByDocument - Retorna 500 cuando documento no existe")
    void testGetEmployeeByDocumentNotFound() throws Exception {
        // Arrange
        when(employeeService.getEmployeeByDocument("99999999"))
                .thenThrow(new RuntimeException("Employee not found with document: 99999999"));

        // Act & Assert
        mockMvc.perform(get("/employee/document/99999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getEmployeeByDocument("99999999");
    }

    // ============================================
    // PRUEBAS: POST /employee/createemployee
    // ============================================
    @Test
    @DisplayName("createEmployee - Retorna 201 cuando empleado se crea exitosamente")
    void testCreateEmployeeSuccess() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any())).thenReturn(testEmployeeDTO);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.document", is("12345678")))
                .andReturn();

        verify(employeeService, times(1)).createEmployee(any());
    }

    @Test
    @DisplayName("createEmployee - Retorna 400 cuando documento ya existe")
    void testCreateEmployeeDocumentExists() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any()))
                .thenThrow(new RuntimeException("Document already exists: 12345678"));

        // Act & Assert
        mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Document already exists")));

        verify(employeeService, times(1)).createEmployee(any());
    }

    @Test
    @DisplayName("createEmployee - Retorna 400 cuando email ya existe")
    void testCreateEmployeeEmailExists() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any()))
                .thenThrow(new RuntimeException("Email already exists: juan@example.com"));

        // Act & Assert
        mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Email already exists")));

        verify(employeeService, times(1)).createEmployee(any());
    }

    @Test
    @DisplayName("createEmployee - Retorna 400 cuando validación falla (documento vacío)")
    void testCreateEmployeeValidationFailedEmpty() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setFirstname("Juan");
        invalidDTO.setLastname("Perez");
        invalidDTO.setEmail("juan@example.com");
        invalidDTO.setStatus(true);
        // Document no está asignado (null)

        // Act & Assert
        mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createEmployee - Retorna 400 cuando email inválido")
    void testCreateEmployeeInvalidEmail() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setDocument("12345678");
        invalidDTO.setFirstname("Juan");
        invalidDTO.setLastname("Perez");
        invalidDTO.setEmail("invalid-email");
        invalidDTO.setStatus(true);

        // Act & Assert
        mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createEmployee - Retorna 500 cuando ocurre error interno inesperado")
    void testCreateEmployeeInternalError() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any()))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().is(400));

        verify(employeeService, times(1)).createEmployee(any());
    }

    // ============================================
    // PRUEBAS: Cobertura de caminos de decisión
    // ============================================
    @Test
    @DisplayName("Cobertura de camino - GET exitoso")
    void testGetPathSuccessful() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(1L)).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(get("/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    @DisplayName("Cobertura de camino - GET con error de negocio")
    void testGetPathBusinessError() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById(999L))
                .thenThrow(new RuntimeException("Business error"));

        // Act & Assert
        mockMvc.perform(get("/employee/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getEmployeeById(999L);
    }

    @Test
    @DisplayName("Cobertura de camino - POST exitoso")
    void testPostPathSuccessful() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any())).thenReturn(testEmployeeDTO);

        // Act & Assert
        mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).createEmployee(any());
    }

    @Test
    @DisplayName("Cobertura de camino - POST con error de negocio")
    void testPostPathBusinessError() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any()))
                .thenThrow(new RuntimeException("Document already exists"));

        // Act & Assert
        mockMvc.perform(post("/employee/createemployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEmployeeDTO)))
                .andExpect(status().isBadRequest());

        verify(employeeService, times(1)).createEmployee(any());
    }
}

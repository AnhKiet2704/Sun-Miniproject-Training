package com.example.employeemanagement;

import com.example.employeemanagement.service.UtilityService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Module 2: kiểm tra UtilityService (không cần Spring context)
class UtilityServiceTest {

    private final UtilityService utilityService = new UtilityService();

    @Test
    void normalizeName_capitalizesEachWordAndTrimsSpaces() {
        assertEquals("Nguyen Van An", utilityService.normalizeName("  nguyen   VAN an "));
    }

    @Test
    void normalizeName_blankReturnsEmpty() {
        assertEquals("", utilityService.normalizeName("   "));
        assertEquals("", utilityService.normalizeName(null));
    }

    @Test
    void formatDepartment_uppercasesAndDefaultsToGeneral() {
        assertEquals("IT", utilityService.formatDepartment(" it "));
        assertEquals("GENERAL", utilityService.formatDepartment(""));
        assertEquals("GENERAL", utilityService.formatDepartment(null));
    }

    @Test
    void generateEmployeeCode_usesPaddedId() {
        assertEquals("EMP00007", utilityService.generateEmployeeCode(7L));
        assertEquals("EMP12345", utilityService.generateEmployeeCode(12345L));
    }
}

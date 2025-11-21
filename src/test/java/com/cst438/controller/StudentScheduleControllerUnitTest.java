package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.cst438.service.GradebookServiceProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentScheduleControllerUnitTest {

    @Autowired
    private WebTestClient client ;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    // default behavior for a Mock bean
    // return 0 or null for a method that returns a value
    // for method that returns void, the mock method records the call but does nothing
    @MockitoBean
    GradebookServiceProxy gradebookService;
    Random random = new Random();

    // test student login, add course, & delete course from student's schedule
    @Test
    public void studentScheduleCreateDelete() throws Exception {
        /* Student Login */
        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        // login user in GET request with email and password authentication
        EntityExchangeResult<LoginDTO> login_dto =  client.get().uri("/login")
                .headers(headers -> headers.setBasicAuth(studentEmail, password))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class).returnResult();

        // get bearer token
        String jwt = login_dto.getResponseBody().jwt();
        assertNotNull(jwt);

        int sectionNo = 1; // default section already in database

        /* Add Course to Student's Schedule */
        // add a course to the student's schedule in POST request with sectionNo as path var
        EntityExchangeResult<EnrollmentDTO> enrollmentResponse =  client.post().uri("/enrollments/sections/"+sectionNo)
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDTO.class).returnResult();
        EnrollmentDTO actualEnrollment = enrollmentResponse.getResponseBody();
        assertTrue(actualEnrollment.enrollmentId()>0, "primary key is invalid");

        // check that the sendMessage from registrar to gradebook was called as expected
        verify(gradebookService, times(1)).sendMessage(eq("addEnrollment"), any());

        // check that the new Enrollment exists in the database
        int userId = actualEnrollment.studentId();
        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, userId);

        assertNotNull(e, "enrollment created was ok but enrollment is not in database");
        assertNull(e.getGrade(), "enrollment created was ok but grade should be null");

        /* Drop Course from Student's Schedule */
        // drop a course from the student's schedule in DELETE request with enrollmentId as path var
        int enrollmentId = e.getEnrollmentId(); // get the enrollmentId created in this test
        client.delete().uri("/enrollments/"+enrollmentId)
                .headers(headers -> headers.setBearerAuth(jwt))
                .exchange()
                .expectStatus().isOk();

        // check that the student's enrollment no longer exists in the database
        e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, userId);
        assertNull(e, "enrollment was not deleted from database");

        // check that the sendMessage to gradebook service was called as expected
        verify(gradebookService, times(1)).sendMessage(eq("deleteEnrollment"), any());
    }

    @Test
    public void createStudentScheduleAlreadyEnrolled() throws Exception {

    }

    @Test
    public void deleteStudentScheduleNotEnrolled() throws Exception {

    }
}

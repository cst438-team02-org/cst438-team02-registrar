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

import java.sql.Date;
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
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TermRepository termRepository;

    // default behavior for a Mock bean
    // return 0 or null for a method that returns a value
    // for method that returns void, the mock method records the call but does nothing
    @MockitoBean
    GradebookServiceProxy gradebookService;
    Random random = new Random();

    // test student login, add course, & delete course from student's schedule
    @Test
    public void studentScheduleCreateDelete() throws Exception {

        // reset databases used in this test
        enrollmentRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        termRepository.deleteAll();

        /* Student Login */
        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        String jwt = loginUser(studentEmail, password);

        /* Generate Term */
        Term t = createTerm(1,
                2026,
                "Spring",
                "2025-11-01",
                "2026-04-30",
                "2026-04-30",
                "2026-01-15",
                "2026-05-17");

        /* Generate Course */
        Course c = createCourse("cst311", "Security", 3);

        /* Generate Section */
        Section s = createSection(c,
                t,
                111,
                "99",
                "b101",
                "W F 10-11",
                "ted@csumb.edu");

        /* Add Course to Student's Schedule */
        // add a course to the student's schedule in POST request with sectionNo as path var
        EntityExchangeResult<EnrollmentDTO> enrollmentResponse =  client.post().uri("/enrollments/sections/"+s.getSectionNo())
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
        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(s.getSectionNo(), userId);
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
        e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(s.getSectionNo(), userId);
        assertNull(e, "enrollment was not deleted from database");

        // check that the sendMessage to gradebook service was called as expected
        verify(gradebookService, times(1)).sendMessage(eq("deleteEnrollment"), any());
    }

    @Test
    public void createStudentScheduleAlreadyEnrolled() throws Exception {

        // reset databases used in this test
        enrollmentRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        termRepository.deleteAll();

        /* Student Login */
        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        String jwt = loginUser(studentEmail, password);

        /* Generate Term */
        Term t = createTerm(1,
                2026,
                "Spring",
                "2025-11-01",
                "2026-04-30",
                "2026-04-30",
                "2026-01-15",
                "2026-05-17");

        /* Generate Course */
        Course c = createCourse("cst311", "Security", 3);

        /* Generate Section */
        Section s = createSection(c,
                t,
                111,
                "99",
                "b101",
                "W F 10-11",
                "ted@csumb.edu");

        /* Add Course to Student's Schedule */
        // add a course to the student's schedule in POST request with sectionNo as path var
        EntityExchangeResult<EnrollmentDTO> enrollmentResponse =  client.post().uri("/enrollments/sections/"+s.getSectionNo())
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
        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(s.getSectionNo(), userId);
        assertNotNull(e, "enrollment created was ok but enrollment is not in database");
        assertNull(e.getGrade(), "enrollment created was ok but grade should be null");

        /* Attempt to Re-add Course to Student's Schedule */
        // add a course to the student's schedule in POST request with sectionNo as path var
        client.post().uri("/enrollments/sections/"+s.getSectionNo())
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[?(@=='student already enrolled in course')]").exists();
    }

    @Test
    public void deleteStudentScheduleNotEnrolled() throws Exception {

        // reset databases used in this test
        enrollmentRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        termRepository.deleteAll();

        /* Student Login */
        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        String jwt = loginUser(studentEmail, password);

        int enrollmentId = 0; // non-existent enrollmentId

        /* Attempt to add Course to Student's Schedule */
        // add a course to the student's schedule in POST request with non-existent enrollmentId as path var
        client.delete().uri("/enrollments/"+enrollmentId)
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void createStudentScheduleInvalidDate() throws Exception {

        // reset databases used in this test
        enrollmentRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        termRepository.deleteAll();

        /* Student Login */
        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        String jwt = loginUser(studentEmail, password);

        /* After Add Deadline */
        /* Generate Term */
        Term t = createTerm(1,
                2026,
                "Spring",
                "2024-11-01",
                "2025-04-30",
                "2026-04-30",
                "2026-01-15",
                "2026-05-17");

        /* Generate Course */
        Course c = createCourse("cst311", "Security", 3);

        /* Generate Section */
        Section s = createSection(c,
                t,
                111,
                "99",
                "b101",
                "W F 10-11",
                "ted@csumb.edu");

        /* Add Course to Student's Schedule */
        // add a course to the student's schedule in POST request with sectionNo as path var
        client.post().uri("/enrollments/sections/"+s.getSectionNo())
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[?(@=='add deadline passed')]").exists();

        /* Before Add Date */
        /* Generate Term */
        t = createTerm(2,
                2027,
                "Spring",
                "2026-11-01",
                "2026-04-30",
                "2026-04-30",
                "2026-01-15",
                "2026-05-17");

        /* Generate Course */
        c = createCourse("cst322", "Security3", 5);

        /* Generate Section */
        s = createSection(c,
                t,
                111,
                "99",
                "b101",
                "W F 10-11",
                "ted@csumb.edu");

        /* Add Course to Student's Schedule */
        // add a course to the student's schedule in POST request with sectionNo as path var
        client.post().uri("/enrollments/sections/"+s.getSectionNo())
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[?(@=='course not accepting enrollments yet')]").exists();
    }

    private String loginUser(String email, String password) {
        // login user in GET request with email and password authentication
        EntityExchangeResult<LoginDTO> login_dto =  client.get().uri("/login")
                .headers(headers -> headers.setBasicAuth(email, password))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class).returnResult();

        // get bearer token
        String jwt = login_dto.getResponseBody().jwt();
        assertNotNull(jwt);
        return jwt;
    }

    // create a term in the database
    private Term createTerm(int termId,
                            int year,
                            String semester,
                            String addDate,
                            String addDeadline,
                            String dropDeadline,
                            String startDate,
                            String endDate) {
        Term t = new Term();
        t.setTermId(termId);
        t.setYear(year);
        t.setSemester(semester);
        t.setAddDate(Date.valueOf(addDate));
        t.setAddDeadline(Date.valueOf(addDeadline));
        t.setDropDeadline(Date.valueOf(dropDeadline));
        t.setStartDate(Date.valueOf(startDate));
        t.setEndDate(Date.valueOf(endDate));
        termRepository.save(t);
        return t;
    }

    // create a course in the database
    private Course createCourse(String courseId,
                                String title,
                                int credits) {
        Course c = new Course();
        c.setCourseId(courseId);
        c.setTitle(title);
        c.setCredits(credits);
        courseRepository.save(c);
        return c;
    }

    // create a section in the database
    private Section createSection(Course c,
                                  Term t,
                                  int sectionId,
                                  String building,
                                  String room,
                                  String times,
                                  String instructorEmail) {
        Section s = new Section();
        s.setCourse(c);
        s.setTerm(t);
        s.setSectionId(sectionId);
        s.setBuilding(building);
        s.setRoom(room);
        s.setTimes(times);
        s.setInstructorEmail(instructorEmail);
        sectionRepository.save(s);
        return s;
    }
}

package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.LoginDTO;
import com.cst438.service.GradebookServiceProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.sql.Date;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerUnitTest {

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

    // test listing student's currently enrolled course schedule with 1 course
    @Test
    public void studentListEnrolledCourseSingle() throws Exception {

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
        EnrollmentDTO eDTO = addCourse(jwt, s.getSectionNo());

        // check that the sendMessage from registrar to gradebook was called as expected
        verify(gradebookService, times(1)).sendMessage(eq("addEnrollment"), any());

        /* Get Student's Schedule */
        int year = eDTO.year();
        String semester = eDTO.semester();
        EntityExchangeResult<List<EnrollmentDTO>> enrollmentsResult =  client.get().uri("/enrollments?year="+year+"&semester="+semester)
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class).returnResult();

        List<EnrollmentDTO> enrollments = enrollmentsResult.getResponseBody();
        assertNotNull(enrollments, "enrollment failed - null");
        assertEquals(1, enrollments.size(), "enrollment failed - size does not equal 1");
        assertEquals(s.getSectionNo(), enrollments.get(0).sectionNo(), "sectionNo incorrect");
        assertEquals(c.getCourseId(), enrollments.get(0).courseId(), "courseId incorrect");
    }

    // test listing student's currently enrolled course schedule with 2 courses
    @Test
    public void studentListEnrolledCourseMultiple() throws Exception {

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
        Course c1 = createCourse("cst311", "Security", 3);
        Course c2 = createCourse("cst113", "Hacking", 3);

        /* Generate Section */
        Section s1 = createSection(c1,
                t,
                111,
                "99",
                "b101",
                "W F 10-11",
                "ted@csumb.edu");
        Section s2 = createSection(c2,
                t,
                222,
                "88",
                "b102",
                "M T 10-11",
                "ted@csumb.edu");

        /* Add Course to Student's Schedule */
        EnrollmentDTO eDTO = addCourse(jwt, s1.getSectionNo());
        addCourse(jwt, s2.getSectionNo());

        // check that the sendMessage from registrar to gradebook was called as expected
        verify(gradebookService, times(2)).sendMessage(eq("addEnrollment"), any());

        /* Get Student's Schedule */
        int year = eDTO.year();
        String semester = eDTO.semester();
        EntityExchangeResult<List<EnrollmentDTO>> enrollmentsResult =  client.get().uri("/enrollments?year="+year+"&semester="+semester)
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class).returnResult();

        List<EnrollmentDTO> enrollments = enrollmentsResult.getResponseBody();
        assertNotNull(enrollments, "enrollment failed - null");
        assertEquals(2, enrollments.size(), "enrollment failed - size does not equal 2");
    }

    // test listing student's currently enrolled course schedule without any enrolled courses
    @Test
    public void studentListEnrolledCourseNone() throws Exception {

        // reset databases used in this test
        enrollmentRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        termRepository.deleteAll();

        /* Student Login */
        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        String jwt = loginUser(studentEmail, password);

        /* Get Student's Schedule */
        int year = 2026;
        String semester = "Spring";
        EntityExchangeResult<List<EnrollmentDTO>> enrollmentsResult =  client.get().uri("/enrollments?year="+year+"&semester="+semester)
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class).returnResult();

        List<EnrollmentDTO> enrollments = enrollmentsResult.getResponseBody();
        assertEquals(0, enrollments.size(), "fail - size does not equal 0");
    }

    // test listing student's currently enrolled course schedule with 1 course
    @Test
    public void studentListTranscriptsSingle() throws Exception {

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
        EnrollmentDTO eDTO = addCourse(jwt, s.getSectionNo());

        // check that the sendMessage from registrar to gradebook was called as expected
        verify(gradebookService, times(1)).sendMessage(eq("addEnrollment"), any());

        /* Get Student's Transcript */
        EntityExchangeResult<List<EnrollmentDTO>> enrollmentsResult =  client.get().uri("/transcripts")
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class).returnResult();

        List<EnrollmentDTO> enrollments = enrollmentsResult.getResponseBody();
        assertNotNull(enrollments, "enrollment failed - null");
        assertEquals(1, enrollments.size(), "enrollment failed - size does not equal 1");
        assertEquals(s.getSectionNo(), enrollments.get(0).sectionNo(), "sectionNo incorrect");
        assertEquals(c.getCourseId(), enrollments.get(0).courseId(), "courseId incorrect");
    }

    // test listing student's transcripts with 2 courses
    @Test
    public void studentListTranscriptsMultiple() throws Exception {

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
        Course c1 = createCourse("cst311", "Security", 3);
        Course c2 = createCourse("cst113", "Hacking", 3);

        /* Generate Section */
        Section s1 = createSection(c1,
                t,
                111,
                "99",
                "b101",
                "W F 10-11",
                "ted@csumb.edu");
        Section s2 = createSection(c2,
                t,
                222,
                "88",
                "b102",
                "M T 10-11",
                "ted@csumb.edu");

        /* Add Course to Student's Schedule */
        EnrollmentDTO eDTO = addCourse(jwt, s1.getSectionNo());
        addCourse(jwt, s2.getSectionNo());

        // check that the sendMessage from registrar to gradebook was called as expected
        verify(gradebookService, times(2)).sendMessage(eq("addEnrollment"), any());

        /* Get Student's Transcripts */
        int year = eDTO.year();
        String semester = eDTO.semester();
        EntityExchangeResult<List<EnrollmentDTO>> enrollmentsResult =  client.get().uri("/transcripts")
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class).returnResult();

        List<EnrollmentDTO> enrollments = enrollmentsResult.getResponseBody();
        assertNotNull(enrollments, "enrollment failed - null");
        assertEquals(2, enrollments.size(), "enrollment failed - size does not equal 2");
    }

    // test listing student's transcripts without any enrolled courses
    @Test
    public void studentListTranscriptsNone() throws Exception {

        // reset databases used in this test
        enrollmentRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        termRepository.deleteAll();

        /* Student Login */
        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        String jwt = loginUser(studentEmail, password);

        /* Get Student's Transcripts */
        int year = 2026;
        String semester = "Spring";
        EntityExchangeResult<List<EnrollmentDTO>> enrollmentsResult =  client.get().uri("/transcripts")
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class).returnResult();

        List<EnrollmentDTO> enrollments = enrollmentsResult.getResponseBody();
        assertEquals(0, enrollments.size(), "fail - size does not equal 0");
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

    // add a course to the student's schedule
    private EnrollmentDTO addCourse(String jwt, int sectionNo) {
        // add a course to the student's schedule in POST request with sectionNo as path var
        EntityExchangeResult<EnrollmentDTO> enrollmentResponse =  client.post().uri("/enrollments/sections/"+sectionNo)
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDTO.class).returnResult();
        EnrollmentDTO actualEnrollment = enrollmentResponse.getResponseBody();
        assertTrue(actualEnrollment.enrollmentId()>0, "primary key is invalid");

        return actualEnrollment;
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


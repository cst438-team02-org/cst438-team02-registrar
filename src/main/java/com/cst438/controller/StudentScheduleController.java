package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.service.GradebookServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentScheduleController {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final GradebookServiceProxy gradebook;

    public StudentScheduleController(
            EnrollmentRepository enrollmentRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            GradebookServiceProxy gradebook
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.gradebook = gradebook;
    }

    // student enrolls in a course
    @PostMapping("/enrollments/sections/{sectionNo}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(@PathVariable int sectionNo, Principal principal ) throws Exception  {

        User u = userRepository.findByEmail(principal.getName());

        // check that the section entity exists in the database
        Section s = sectionRepository.findById(sectionNo).orElse(null);
        if (s == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid section id");
        }

        // check that the term entity exists in the database
        Term t = s.getTerm();
        if (t == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid term");
        }

        // check that student is not already enrolled in the section
        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, u.getId());
        if (e != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student already enrolled in course");
        }

        // check that the current date is not before addDate, not after addDeadline
        // of the section's term.
        if (Date.valueOf(LocalDate.now()).before(t.getAddDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "course not accepting enrollments yet");
        } else if (Date.valueOf(LocalDate.now()).after(t.getAddDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "add deadline passed");
        }

        // create and save an Enrollment Entity
        // relate enrollment to the student's User entity and to the Section entity
        e = new Enrollment();
        e.setSection(s);
        e.setStudent(u);
        enrollmentRepository.save(e);

        // create an EnrollmentDTO with the id of the
        // Enrollment and other fields.
        EnrollmentDTO eDTO = new EnrollmentDTO(
                e.getEnrollmentId(),
                null,
                u.getId(),
                u.getName(),
                u.getEmail(),
                s.getCourse().getCourseId(),
                s.getCourse().getTitle(),
                s.getSectionId(),
                s.getSectionNo(),
                s.getBuilding(),
                s.getRoom(),
                s.getTimes(),
                s.getCourse().getCredits(),
                s.getTerm().getYear(),
                s.getTerm().getSemester()
        );

        // send a message to the gradebook service
        gradebook.sendMessage("addEnrollment", eDTO);

        // Return an EnrollmentDTO with the id of the
		// Enrollment and other fields.
        return eDTO;
    }

    // student drops a course
    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId, Principal principal) throws Exception {

        // check that the enrollment entity exists in the database
        Enrollment e = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student not enrolled in course");
        }

        // check that the section entity exists in the database
        Section s = e.getSection();

        // check that the term entity exists in the database
        Term t = s.getTerm();

        // check that enrollment belongs to the logged in student
        User u = userRepository.findByEmail(principal.getName());
        e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(s.getSectionNo(), u.getId());
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student not enrolled in course");
        }

		// check that today is not after the dropDeadLine for the term.
        if (Date.valueOf(LocalDate.now()).after(t.getDropDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "drop deadline passed");
        }

        enrollmentRepository.deleteById(enrollmentId);

        gradebook.sendMessage("deleteEnrollment", enrollmentId);
    }

}

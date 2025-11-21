package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {

   private final EnrollmentRepository enrollmentRepository;
   private final UserRepository userRepository;
    private final SectionRepository sectionRepository;

    public StudentController(
           EnrollmentRepository enrollmentRepository,
           UserRepository userRepository,
           SectionRepository sectionRepository) {
       this.enrollmentRepository = enrollmentRepository;
       this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
    }

   // retrieve schedule for student for a term
   @GetMapping("/enrollments")
   @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           Principal principal) {

       User u = userRepository.findByEmail(principal.getName());

       // use the EnrollmentController findByYearAndSemesterOrderByCourseId
       // method to retrieve the enrollments given the year, semester and id
       // of the logged in student.
       // Return a list of EnrollmentDTO.
       return enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, u.getId()).stream().map( e -> {
           Section s = e.getSection();
           Course c = s.getCourse();
           Term t = s.getTerm();
           return new EnrollmentDTO(
                   e.getEnrollmentId(),
                   e.getGrade(),
                   u.getId(),
                   u.getName(),
                   u.getEmail(),
                   c.getCourseId(),
                   c.getTitle(),
                   s.getSectionId(),
                   s.getSectionNo(),
                   s.getBuilding(),
                   s.getRoom(),
                   s.getTimes(),
                   c.getCredits(),
                   t.getYear(),
                   t.getSemester()
           );
       }).toList();
   }

   // return transcript for student
    @GetMapping("/transcripts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(Principal principal) {

        // use the EnrollmentController findEnrollmentsByStudentIdOrderByTermId
		// method to retrive the enrollments given the id 
		// of the logged in student.
		// Return a list of EnrollmentDTO.
		
        return null;
    }
}
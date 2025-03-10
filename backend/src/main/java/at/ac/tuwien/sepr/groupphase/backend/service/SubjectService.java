package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SubjectCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SubjectDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Subject;
import at.ac.tuwien.sepr.groupphase.backend.entity.UserSubject;
import at.ac.tuwien.sepr.groupphase.backend.exception.TissClientException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubjectService {
    void setUserSubjects(ApplicationUser student, List<Long> traineeSubjects, List<Long> tutorSubjects) throws ValidationException;


    List<Subject> findSubjectById(List<Long> ids);


    /**
     * find all subjects that are assigned to a user.
     *
     * @param user a user entity
     * @return a list of subject assigned to a user
     */
    List<UserSubject> findSubjectsByUser(ApplicationUser user);

    /**
     * find subjects by queryString, included are Typ, title and number of subject.
     *
     * @param searchParam the query for search
     * @param pageable    the page filter
     * @return subjects by queryString
     */
    Page<Subject> findSubjectsBySearchParam(String searchParam, Pageable pageable);

    /**
     * updates a single subject.
     *
     * @param subject the values to be updated
     */
    Subject updateSubject(SubjectDetailDto subject) throws Exception;


    /**
     * deletes a Subject form the Database.
     *
     * @param id the Subject that is deleted
     */
    void deleteSubject(Long id);

    /**
     * creates a new Subject.
     *
     * @param subjectDetailDto Subject to create
     * @return created Subject
     * @throws ValidationException if Subject is not valid
     */
    Subject createSubject(SubjectCreateDto subjectDetailDto) throws ValidationException;

    /**
     * finds a Subject by its id.
     *
     * @param id the subject is
     * @return the persisted Subject for the corresponding id
     */
    Subject getSubjectById(Long id);


    /**
     * Creates a preview of a subject based on the course number and semester from the TISS system.
     *
     * @param courseNr the course number for which the subject preview is to be created
     * @param semester the semester for which the subject preview is to be created
     * @return a {@link Subject} object containing the preview of the subject for the specified course number and semester
     */
    Subject createSubjectPreviewFromTiss(String courseNr, String semester) throws TissClientException;
}

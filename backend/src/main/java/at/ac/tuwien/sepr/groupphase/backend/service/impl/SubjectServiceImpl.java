package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SubjectCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SubjectDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Subject;
import at.ac.tuwien.sepr.groupphase.backend.entity.UserSubject;
import at.ac.tuwien.sepr.groupphase.backend.entity.UserSubjectKey;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.TissClientException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.SubjectRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserSubjectRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.SubjectService;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.SubjectValidator;
import at.ac.tuwien.sepr.groupphase.backend.service.validators.UserSubjectValidator;
import at.ac.tuwien.sepr.groupphase.backend.tiss.TissClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserSubjectRepository userSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final UserSubjectValidator userSubjectValidator;
    private final SubjectValidator subjectValidator;
    private final TissClient tissClient;

    public SubjectServiceImpl(UserSubjectRepository userSubjectRepository, SubjectRepository subjectRepository,
                              UserSubjectValidator validator, SubjectValidator subjectValidator, TissClient tissClient) {
        this.userSubjectRepository = userSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.userSubjectValidator = validator;
        this.subjectValidator = subjectValidator;
        this.tissClient = tissClient;
    }

    @Override
    public void setUserSubjects(ApplicationUser student, List<Long> trainees, List<Long> tutors) throws ValidationException {
        LOGGER.trace("choose subjects with user: {}, trainee: {}, tutor: {}", student, trainees, tutors);
        userSubjectValidator.validateSubjectSelection(trainees, tutors, student);
        List<UserSubject> keys = userSubjectRepository.getUserSubjectByUser(student);
        for (UserSubject key : keys) {
            if (key != null) {
                userSubjectRepository.removeUserSubjectById(key.getId());
            }
        }
        List<Subject> traineeList = findSubjectById(trainees);
        List<Subject> tutorList = findSubjectById(tutors);
        for (Subject subject : traineeList) {
            userSubjectRepository.save(new UserSubject(new UserSubjectKey(student.getId(), subject.getId()), student, subject, "trainee"));
        }
        for (Subject subject : tutorList) {
            userSubjectRepository.save(new UserSubject(new UserSubjectKey(student.getId(), subject.getId()), student, subject, "tutor"));
        }
    }

    @Override
    public List<Subject> findSubjectById(List<Long> ids) {
        LOGGER.trace("findSubjectById: {}", ids);
        List<Subject> list = new ArrayList<>();
        for (Long id : ids) {
            list.add(subjectRepository.findSubjectById(id));
        }
        return list;
    }

    @Override
    public List<UserSubject> findSubjectsByUser(ApplicationUser user) {
        LOGGER.trace("findSubjectsByUserId: user {}", user);
        return userSubjectRepository.getUserSubjectByUser(user);
    }

    @Override
    public Page<Subject> findSubjectsBySearchParam(String searchParam, Pageable pageable) {
        LOGGER.trace("findSubjectsByQuery: searchParam {}", searchParam);
        return subjectRepository.findAllSubjectByQueryParam(searchParam, pageable);
    }

    @Override
    public Subject updateSubject(SubjectDetailDto subject) throws Exception {
        LOGGER.trace("updateSubject: subject{}", subject);
        subjectValidator.validateSubject(subject);
        Subject s = subjectRepository.findSubjectById(subject.getId());
        if (s == null) {
            throw new NotFoundException(String.format("Subject with id %d not found", subject.getId()));
        }
        saveSubjectDetailDto(subject, s);
        return s;
    }

    @Override
    public void deleteSubject(Long id) {
        LOGGER.trace("deleteSubject: id{}", id);
        Subject s = subjectRepository.findSubjectById(id);
        if (s == null) {
            throw new NotFoundException(String.format("Subject with id %d not found", id));
        }
        userSubjectRepository.deleteUserSubjectsBySubject(s);
        subjectRepository.deleteById(id);
    }

    @Override
    public Subject createSubject(SubjectCreateDto subject) throws ValidationException {
        LOGGER.trace("updateSubject: subject{}", subject);
        subjectValidator.validateSubject(subject);
        Subject s = new Subject();
        saveSubjectDetailDto(subject, s);
        return s;
    }

    @Override
    public Subject getSubjectById(Long id) {
        Subject subject = subjectRepository.findSubjectById(id);
        if (subject == null) {
            throw new NotFoundException("Subject not found");
        }
        return subject;
    }

    @Override
    public Subject createSubjectPreviewFromTiss(String courseNr, String semester) throws TissClientException {
        LOGGER.trace("createSubjectPreviewFromTiss: courseNr{}, semester{}", courseNr, semester);
        return tissClient.getCourseInfo(courseNr, semester);
    }

    private void saveSubjectDetailDto(SubjectCreateDto subject, Subject s) {
        LOGGER.trace("saveSubjectDetailDto: subjectDetailDto{}, subject:{}", subject, s);
        s.setDescription(subject.getDescription());
        s.setTitle(subject.getTitle());
        s.setType(subject.getType());
        s.setNumber(subject.getNumber());
        s.setSemester(subject.getSemester());
        s.setUrl(subject.getUrl());
        subjectRepository.save(s);
    }
}

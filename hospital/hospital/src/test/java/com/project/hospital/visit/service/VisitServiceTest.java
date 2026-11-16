package com.project.hospital.visit.service;

import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.doctor.entity.Doctor;
import com.project.hospital.doctor.repository.DoctorRepository;
import com.project.hospital.patient.entity.Patient;
import com.project.hospital.patient.repository.PatientRepository;
import com.project.hospital.schedule.entity.DoctorSchedule;
import com.project.hospital.schedule.repository.DoctorScheduleRepository;
import com.project.hospital.user.entity.User;
import com.project.hospital.visit.dto.VisitRegisterRequest;
import com.project.hospital.visit.entity.Visit;
import com.project.hospital.visit.entity.VisitStatus;
import com.project.hospital.visit.mapper.VisitMapper;
import com.project.hospital.visit.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final String EMAIL = "budi@hospital.local";

    @Mock
    private VisitRepository visitRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private DoctorScheduleRepository scheduleRepository;
    @Mock
    private VisitMapper visitMapper;

    @InjectMocks
    private VisitService visitService;

    private Patient patient;
    private Doctor doctor;
    private DoctorSchedule schedule;
    private LocalDate visitDate;

    @BeforeEach
    void setUp() {
        visitDate = LocalDate.now(ZONE).plusDays(1);

        patient = new Patient();
        patient.setId(1L);

        User doctorUser = new User();
        doctorUser.setActive(true);

        doctor = new Doctor();
        doctor.setId(5L);
        doctor.setUser(doctorUser);

        schedule = new DoctorSchedule();
        schedule.setId(10L);
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(visitDate.getDayOfWeek());
        schedule.setQuota(2);
    }

    private void givenPatientAndSchedule() {
        when(patientRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(patient));
        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
    }

    // ---------- pendaftaran ----------

    @Test
    void register_success_givesNextQueueNumberAndWaitingStatus() {
        givenPatientAndSchedule();
        when(doctorRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByPatientIdAndDoctorIdAndVisitDateAndStatusNot(
                1L, 5L, visitDate, VisitStatus.CANCELLED)).thenReturn(false);
        when(visitRepository.countByScheduleIdAndVisitDateAndStatusNot(
                10L, visitDate, VisitStatus.CANCELLED)).thenReturn(1L);
        when(visitRepository.findMaxQueueNumber(5L, visitDate)).thenReturn(5);
        when(visitRepository.save(any(Visit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        visitService.registerForSelf(EMAIL, new VisitRegisterRequest(10L, visitDate, "  Demam  "));

        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitRepository).save(captor.capture());
        Visit saved = captor.getValue();

        assertThat(saved.getQueueNumber()).isEqualTo(6);
        assertThat(saved.getStatus()).isEqualTo(VisitStatus.WAITING);
        assertThat(saved.getComplaint()).isEqualTo("Demam");
        assertThat(saved.getPatient()).isSameAs(patient);
        assertThat(saved.getDoctor()).isSameAs(doctor);
    }

    @Test
    void register_quotaFull_throwsConflict() {
        givenPatientAndSchedule();
        when(doctorRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByPatientIdAndDoctorIdAndVisitDateAndStatusNot(
                1L, 5L, visitDate, VisitStatus.CANCELLED)).thenReturn(false);
        when(visitRepository.countByScheduleIdAndVisitDateAndStatusNot(
                10L, visitDate, VisitStatus.CANCELLED)).thenReturn(2L);

        assertThatThrownBy(() ->
                visitService.registerForSelf(EMAIL, new VisitRegisterRequest(10L, visitDate, null)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Quota for this schedule is full");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void register_alreadyRegisteredWithSameDoctor_throwsConflict() {
        givenPatientAndSchedule();
        when(doctorRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByPatientIdAndDoctorIdAndVisitDateAndStatusNot(
                1L, 5L, visitDate, VisitStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() ->
                visitService.registerForSelf(EMAIL, new VisitRegisterRequest(10L, visitDate, null)))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("already registered");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void register_inactiveDoctor_throwsBusinessRule() {
        givenPatientAndSchedule();
        doctor.getUser().setActive(false);
        when(doctorRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() ->
                visitService.registerForSelf(EMAIL, new VisitRegisterRequest(10L, visitDate, null)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Doctor is not active");

        verify(visitRepository, never()).save(any());
    }

    @Test
    void register_pastDate_throwsBusinessRule() {
        givenPatientAndSchedule();
        LocalDate yesterday = LocalDate.now(ZONE).minusDays(1);
        schedule.setDayOfWeek(yesterday.getDayOfWeek());

        assertThatThrownBy(() ->
                visitService.registerForSelf(EMAIL, new VisitRegisterRequest(10L, yesterday, null)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Visit date must not be in the past");
    }

    @Test
    void register_tooFarInFuture_throwsBusinessRule() {
        givenPatientAndSchedule();
        LocalDate farDate = LocalDate.now(ZONE).plusDays(31);
        schedule.setDayOfWeek(farDate.getDayOfWeek());

        assertThatThrownBy(() ->
                visitService.registerForSelf(EMAIL, new VisitRegisterRequest(10L, farDate, null)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("within 30 days");
    }

    @Test
    void register_dateDoesNotMatchScheduleDay_throwsBusinessRule() {
        givenPatientAndSchedule();
        schedule.setDayOfWeek(visitDate.getDayOfWeek().plus(1));

        assertThatThrownBy(() ->
                visitService.registerForSelf(EMAIL, new VisitRegisterRequest(10L, visitDate, null)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("does not match the schedule day");
    }

    @Test
    void register_scheduleNotFound_throwsNotFound() {
        when(patientRepository.findByUserEmail(EMAIL)).thenReturn(Optional.of(patient));
        when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                visitService.registerForSelf(EMAIL, new VisitRegisterRequest(99L, visitDate, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule not found");
    }

    // ---------- pembatalan ----------

    @Test
    void cancelOwn_waitingVisit_becomesCancelled() {
        Visit visit = new Visit();
        visit.setStatus(VisitStatus.WAITING);
        when(visitRepository.findByIdAndPatientUserEmail(7L, EMAIL)).thenReturn(Optional.of(visit));

        visitService.cancelOwn(EMAIL, 7L);

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
        verify(visitRepository).save(visit);
    }

    @Test
    void cancelOwn_completedVisit_throwsBusinessRule() {
        Visit visit = new Visit();
        visit.setStatus(VisitStatus.COMPLETED);
        when(visitRepository.findByIdAndPatientUserEmail(7L, EMAIL)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> visitService.cancelOwn(EMAIL, 7L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Only waiting visits can be cancelled");

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.COMPLETED);
        verify(visitRepository, never()).save(any());
    }

    @Test
    void cancelOwn_visitOfAnotherPatient_throwsNotFound() {
        when(visitRepository.findByIdAndPatientUserEmail(7L, EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.cancelOwn(EMAIL, 7L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
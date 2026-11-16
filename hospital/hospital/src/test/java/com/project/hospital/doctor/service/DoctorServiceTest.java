package com.project.hospital.doctor.service;

import com.project.hospital.common.exception.ResourceConflictException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.doctor.dto.DoctorCreateRequest;
import com.project.hospital.doctor.dto.DoctorUpdateRequest;
import com.project.hospital.doctor.entity.Doctor;
import com.project.hospital.doctor.mapper.DoctorMapper;
import com.project.hospital.doctor.repository.DoctorRepository;
import com.project.hospital.poli.entity.Poli;
import com.project.hospital.poli.repository.PoliRepository;
import com.project.hospital.user.entity.Role;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PoliRepository poliRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    private Poli poli(Long id, String name) {
        Poli poli = new Poli();
        poli.setId(id);
        poli.setName(name);
        return poli;
    }

    // ---------- create ----------

    @Test
    void create_success_savesUserAndDoctorWithCleanedData() {
        // Arrange: email dan nomor STR sengaja dikirim dengan spasi dan huruf besar
        DoctorCreateRequest request = new DoctorCreateRequest(
                "  Dr.Andi@Hospital.Local ", "Dokter123", "dr. Andi Wijaya",
                " STR-0001 ", "081200000001", 1L);
        Poli umum = poli(1L, "Umum");

        when(userRepository.existsByEmail("dr.andi@hospital.local")).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("STR-0001")).thenReturn(false);
        when(poliRepository.findById(1L)).thenReturn(Optional.of(umum));
        when(passwordEncoder.encode("Dokter123")).thenReturn("hashed");
        when(doctorMapper.toEntity(request)).thenReturn(new Doctor());
        when(doctorRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        doctorService.create(request);

        // Assert: akun user yang dibuat
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("dr.andi@hospital.local");
        assertThat(savedUser.getPassword()).isEqualTo("hashed");
        assertThat(savedUser.getRole()).isEqualTo(Role.DOCTOR);

        // Assert: profil dokter yang disimpan
        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());
        Doctor savedDoctor = doctorCaptor.getValue();

        assertThat(savedDoctor.getLicenseNumber()).isEqualTo("STR-0001");
        assertThat(savedDoctor.getUser()).isSameAs(savedUser);
        assertThat(savedDoctor.getPoli()).isSameAs(umum);
    }

    @Test
    void create_emailAlreadyRegistered_throwsConflict() {
        DoctorCreateRequest request = new DoctorCreateRequest(
                "dr.andi@hospital.local", "Dokter123", "dr. Andi Wijaya",
                "STR-0001", "081200000001", 1L);
        when(userRepository.existsByEmail("dr.andi@hospital.local")).thenReturn(true);

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Email is already registered");

        verify(userRepository, never()).save(any());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_licenseNumberAlreadyExists_throwsConflict() {
        DoctorCreateRequest request = new DoctorCreateRequest(
                "dr.andi@hospital.local", "Dokter123", "dr. Andi Wijaya",
                "STR-0001", "081200000001", 1L);
        when(userRepository.existsByEmail("dr.andi@hospital.local")).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("STR-0001")).thenReturn(true);

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("License number already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_poliNotFound_throwsNotFoundAndCreatesNoAccount() {
        DoctorCreateRequest request = new DoctorCreateRequest(
                "dr.andi@hospital.local", "Dokter123", "dr. Andi Wijaya",
                "STR-0001", "081200000001", 99L);
        when(userRepository.existsByEmail("dr.andi@hospital.local")).thenReturn(false);
        when(doctorRepository.existsByLicenseNumber("STR-0001")).thenReturn(false);
        when(poliRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Poli not found");

        // poli dicek sebelum akun dibuat, jadi tidak ada akun yatim
        verify(userRepository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    void update_licenseUsedByAnotherDoctor_throwsConflict() {
        Doctor doctor = new Doctor();
        doctor.setId(5L);
        when(doctorRepository.findById(5L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.existsByLicenseNumberAndIdNot("STR-0002", 5L)).thenReturn(true);

        DoctorUpdateRequest request = new DoctorUpdateRequest(
                "dr. Andi Wijaya", "STR-0002", "081200000001", 1L);

        assertThatThrownBy(() -> doctorService.update(5L, request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("License number already exists");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void update_success_changesPoliAndTrimsLicense() {
        Doctor doctor = new Doctor();
        doctor.setId(5L);
        Poli gigi = poli(2L, "Gigi");
        when(doctorRepository.findById(5L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.existsByLicenseNumberAndIdNot("STR-0002", 5L)).thenReturn(false);
        when(poliRepository.findById(2L)).thenReturn(Optional.of(gigi));

        doctorService.update(5L, new DoctorUpdateRequest(
                "dr. Andi Wijaya", " STR-0002 ", "081200000001", 2L));

        assertThat(doctor.getLicenseNumber()).isEqualTo("STR-0002");
        assertThat(doctor.getPoli()).isSameAs(gigi);
        verify(doctorRepository).save(doctor);
    }
}
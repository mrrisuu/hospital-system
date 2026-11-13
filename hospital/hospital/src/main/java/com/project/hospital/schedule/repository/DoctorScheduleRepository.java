package com.project.hospital.schedule.repository;

import com.project.hospital.schedule.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    @Query("""
            select count(s) > 0 from DoctorSchedule s
            where s.doctor.id = :doctorId
              and s.dayOfWeek = :day
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
    boolean existsOverlap(@Param("doctorId") Long doctorId,
                          @Param("day") DayOfWeek day,
                          @Param("startTime") LocalTime startTime,
                          @Param("endTime") LocalTime endTime);

    @Query("""
            select count(s) > 0 from DoctorSchedule s
            where s.doctor.id = :doctorId
              and s.dayOfWeek = :day
              and s.startTime < :endTime
              and s.endTime > :startTime
              and s.id <> :excludeId
            """)
    boolean existsOverlapExcluding(@Param("doctorId") Long doctorId,
                                   @Param("day") DayOfWeek day,
                                   @Param("startTime") LocalTime startTime,
                                   @Param("endTime") LocalTime endTime,
                                   @Param("excludeId") Long excludeId);
}
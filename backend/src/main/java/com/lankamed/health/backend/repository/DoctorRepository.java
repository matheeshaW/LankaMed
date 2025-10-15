package com.lankamed.health.backend.repository;

import com.lankamed.health.backend.dto.DoctorProfileDto;
import com.lankamed.health.backend.model.StaffDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<StaffDetails, Long> {
    
    @Query("SELECT s FROM StaffDetails s WHERE s.user.role = 'DOCTOR' " +
           "AND (:name IS NULL OR LOWER(CONCAT(s.user.firstName, ' ', s.user.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:specialization IS NULL OR LOWER(s.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')))")
    List<StaffDetails> findDoctorsByNameAndSpecialization(@Param("name") String name, @Param("specialization") String specialization);
    
    @Query("SELECT s FROM StaffDetails s WHERE s.user.role = 'DOCTOR'")
    List<StaffDetails> findAllDoctors();
    
    @Query(value = """
        SELECT
            u.user_id AS doctorId,
            u.first_name AS firstName,
            u.last_name AS lastName,
            CONCAT(u.first_name, ' ', u.last_name) AS fullName,
            sd.specialization,
            h.name AS hospitalName,
            sc.name AS categoryName,
            COALESCE(AVG(dr.rating), 0) AS averageRating,
            COUNT(dr.review_id) AS reviewCount,
            u.email,
            h.address AS hospitalAddress,
            h.contact_number AS hospitalContact
        FROM
            users u
        JOIN
            staff_details sd ON u.user_id = sd.staff_id
        JOIN
            hospitals h ON sd.hospital_id = h.hospital_id
        JOIN
            service_categories sc ON sd.service_category_id = sc.category_id
        LEFT JOIN
            reviews dr ON u.user_id = dr.doctor_id
        WHERE
            u.role = 'DOCTOR'
            AND (:name IS NULL OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:specialization IS NULL OR LOWER(sd.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')))
        GROUP BY
            u.user_id, u.first_name, u.last_name, sd.specialization, h.name, sc.name, u.email, h.address, h.contact_number
        ORDER BY
            averageRating DESC
        """, nativeQuery = true)
    List<Object[]> findDoctorProfilesWithRatings(@Param("name") String name, @Param("specialization") String specialization);
    
    @Query(value = """
        SELECT
            u.user_id AS doctorId,
            u.first_name AS firstName,
            u.last_name AS lastName,
            CONCAT(u.first_name, ' ', u.last_name) AS fullName,
            sd.specialization,
            h.name AS hospitalName,
            sc.name AS categoryName,
            COALESCE(AVG(dr.rating), 0) AS averageRating,
            COUNT(dr.review_id) AS reviewCount,
            u.email,
            h.address AS hospitalAddress,
            h.contact_number AS hospitalContact
        FROM
            users u
        JOIN
            staff_details sd ON u.user_id = sd.staff_id
        JOIN
            hospitals h ON sd.hospital_id = h.hospital_id
        JOIN
            service_categories sc ON sd.service_category_id = sc.category_id
        LEFT JOIN
            reviews dr ON u.user_id = dr.doctor_id
        WHERE
            u.role = 'DOCTOR'
        GROUP BY
            u.user_id, u.first_name, u.last_name, sd.specialization, h.name, sc.name, u.email, h.address, h.contact_number
        ORDER BY
            averageRating DESC
        """, nativeQuery = true)
    List<Object[]> findAllDoctorProfilesWithRatings();
}

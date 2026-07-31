package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Integer> {

    /** Bir ilin tüm ilçelerini getirir. */
    List<District> findByCityId(Short cityId);
}

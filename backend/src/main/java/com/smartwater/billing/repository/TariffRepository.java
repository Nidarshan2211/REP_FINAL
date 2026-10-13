package com.smartwater.billing.repository;

import com.smartwater.billing.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    List<Tariff> findAllByOrderByMinimumUnitsAsc();

    @Query("SELECT t FROM Tariff t WHERE :units >= t.minimumUnits AND :units <= t.maximumUnits")
    List<Tariff> findApplicableTariff(@Param("units") Double units);
}

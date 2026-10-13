package com.smartwater.billing.repository;

import com.smartwater.billing.entity.Building;
import com.smartwater.billing.entity.BuildingOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByOwner(BuildingOwner owner);
    List<Building> findByOwnerOwnerId(Long ownerId);
}

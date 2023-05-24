package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.WReplenishmentItemEntity;

/**
 *
 * WReplenishmentItemRepository.
 *
 */
@Repository
public interface WReplenishmentItemRepository
extends JpaRepository<WReplenishmentItemEntity, BigInteger> {
    /**
     * テーブルをtruncateする.
     */
    @Modifying
    @Query(value = "TRUNCATE TABLE w_replenishment_item", nativeQuery = true)
    void truncateTable();
}

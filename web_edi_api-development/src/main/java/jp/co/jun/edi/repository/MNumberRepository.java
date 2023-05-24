package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MNumberEntity;

/**
 * 採番マスタのRepository.
 */
public interface MNumberRepository extends JpaRepository<MNumberEntity, BigInteger> {

    /**
     * @param tableName テーブル名
     * @param columnName カラム名
     * @return 採番マスタエンティティ
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM MNumberEntity t"
            + " WHERE t.tableName = :tableName"
            + " AND t.columnName = :columnName"
            + " AND t.deletedAt IS NULL")
    Optional<MNumberEntity> findByTableNameAndColumnName(
            @Param("tableName") String tableName,
            @Param("columnName") String columnName);

}

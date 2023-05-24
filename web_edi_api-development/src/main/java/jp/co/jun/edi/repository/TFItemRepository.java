package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TFItemEntity;

/**
 * フクキタル用品番情報を検索するリポジトリ.
 */
public interface TFItemRepository extends JpaRepository<TFItemEntity, BigInteger>, JpaSpecificationExecutor<TFItemEntity> {
    /**
     * フクキタル品番Idからフクキタル品番情報を取得.
     * @param fItemId フクキタル品番Id
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM TFItemEntity t"
            + " WHERE t.id = :fItemId"
            + " AND t.deletedAt is null")
    Optional<TFItemEntity> findByFItemId(
            @Param("fItemId") BigInteger fItemId);

    /**
     * 品番Idからフクキタル品番情報を取得.
     * @param partNoId 品番Id
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM TFItemEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.deletedAt is null")
    Optional<TFItemEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId);


    /**
     * フクキタル品番IDをキーにフクキタル品番情報テーブルから論理削除する.
     * @param fItemId フクキタル品番ID
     * @return 更新件数
     */
    @Modifying
    @Query(value = " UPDATE TFItemEntity t"
            + " SET t.deletedAt = now()"
            + " WHERE t.id = :fItemId "
            + " AND t.deletedAt IS NULL")
    int updateDeleteAtByFItemId(
            @Param("fItemId") BigInteger fItemId);
}

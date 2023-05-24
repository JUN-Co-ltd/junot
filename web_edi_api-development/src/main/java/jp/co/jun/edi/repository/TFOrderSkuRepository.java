package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TFOrderSkuEntity;

/**
 * フクキタル用発注情報SKUを検索するリポジトリ.
 */
public interface TFOrderSkuRepository extends JpaRepository<TFOrderSkuEntity, BigInteger>, JpaSpecificationExecutor<TFOrderSkuEntity>  {
    /**
     * フクキタル発注Idからフクキタル発注SKU情報を取得.
     * @param fOrderId フクキタル発注Id
     * @param pageable {@link Pageable}}
     * @return フクキタル発注SKU情報
     */
    @Query("SELECT t FROM TFOrderSkuEntity t"
            + " WHERE t.fOrderId = :fOrderId"
            + " AND t.deletedAt is null")
    Page<TFOrderSkuEntity> findByFOrderIdDeletedAtIsNull(
            @Param("fOrderId") BigInteger fOrderId, Pageable pageable);

    /**
     * フクキタル発注ID,除外ID配列からフクキタル発注SKU情報の削除日を設定する.
     * @param fOrderId フクキタル発注ID
     * @param ids 除外ID配列
     * @param deletedAt 削除日
     * @return フクキタル発注SKU情報
     */
    @Modifying
    @Query("UPDATE FROM TFOrderSkuEntity t"
            + " SET t.deletedAt = :deletedAt"
            + " WHERE t.fOrderId = :fOrderId"
            + " AND t.id NOT IN (:ids)"
            + " AND t.deletedAt IS NULL")
    int updateByFOrderIdNotInIds(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("ids") List<BigInteger> ids,
            @Param("deletedAt") Date deletedAt);

    /**
     * フクキタル発注IDをキーにフクキタル発注SKU情報テーブルから論理削除する.
     * @param fOrderId フクキタル発注ID
     * @param deletedAt 削除日
     * @return 更新件数
     */
    @Modifying
    @Query(value = " UPDATE TFOrderSkuEntity t"
            + " SET t.deletedAt = :deletedAt"
            + " WHERE t.fOrderId = :fOrderId "
            + " AND t.deletedAt IS NULL")
    int updateDeleteAtByFOrderId(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("deletedAt") Date deletedAt);

}

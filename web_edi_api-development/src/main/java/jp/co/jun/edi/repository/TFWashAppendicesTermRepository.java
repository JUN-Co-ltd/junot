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

import jp.co.jun.edi.entity.TFWashAppendicesTermEntity;

/**
 * フクキタル用洗濯ネーム付記用語情報を検索するリポジトリ.
 */
public interface TFWashAppendicesTermRepository
        extends JpaRepository<TFWashAppendicesTermEntity, BigInteger>, JpaSpecificationExecutor<TFWashAppendicesTermEntity> {
    /**
     * フクキタル品番ID,除外ID配列からフクキタル用洗濯ネーム付記用語情報の削除日を設定する.
     * @param fItemId フクキタル発注ID
     * @param ids 除外ID配列
     * @param deletedAt 削除日
     * @return フクキタル用洗濯ネーム付記用語情報の更新した数
     */
    @Modifying
    @Query("UPDATE FROM TFWashAppendicesTermEntity t"
            + " SET t.deletedAt = :deletedAt"
            + " WHERE t.fItemId = :fItemId"
            + " AND t.id NOT IN (:ids)"
            + " AND t.deletedAt IS NULL")
    int updateDeleteByFItemIdNotInIds(
            @Param("fItemId") BigInteger fItemId,
            @Param("ids") List<BigInteger> ids,
            @Param("deletedAt") Date deletedAt);

    /**
     * フクキタル品番IDをキーにフクキタル用洗濯ネーム付記用語情報テーブルから論理削除する.
     * @param fItemId フクキタル品番ID
     * @param deletedAt 削除日時
     * @return 更新件数
     */
    @Modifying
    @Query(value = " UPDATE TFWashAppendicesTermEntity t"
            + " SET t.deletedAt = :deletedAt"
            + " WHERE t.fItemId = :fItemId "
            + " AND t.deletedAt IS NULL")
    int updateDeleteAtByFItemId(
            @Param("fItemId") BigInteger fItemId,
            @Param("deletedAt") Date deletedAt);

    /**
     * フクキタル品番IDをキーにフクキタル用洗濯ネーム付記用語情報を検索.
     * @param fItemId フクキタル品番ID
     * @param pageable {@link Pageable} instance
     * @return フクキタル用洗濯ネーム付記用語情報のリスト
     */
    @Query("SELECT t FROM TFWashAppendicesTermEntity t"
            + " WHERE t.deletedAt IS NULL"
            + " AND t.fItemId = :fItemId")
    Page<TFWashAppendicesTermEntity> findByFItemId(
            @Param("fItemId") BigInteger fItemId, Pageable pageable);
}

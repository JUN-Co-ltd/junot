package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TFileInfoEntity;

/**
 *
 * TTanzakuImageFileRepository.
 *
 */
@Repository
public interface TFileInfoRepository extends JpaRepository<TFileInfoEntity, BigInteger> {

    /**
     * 品番IDからファイル情報を削除する.
     * @param partNoId partNoId
     * @return int
     */
    @Modifying
    @Query("DELETE FROM TFileInfoEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.deletedAt is null")
    int deleteByPartNoId(
            @Param("partNoId") BigInteger partNoId);

    /**
     * 品番IDから タンザクファイル情報 を検索する.
     *
     * @param partNoId
     *            品番ID
     * @param pageable pageable
     * @return タンザク画像情報
     */
    @Query("SELECT t FROM TFileInfoEntity t"
            + " WHERE t.id.partNoId = :partNoId "
            + " AND t.deletedAt is null")
    Page<TFileInfoEntity> findByPartNoId(@Param("partNoId") BigInteger partNoId, Pageable pageable);

    /**
     * ファイルIDに紐づくファイル情報に削除日を設定する.
     * @param fileNoId fileNoId
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_file_info t"
            + " SET t.deleted_at = now(),"
            + " t.updated_user_id = ?2 ,"
            + " t.updated_at = now()"
            + " WHERE t.file_no_id = ?1", nativeQuery = true)
    int updateDeleteAtByFileNoId(
            @Param("fileNoId") BigInteger fileNoId,
            @Param("updatedUserId") BigInteger updatedUserId);

}

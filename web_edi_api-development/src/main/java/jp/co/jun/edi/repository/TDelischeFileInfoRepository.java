package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDelischeFileInfoEntity;

/**
 *
 * TDelischeFileInfoRepository.
 *
 */
@Repository
public interface TDelischeFileInfoRepository extends JpaRepository<TDelischeFileInfoEntity, BigInteger> {
    /**
     * IDをキーにデリスケファイル情報を検索する.
     *
     * @param id id
     * @return デリスケファイル情報
     */
    @Query("SELECT t FROM TDelischeFileInfoEntity t"
            + " WHERE t.id = :id"
            + " AND t.deletedAt is null")
    Optional<TDelischeFileInfoEntity> findById(@Param("id") BigInteger id);

    /**
     * 登録ユーザーIDをキーにデリスケファイル情報 を検索する.
     *
     * @param userId ユーザーID
     * @param pageable pageable
     * @return デリスケファイル情報
     */
    @Query("SELECT t FROM TDelischeFileInfoEntity t"
            + " WHERE t.createdUserId = :userId"
            + " AND t.deletedAt is null")
    Page<TDelischeFileInfoEntity> findByCreateUserId(
            @Param("userId") BigInteger userId,
            Pageable pageable);

    /**
     * ファイルIDとステータスを更新する.
     * @param fileNoId ファイルID
     * @param status ステータス
     * @param updatedUserId 更新ユーザーID
     * @param delischeFileInfoId デリスケファイル情報ID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_delische_file_info t"
            + " SET t.file_no_id = :fileNoId,"
            + " t.status = :status,"
            + " t.updated_at = now(),"
            + " t.updated_user_id = :updatedUserId"
            + " WHERE t.id = :delischeFileInfoId"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateFileNoIdAndStatus(
            @Param("fileNoId") BigInteger fileNoId,
            @Param("status") int status,
            @Param("updatedUserId") BigInteger updatedUserId,
            @Param("delischeFileInfoId") BigInteger delischeFileInfoId);
}

package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TFileEntity;

/**
 *
 */
public interface TFileRepository extends JpaRepository<TFileEntity, BigInteger> {

    /**
     * @param fileId ファイルID
     * @return ファイル情報
     */
    @Query("SELECT t FROM TFileEntity t"
            + " WHERE t.id = :fileId"
            + " AND t.deletedAt is null")
    Optional<TFileEntity> findByFileId(
            @Param("fileId") BigInteger fileId);

    /**
     * IDをキーにファイル情報テーブルから論理削除する.
     * @param id ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_file t"
            + " SET t.deleted_at = now() ,"
            + " t.updated_user_id = ?2 ,"
            + " t.updated_at = now()"
            + " WHERE t.id = ?1", nativeQuery = true)
    int updateDeleteAtById(
            @Param("id") BigInteger id,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * ファイル情報検索.
     * 複数のファイルIDの情報をまとめて取得する
     *
     * @param fileIds ファイルIDの配列
     * @return ファイル情報のリスト
     */
    @Query("SELECT t FROM TFileEntity t"
            + " WHERE t.id IN(:ids)"
            + " AND t.deletedAt IS NULL")
    List<TFileEntity> findByFileIds(
            @Param("ids") Set<BigInteger> fileIds);
}

package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.TFLinkingFileEntity;

/**
 * フクキタル用連携ファイル情報を検索するリポジトリ.
 */
public interface TFLinkingFileRepository extends JpaRepository<TFLinkingFileEntity, BigInteger> {

    /**
     * @param fileId ファイルID
     * @return ファイル情報
     */
    @Query("SELECT t FROM TFLinkingFileEntity t"
            + " WHERE t.id = :fileId"
            + " AND t.deletedAt is null")
    Optional<TFLinkingFileEntity> findByFileId(
            @Param("fileId") BigInteger fileId);

    /**
     * IDをキーに連携ファイル情報テーブルから論理削除する.
     * @param id ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_f_linking_file t"
            + " SET t.deleted_at = now() ,"
            + " t.updated_user_id = :updatedUserId ,"
            + " t.updated_at = now()"
            + " WHERE t.id = :id", nativeQuery = true)
    int updateDeleteAtById(
            @Param("id") BigInteger id,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * 連携ファイル情報検索.
     * 複数のファイルIDの情報をまとめて取得する
     *
     * @param fileIds ファイルIDの配列
     * @return ファイル情報のリスト
     */
    @Query("SELECT t FROM TFLinkingFileEntity t"
            + " WHERE t.id IN(:ids)"
            + " AND t.deletedAt IS NULL")
    List<TFLinkingFileEntity> findByFileIds(
            @Param("ids") Set<BigInteger> fileIds);
}

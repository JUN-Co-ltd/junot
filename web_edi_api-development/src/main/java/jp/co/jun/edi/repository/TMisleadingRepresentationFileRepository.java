package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TMisleadingRepresentationFileEntity;

/**
 *
 * TMisleadingRepresentationFileRepository.
 *
 */
@Repository
public interface TMisleadingRepresentationFileRepository extends JpaRepository<TMisleadingRepresentationFileEntity, BigInteger> {

    /**
     * ファイルIDと品番IDに紐づく優良誤認ファイル情報に削除日を設定する.
     * @param fileNoId ファイルID
     * @param partNoId 品番ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_misleading_representation_file t"
            + " SET t.deleted_at = now(),"
            + " t.updated_user_id = ?3 ,"
            + " t.updated_at = now()"
            + " WHERE t.file_no_id = ?1"
            + " AND t.part_no_id = ?2", nativeQuery = true)
    int updateDeleteAtByFileNoIdAndPartNoId(
            @Param("fileNoId") BigInteger fileNoId,
            @Param("partNoId") BigInteger partNoId,
            @Param("updatedUserId") BigInteger updatedUserId);

    /**
     * ファイルIDに紐づく優良誤認検査ファイル情報の件数を取得する.
     * @param fileNoId 品番ID
     * @return 件数
     */
    @Query("SELECT COUNT(*) FROM TMisleadingRepresentationFileEntity t"
            + " WHERE t.fileNoId = :fileNoId"
            + " AND t.deletedAt is null")
    int cntByFileNoId(
            @Param("fileNoId") BigInteger fileNoId);
}

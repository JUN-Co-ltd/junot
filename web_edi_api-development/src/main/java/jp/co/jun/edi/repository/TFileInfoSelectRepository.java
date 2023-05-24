package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TFileInfoSelectEntity;

/**
 *
 * TTanzakuImageFileRepository.
 *
 */
@Repository
public interface TFileInfoSelectRepository extends JpaRepository<TFileInfoSelectEntity, BigInteger> {

    /**
     * 品番IDから ファイル情報 を検索する.
     *
     * @param partNoId
     *            品番ID
     * @param pageable pageable
     * @return ファイル情報
     */
    @Query(value = "SELECT t.id"
            + "           ,t.part_no_id "
            + "           ,t.file_no_id "
            + "           ,t.file_category "
            + "           ,t.created_at "
            + "           ,t.created_user_id "
            + "           ,t.updated_at "
            + "           ,t.updated_user_id "
            + "           ,t.deleted_at "
            + "           , m.file_name "
            + " FROM t_file_info t,t_file m "
            + " WHERE t.part_no_id = ?1 "
            + " AND t.file_no_id = m.id "
            + " AND t.deleted_at is null", nativeQuery = true)
    Page<TFileInfoSelectEntity> findByPartNoId(@Param("partNoId") BigInteger partNoId, Pageable pageable);

    /**
     * 品番IDからファイル分類を指定してファイル情報を検索する.
     *
     * @param partNoId
     *            品番ID
     * @param fileCategory ファイル分類
     * @param pageable pageable
     * @return ファイル情報
     */
    @Query(value = "SELECT t.id"
            + "           ,t.part_no_id "
            + "           ,t.file_no_id "
            + "           ,t.file_category "
            + "           ,t.created_at "
            + "           ,t.created_user_id "
            + "           ,t.updated_at "
            + "           ,t.updated_user_id "
            + "           ,t.deleted_at "
            + "           , m.file_name "
            + " FROM t_file_info t,t_file m "
            + " WHERE t.part_no_id = ?1 "
            + " AND t.file_no_id = m.id "
            + " AND t.file_category = ?2 "
            + " AND t.deleted_at is null", nativeQuery = true)
    Page<TFileInfoSelectEntity> findByPartNoIdAndFileCategory(
                @Param("partNoId") BigInteger partNoId,
                @Param("fileCategory") int fileCategory,
                Pageable pageable
            );

}

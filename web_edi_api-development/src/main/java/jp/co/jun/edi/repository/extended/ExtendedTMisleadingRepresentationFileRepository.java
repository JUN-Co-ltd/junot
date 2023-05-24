package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTMisleadingRepresentationFileEntity;

/**
 *
 * ExtendedTMisleadingRepresentationFileRepository.
 *
 */
@Repository
public interface ExtendedTMisleadingRepresentationFileRepository extends JpaRepository<ExtendedTMisleadingRepresentationFileEntity, BigInteger> {

    /**
     * 品番IDから 優良誤認検査ファイル情報+ファイル情報 を検索する.
     *
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return 拡張優良誤認検査ファイル情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "   ,file.content_type as content_type"
            + "   ,file.file_name as file_name"
            + "   ,file.memo as memo"
            + " FROM t_misleading_representation_file t"
            + " LEFT OUTER JOIN t_file file"
            + " ON t.file_no_id = file.id "
            + " WHERE t.part_no_id = :partNoId "
            + " AND t.deleted_at is null "
            + " AND file.deleted_at is null ", nativeQuery = true)
    Page<ExtendedTMisleadingRepresentationFileEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId, Pageable pageable);


}

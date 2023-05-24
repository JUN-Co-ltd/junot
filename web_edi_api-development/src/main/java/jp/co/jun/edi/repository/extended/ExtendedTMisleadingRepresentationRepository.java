package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTMisleadingRepresentationEntity;

/**
 *
 * ExtendedTMisleadingRepresentationRepository.
 *
 */
@Repository
public interface ExtendedTMisleadingRepresentationRepository extends JpaRepository<ExtendedTMisleadingRepresentationEntity, BigInteger> {

    /**
     * 品番IDから 優良誤認情報を検索する.
     * メンテ区分＝"3"（削除）分も表示する.
     *
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return 拡張優良誤認情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "   ,mc2.item2 as approval_user_name"
            + " FROM t_misleading_representation t"
            + " LEFT JOIN m_codmst mc2 "
            + "        ON mc2.tblid = '22' "
            + "       AND mc2.deleted_at is null "
            + "       AND t.approval_user_account_name = mc2.code1 "
            + " WHERE t.part_no_id = :partNoId "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Page<ExtendedTMisleadingRepresentationEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId, Pageable pageable);


}

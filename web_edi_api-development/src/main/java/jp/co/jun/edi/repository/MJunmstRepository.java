package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MJunmstEntity;

/**
 * 発注生産システムの配分順マスタのリポジトリ.
 */
public interface MJunmstRepository extends JpaRepository<MJunmstEntity, BigInteger> {

    /**
     * ブランド、課コードリストで検索し、
     * 課ごとに配分順が一番少ない店舗コード1件ずつ取得.
     *
     * @param brandCode ブランドコード
     * @param divisionCodes 課コードリスト
     * @return サイズマスタのリスト
     */
    @Query(value = "SELECT "
            + "       j1.* "
            + "     FROM m_junmst j1 "
            + "     INNER JOIN ( "
            + "       SELECT "
            + "         hka "
            + "         , MIN(hjun) hjun "
            + "       FROM "
            + "         m_junmst "
            + "       WHERE "
            + "         brand = :brandCode "
            + "         AND hka IN (:divisionCodes) "
            + "         AND mntflg IN ('1', '2', '') "
            + "         AND deleted_at IS NULL "
            + "         GROUP BY "
            + "           hka "
            + "       ) j2 "
            + "         ON j1.hka = j2.hka "
            + "         AND j1.hjun = j2.hjun "
            + "     WHERE "
            + "       j1.brand = :brandCode "
            + "       AND j1.mntflg IN ('1', '2', '') "
            + "       AND j1.deleted_at IS NULL", nativeQuery = true)
    List<MJunmstEntity> findMinHjunByBrandAndDivisionCodeListGroupByHka(
            @Param("brandCode") String brandCode,
            @Param("divisionCodes") List<String> divisionCodes);
}

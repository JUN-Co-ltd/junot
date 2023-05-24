package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MSizmstEntity;

/**
 * 発注生産システムのサイズマスタのリポジトリ.
 */
public interface MSizmstRepository extends JpaRepository<MSizmstEntity, BigInteger> {
    /**
     * サイズマスタ検索.
     *
     * @param hscd 品種
     * @param pageable ページ情報
     * @return サイズマスタのリスト
     */
    @Query("SELECT t FROM MSizmstEntity t"
            + " WHERE t.hscd = :hscd"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " ORDER BY CAST(t.jun AS int)")
    Page<MSizmstEntity> findByHscdOrderByJun(
            @Param("hscd") String hscd,
            Pageable pageable);
    //PRD_0137 #10669 add start
    /**
     * サイズマスタ検索.
     *
     * @param hscd 品種
     * @return サイズマスタのリスト
     */
    @Query("SELECT t FROM MSizmstEntity t"
            + " WHERE t.hscd = :hscd"
            + " AND t.deletedAt IS NULL"
            + " ORDER BY CAST(t.jun AS int)")
    Page<MSizmstEntity> findByHscdOrderByJunNoMntflg(
            @Param("hscd") String hscd,
            Pageable pageable);
    //PRD_0137 #10669 add end

    //PRD_0154 #10699 add start
    /**
     * サイズマスタ検索.
     *
     * @param hscd 品種
     * @param szkg サイズ
     * @return サイズマスタ
     */

    @Query(value = " SELECT s.id "
            + "         ,   s.hscd "
            + "         ,   s.szkg "
            + "         ,   s.jun "
            + "         ,   s.mntflg "
            + "         ,   s.deleted_at "
            + "     FROM junot.m_sizmst s "
            + "     WHERE s.hscd = :hscd"
            + "     AND s.szkg = :szkg", nativeQuery = true)
    List<MSizmstEntity> findByHscdAndSzkg(
            @Param("hscd") String hscd,
            @Param("szkg") String szkg);
  //PRD_0154 #10669 add end
}

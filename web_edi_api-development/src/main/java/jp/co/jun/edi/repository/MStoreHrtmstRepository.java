package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MStoreHrtmstEntity;

/**
 * 店舗別配分率マスタのRepository.
 */
@Repository
public interface MStoreHrtmstRepository extends JpaRepository<MStoreHrtmstEntity, BigInteger> {
    /**
     * ブランド・アイテム・シーズンをキーに店舗別配分率マスタを抽出する.
     * アイテムとシーズンが空白で登録されているケースがあるため、それぞれ空白の検索条件も追加する。
     *
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @param season シーズン
     * @param pageable pageable
     * @return 店舗別配分率マスタエンティティのリスト
     */
    @Query("SELECT t FROM MStoreHrtmstEntity t"
            + " WHERE t.deletedAt IS NULL"
            + "   AND t.mntflg IN ('1', '2', '')"
            + "   AND t.brandCode = :brandCode"
            + "   AND ("
            + "       (t.itemCode = :itemCode AND t.season = :season)"
            + "    OR (t.itemCode = :itemCode AND t.season = '')"
            + "    OR (t.itemCode = '' AND t.season = '')"
            + ")")
    Page<MStoreHrtmstEntity> findByBrandCodeAndItemCodeAndSeason(
            @Param("brandCode") String brandCode,
            @Param("itemCode") String itemCode,
            @Param("season") String season,
            Pageable pageable);

    // #PRD_0138 #10680 add JFE start
    /**
     * 登録日＝日計日である店舗別配分率マスタを抽出し、件数を返す.
     *
     * @param nitymd 日計日
     * @return 店舗別配分率マスタ
     */
    @Query(value = "SELECT COUNT(*) FROM m_store_hrtmst t"
            + " WHERE t.souymd = :nitymd"
            + " AND t.deleted_at IS NULL",nativeQuery = true)
    int countByNitymd(
    		@Param("nitymd") String nitymd);
	// #PRD_0138 #10680 add JFE end
}

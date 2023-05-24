package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.MSirmstEntity;

/**
 * 発注生産システムの仕入先マスタのリポジトリ.
 */
//PRD_0141 #10656 JFE upd start 既存バグ？
//public interface MSirmstRepository extends JpaRepository<MCodmstEntity, BigInteger> {
public interface MSirmstRepository extends JpaRepository<MSirmstEntity, BigInteger> {
//PRD_0141 #10656 JFE upd end
    /**
     * 区分別コード値検索.
     *
     * // param sirkbn 仕入先区分
     * @param sire 仕入先
     * @param pageable ページ情報
     * @return 仕入先マスタのリスト
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE "
            // 生地縫製の機能実装時にコメント外す
            // + "t.sirkbn = :sirkbn"
            // + " AND "
            + "t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.sire LIKE %:sire%"
            + " ORDER BY CAST(t.sire AS int)")
    Page<MSirmstEntity> findBySirkbnAndSireLikeOrderBySire(
            //  @Param("sirkbn") String sirkbn,
            @Param("sire") String sire,
            Pageable pageable);

    /**
     * 区分別名称値検索.
     *
     * // param sirkbn 仕入先区分
     * @param name 仕入先正式名称
     * @param pageable ページ情報
     * @return 仕入先マスタのリスト
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE "
            // 生地縫製の機能実装時にコメント外す
            // + "t.sirkbn = :sirkbn"
            // + " AND "
            + "t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.name LIKE %:name%"
            + " ORDER BY CAST(t.sire AS int)")
    Page<MSirmstEntity> findBySirkbnAndNameLikeOrderBySire(
            // @Param("sirkbn") String sirkbn,
            @Param("name") String name,
            Pageable pageable);

    /**
     * 区分別名称・コードOR検索.
     *
     * // param sirkbn 仕入先区分
     * @param sire 検索文字
     * @param name 仕入先正式名称
     * @param pageable ページ情報
     * @return 仕入先マスタのリスト
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE "
            // 生地縫製の機能実装時にコメント外す
            // + "t.sirkbn = :sirkbn"
            // + " AND "
            + "t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND (t.sire LIKE %:sire%"
            + " OR t.name LIKE %:name%)"
            + " ORDER BY CAST(t.sire AS int)")
    Page<MSirmstEntity> findBySirkbnAndSireLikeOrNameLikeOrderBySire(
            // @Param("sirkbn") String sirkbn,
            @Param("sire") String sire,
            @Param("name") String name,
            Pageable pageable);

    /**
     * 区分検索.
     *
     * // param sirkbn 仕入先区分
     * @param pageable ページ情報
     * @return 仕入先マスタのリスト
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE "
            // 生地縫製の機能実装時にコメント外す
            // + "t.sirkbn = :sirkbn"
            // + " AND "
            + "t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " ORDER BY CAST(t.sire AS int)")
    Page<MSirmstEntity> findBySirkbnOrderBySire(
            // @Param("sirkbn") String sirkbn,
            Pageable pageable);

    /**
     * 区分検索.
     *
     * // param sirkbn 仕入先区分
     * @param sires 仕入先のリスト
     * @param pageable ページ情報
     * @return 仕入先マスタのリスト
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE "
            // 生地縫製の機能実装時にコメント外す
            // + "t.sirkbn = :sirkbn"
            // + " AND "
            + "t.sire IN(:sires)"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL")
    Page<MSirmstEntity> findBySirkbnAndSirs(
            // @Param("sirkbn") String sirkbn,
            @Param("sires") Set<String> sires,
            Pageable pageable);

    /**
     * 仕入先コードから仕入先マスタ情報を取得する.
     *
     * @param sire 仕入先コード
     * @return yugaikbn 仕入先マスタ情報
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL")
    Optional<MSirmstEntity> findBySire(
            @Param("sire") String sire);
    
    // PRD_0112 #7710 JFE add start
    /**
     * 品番情報のメーカーコードから仕入マスタ情報を取得する
     * @param sire 仕入先コード
     * @return  仕入先マスタ情報
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.hseihin = '1'"
            + " AND t.deletedAt is null")
    Optional<MSirmstEntity>findByOrderMakerId(
            @Param("sire") String sire);
    // PRD_0112 #7710 JFE add end

    // PRD_0141 #10656 JFE add start
    /**
     * 仕入先コードで取得する.
     * @param sire 仕入先コード
     * @return 仕入先マスタ情報
     */
    @Query("SELECT t FROM MSirmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.deletedAt IS NULL")
    Optional<MSirmstEntity> findBySireCodeIgnoreSystemManaged(
            @Param("sire") String sire);
    // PRD_0141 #10656 JFE add end
}

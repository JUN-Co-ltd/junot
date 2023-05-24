package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MKojmstEntity;

/**
 * 発注生産システムの工場マスタのリポジトリ.
 */
public interface MKojmstRepository extends JpaRepository<MKojmstEntity, BigInteger> {
    /**
     * 仕入先、仕入先区分検索.
     *
     * @param sire 仕入先
     * @param sirkbn 仕入先区分
     * @param pageable ページ情報
     * @return 工場マスタのリスト
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.sirkbn = :sirkbn"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.kojcd > ''"
            + " ORDER BY CAST(t.kojcd AS int)")
    Page<MKojmstEntity> findBySireAndSirkbnOrderByKojcd(
            @Param("sire") String sire,
            @Param("sirkbn") String sirkbn,
            Pageable pageable);

    /**
     * 区分別コード値検索.
     *
     * @param sire 仕入先
     * @param sirkbn 仕入先区分
     * @param kojcd 工場コード（スポットコード）
     * @param pageable ページ情報
     * @return 工場マスタのリスト
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.sirkbn = :sirkbn"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.kojcd LIKE %:kojcd%"
            + " ORDER BY CAST(t.kojcd AS int)")
    Page<MKojmstEntity> findBySireAndSirkbnAndKojcdLikeOrderByKojcd(
            @Param("sire") String sire,
            @Param("sirkbn") String sirkbn,
            @Param("kojcd") String kojcd,
            Pageable pageable);

    /**
     * 区分別名称値検索.
     *
     * @param sire 仕入先
     * @param sirkbn 仕入先区分
     * @param name 仕入先正式名称字
     * @param pageable ページ情報
     * @return 工場マスタのリスト
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.sirkbn = :sirkbn"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.name LIKE %:name%"
            + " ORDER BY CAST(t.kojcd AS int)")
    Page<MKojmstEntity> findBySireAndSirkbnAndNameLikeOrderByKojcd(
            @Param("sire") String sire,
            @Param("sirkbn") String sirkbn,
            @Param("name") String name,
            Pageable pageable);

    /**
     * 区分別名称・コードOR検索.
     *
     * @param sire 仕入先
     * @param sirkbn 仕入先区分
     * @param kojcd 工場コード（スポットコード）
     * @param name 仕入先正式名称字
     * @param pageable ページ情報
     * @return 工場マスタのリスト
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.sirkbn = :sirkbn"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND (t.kojcd LIKE %:kojcd%"
            + " OR t.name LIKE %:name%)"
            + " ORDER BY CAST(t.kojcd AS int)")
    Page<MKojmstEntity> findBySireAndSirkbnAndKojcdLikeOrNameLikeOrderByKojcd(
            @Param("sire") String sire,
            @Param("sirkbn") String sirkbn,
            @Param("kojcd") String kojcd,
            @Param("name") String name,
            Pageable pageable);


    /**
     * 仕入先・工場コード（スポットコード）検索.
     * ※注意(内部課題No113対応完了までの仮仕様）※
     * 「m_kojmst.kojcd」と「t_item.mdf_maker_factory_code」を突合する場合は、
     * 「t_item.mdf_maker_factory_code」がNULLの場合、空文字に変換する
     *
     * @param sire 仕入先
     * @param kojcd 工場コード（スポットコード）
     * @return 工場マスタのリスト
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.kojcd = :kojcd"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.nsofkbn IN ('3','8')"
            + " AND t.deletedAt IS NULL")
    Optional<MKojmstEntity> findBySireKojCd(
            @Param("sire") String sire,
            @Param("kojcd") String kojcd);

    /**
     * 仕入先・工場コード（スポットコード）検索.
     * ※注意(内部課題No113対応完了までの仮仕様）※
     * 「m_kojmst.kojcd」と「t_item.mdf_maker_factory_code」を突合する場合は、
     * 「t_item.mdf_maker_factory_code」がNULLの場合、空文字に変換する
     *
     * @param sire 仕入先
     * @param kojcd 工場コード（スポットコード）
     * @return 工場マスタのリスト
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.kojcd = :kojcd"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.hsofkbn IN ('3','8')"
            + " AND t.deletedAt IS NULL")
    Optional<MKojmstEntity> findBySireKojCdHsofkbn(
            @Param("sire") String sire,
            @Param("kojcd") String kojcd);
    //PRD_0134 #10654 add JEF start
    /**
     * 仕入先・工場コード（スポットコード）検索.
     * @param sire 仕入先
     * @return 工場マスタのリスト
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.ysofkbn IN ('3','8')"
            + " AND t.deletedAt IS NULL")
    Optional<MKojmstEntity> findBySireysofkbn(
            @Param("sire") String sire);
    //PRD_0134 #10654 add JEF end

    /**
     * 仕入先、仕入先区分、工場コード検索.
     *
     * @param sire 仕入先
     * @param sirkbn 仕入先区分
     * @param kojcd 工場コード（スポットコード）
     * @return 工場マスタ
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.sirkbn = :sirkbn"
            + " AND t.kojcd = :kojcd"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL")
    Optional<MKojmstEntity> findBySireAndSirkbnAndKojcd(
            @Param("sire") String sire,
            @Param("sirkbn") String sirkbn,
            @Param("kojcd") String kojcd);

    // PRD_0141 #10656 JFE add start
    /**
     * 仕入先コードと工場コードで取得する.
     * @param sire 仕入先コード
     * @param kojcd 工場コード
     * @return 工場マスタ
     */
    @Query("SELECT t FROM MKojmstEntity t"
            + " WHERE t.sire = :sire"
            + " AND t.kojcd = :kojcd"
            + " AND t.deletedAt IS NULL")
    Optional<MKojmstEntity> findBySireCodeAndKojCodeIgnoreSystemManaged(
            @Param("sire") String sire,
            @Param("kojcd") String kojcd);
    // PRD_0141 #10656 JFE add end
}

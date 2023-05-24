package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MCodmstEntity;

/**
 * 発注生産システムのコードマスタのリポジトリ.
 */
public interface MCodmstRepository extends JpaRepository<MCodmstEntity, BigInteger>, JpaSpecificationExecutor<MCodmstEntity> {

    /**
     * テーブルID指定コードマスタ情報取得.
     *
     * @param tblid テーブル区分
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " ORDER BY CAST(t.code1 AS int)")
    Page<MCodmstEntity> findByTblIdOrderByCode1(
            @Param("tblid") String tblid,
            Pageable pageable);

    /**
     * テーブルIDとキーコード１をキーにコードマスタ情報を取得する.
     *
     * @param tblId テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.code1 = :code1"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL")
    Page<MCodmstEntity> findByTblIdAndCode1(
            @Param("tblid") String tblId,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND TRIM(t.code1) = :code1"
            + " ORDER BY t.id")
    Page<MCodmstEntity> findByTblidAndCode1OrderById(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND TRIM(t.code1) = :code1"
            + " ORDER BY t.code1,t.code2")
    Page<MCodmstEntity> findByTblidAndCode1OrderByCode1Code2(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND TRIM(t.code1) = :code1"
            + " ORDER BY CAST(t.code2 AS int)")
    Page<MCodmstEntity> findByTblidAndCode1OrderByCode2(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1List キーコード１リスト
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.code1 IN :code1List"
            + " AND t.deletedAt IS NULL")
    Page<MCodmstEntity> findByTblidAndCode1List(
            @Param("tblid") String tblid,
            @Param("code1List") List<String> code1List,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1List キーコード１リスト
     * @return 取得件数
     */
    @Query("SELECT COUNT(*) FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.code1 IN :code1List"
            + " AND t.deletedAt IS NULL")
    int countByTblidAndCode1List(
            @Param("tblid") String tblid,
            @Param("code1List") List<String> code1List);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.code1 = :code1"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " ORDER BY CAST(t.item3 AS int)")
    Page<MCodmstEntity> findByTblidAndCode1OrderByItem3(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param item2 内容２
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND (t.code1 LIKE %:code1%"
            + " OR t.item2 LIKE %:item2%)"
            + " ORDER BY CAST(t.code1 AS int)")
    Page<MCodmstEntity> findByTblidAndCode1LikeOrItem2LikeOrderByCode1(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            @Param("item2") String item2,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.code1 LIKE %:code1%"
            + " ORDER BY CAST(t.code1 AS int)")
    Page<MCodmstEntity> findByTblidAndCode1LikeOrderByCode1(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param code2 キーコード２
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND TRIM(t.code1) = :code1"
            + " AND TRIM(t.code2) = :code2"
            + " ORDER BY t.id")
    Page<MCodmstEntity> findByTblidAndCode1AndCode2OrderById(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            @Param("code2") String code2,
            Pageable pageable);

    /**
     * 丸井品番取得用SQL.
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query(value = "SELECT t.id "
            + "         ,t.tblid "
            + "         ,t.code1 "
            + "         ,t.code2 "
            + "         ,t.code3 "
            + "         ,'' as code5 "
            + "         ,t.item1 "
            + "         ,'' as item2 "
            + "         ,'' as item3 "
            + "         ,'' as item4 "
            + "         ,'' as item5 "
            + "         ,'' as item6 "
            + "         ,'' as item7 "
            + "         ,'' as item10 "
            + "         ,item30 "
            + "         ,'' as mntflg "
            + "         ,null as deleted_at "
            + " FROM m_codmst t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deleted_at IS NULL"
            + " AND t.code1 = :code1"
            + " ORDER BY t.code3,code2", nativeQuery = true)
    Page<MCodmstEntity> findByMaruiItem(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * 丸井品番件数取得用SQL.
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @return 取得した丸井品番の件数
     */
    @Query(value = "SELECT COUNT(t.id) FROM m_codmst t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deleted_at IS NULL"
            + " AND t.code1 = :code1", nativeQuery = true)
    int countMaruiItemByTblidAndCode1(
            @Param("tblid") String tblid,
            @Param("code1") String code1);

    /**
     * ※丸井品番存在チェック用SQL.
     * ブランドコードと指定した丸井品番に紐づく丸井品番の数を取得.
     * @param tblid テーブル区分
     * @param code1 キーコード1(ブランドコード)
     * @param code3 キーコード3(丸井品番)
     * @return 取得件数
     */
    @Query("SELECT COUNT(t.id) FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.code1 = :code1"
            + " AND t.code3 = :code3"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL")
    int countByTblidAndCode1AndCode3(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            @Param("code3") String code3);

    /**
     * @param tblid テーブル区分
     * @param item2 内容１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.item2 LIKE %:item2%"
            + " ORDER BY CAST(t.code1 AS int)")
    Page<MCodmstEntity> findByTblidAndItem2LikeOrderByCode1(
            @Param("tblid") String tblid,
            @Param("item2") String item2,
            Pageable pageable);

    /**
     * タイプ１～２マスタ検索.
     * テーブルID指定および、許可タイプの部分一致検索を行う
     *
     * @param tblid テーブル区分
     * @param item2 内容２
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND (t.item2 = '' "
            + " OR t.item2 LIKE %:item2%)"
            + " ORDER BY CAST(t.code1 AS int)")
    Page<MCodmstEntity> findByTblidAndItem2IsEmptyOrItem2LikeOrderByCode1(
            @Param("tblid") String tblid,
            @Param("item2") String item2,
            Pageable pageable);

    /**
     * タイプ３マスタ検索.
     * テーブルID指定および、許可タイプの指定桁数部分一致検索を行う
     *
     * @param tblid テーブル区分
     * @param item2 内容２
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND (t.item2 = '' "
            + " OR INSTR(t.item2, :item2) % 2 != 0)"
            + " ORDER BY CAST(t.code1 AS int)")
    Page<MCodmstEntity> findByTblidAndItem2IsEmptyOrItem2OrderByCode1(
            @Param("tblid") String tblid,
            @Param("item2") String item2,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param item1 内容１
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND t.code1 = :code1"
            + " AND t.item1 = :item1"
            + " ORDER BY t.id")
    Page<MCodmstEntity> findByTblidAndCode1AndItem1OrderById(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            @Param("item1") String item1,
            Pageable pageable);

    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタ(ゾーン)のリスト
     */
    @Query(value = "SELECT t.id "
            + "         ,t.tblid "
            + "         ,SUBSTRING(t.code1, 3) as code1 "
            + "         ,t.code2 "
            + "         ,t.code3 "
            + "         ,t.code5 "
            + "         ,t.item1 "
            + "         ,t.item2 "
            + "         ,t.item3 "
            + "         ,t.item4 "
            + "         ,t.item5 "
            + "         ,t.item6 "
            + "         ,t.item7 "
            + "         ,t.item10 "
            + "         ,t.item30 "
            + "         ,t.mntflg "
            + "         ,t.deleted_at "
            + " FROM m_codmst t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deleted_at IS NULL"
            + " AND t.code1 LIKE :code1%"
            + " ORDER BY t.code1", nativeQuery = true)
    Page<MCodmstEntity> findZoneByTblidAndCode1AheadLikeOrderByCode1(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * ブランドコードを指定して配分課を取得する.
     * 暫定的な課(code1の末尾9)は取得しない.
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @param pageable ページ情報
     * @return コードマスタ(配分課)のリスト
     */
    @Query(value = "SELECT * "
            + " FROM m_codmst t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deleted_at IS NULL"
            + " AND t.code1 LIKE :code1%"
            + " AND RIGHT(t.code1, 1) <> '9'"
            + " ORDER BY t.id", nativeQuery = true)
    Page<MCodmstEntity> findAllocationByTblidAndCode1AheadLikeOrderById(
            @Param("tblid") String tblid,
            @Param("code1") String code1,
            Pageable pageable);

    /**
     * アカウント名でブランドコードリストを取得する.
     * @param staffTblid Staffテーブル区分
     * @param brandTblid Brandテーブル区分
     * @param accountName アカウント名
     * @return ブランドコードリスト
     */
    @Query(value = "SELECT bm.code1 "
            + " FROM m_codmst bm"
            + " WHERE bm.tblid = :brandTblid"
            + " AND bm.mntflg IN ('1', '2', '')"
            + " AND bm.deleted_at IS NULL"
            + " AND bm.item10 = (SELECT sm.item6 "
            + " FROM m_codmst sm"
            + " WHERE sm.tblid = :staffTblid"
            + " AND sm.mntflg IN ('1', '2', '')"
            + " AND sm.deleted_at IS NULL"
            + " AND sm.code1 = :accountName)"
            + " ORDER BY bm.code1", nativeQuery = true)
    List<String> findBrandCodeByAccountName(
            @Param("staffTblid") String staffTblid,
            @Param("brandTblid") String brandTblid,
            @Param("accountName") String accountName);


    /**
     * 登録されているアイテムコードをブランドの重複無しの状態で取得する.
     * @param tblid テーブル区分
     * @param pageable ページ情報
     * @return アイテムコードリスト
     */
    @Query(value = "SELECT t.id "
            + "         ,t.tblid "
            + "         ,'' as code1 "
            + "         ,t.code2"
            + "         ,'' as code3 "
            + "         ,'' as code5 "
            + "         ,'' as item1 "
            + "         ,'' as item2 "
            + "         ,'' as item3 "
            + "         ,'' as item4 "
            + "         ,'' as item5 "
            + "         ,'' as item6 "
            + "         ,'' as item7 "
            + "         ,'' as item10 "
            + "         ,'' as item30 "
            + "         ,'' as mntflg "
            + "         ,null as deleted_at "
            + " FROM m_codmst t"
            + " WHERE t.tblid = :tblid "
            +  "GROUP BY tblid, code2 "
            + " ORDER BY t.code2 ASC", nativeQuery = true)
    Page<MCodmstEntity> findNoDuplicationItemCode(
            @Param("tblid") String tblid,
            Pageable pageable);

    /**
     * tblidとcode1を指定して取得する.
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @return コードマスタ
     */
    @Query(value = "SELECT t.* "
            + " FROM m_codmst t"
            + " WHERE t.tblid = :tblid"
            + " AND TRIM(t.code1) = :code1"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    Optional<MCodmstEntity> findByTblidAndCode1(
            @Param("tblid") String tblid,
            @Param("code1") String code1);

    /**
     * 事業部コードでブランドコードリストを取得する.
     * @param brandTblid Brandテーブル区分
     * @param divisionCode 事業部コード
     * @return ブランドコードリスト
     */
    @Query(value = "SELECT m.code1 "
            + " FROM m_codmst m"
            + " WHERE m.tblid = :brandTblid"
            + " AND m.item4 = :divisionCode"
            + " AND m.mntflg IN ('1', '2', '')"
            + " AND m.deleted_at IS NULL"
            + " ORDER BY m.code1", nativeQuery = true)
    List<String> findBrandCodesByDivisionCode(
            @Param("brandTblid") String brandTblid,
            @Param("divisionCode") String divisionCode);
    /**
     * @param tblid テーブル区分
     * @param code1 キーコード１
     * @return item7(職種)
     */
    @Query("SELECT t.item7 FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND TRIM(t.code1) = :code1")
    String findItem7ByTblidAndCode1(
            @Param("tblid") String tblid,
            @Param("code1") String code1);

    /**
     * @param code1s コード1の値の配列
     * @param item1 内容１
     * @param tblid テーブル区分
     * @return MCodmstEntity
     */
    @Query("SELECT t FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid "
            + " AND t.code1 IN (:code1s) "
            + " AND TRIM(t.item1) = :item1"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL")
    Optional<MCodmstEntity> findByTblidAndCode1sAndItem1(
            @Param("tblid") String tblid,
            @Param("code1s") List<String> code1s,
            @Param("item1") String item1);
    
    // PRD_0131 #10039 add JFE start
    /**
     * @param tblid テーブル区分
     * @param divisionCode 課コード(code1の下２桁)
     * @param brandCode ブランドコード(code1の上２桁)
     * @return item3の先頭２文字(物流コード)
     */
    @Query("SELECT SUBSTRING(t.item3,1,2) FROM MCodmstEntity t"
            + " WHERE t.tblid = :tblid"
            + " AND t.mntflg IN ('1', '2', '')"
            + " AND t.deletedAt IS NULL"
            + " AND SUBSTRING(t.code1,1,2) = :brandCode"
            + " AND SUBSTRING(t.code1,3,2) = :divisionCode")
    String findItem3ByTblidAndCode1(
            @Param("tblid") String tblid,
            @Param("divisionCode") String divisionCode,
            @Param("brandCode") String brandCode);
    // PRD_0131 #10039 add JFE end
}

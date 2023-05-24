package jp.co.jun.edi.repository;

import java.math.BigInteger;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MHrtmstEntity;

/**
 * 課別配分率マスタのRepository.
 */
@Repository
public interface MHrtmstRepository extends JpaRepository<MHrtmstEntity, BigInteger> {
    /**
     * ブランド・アイテム・シーズンをキーに課別配分率マスタを抽出する.
     * アイテムとシーズンが空白で登録されているケースがあるため、それぞれ空白の検索条件も追加する。
     *
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @param season シーズン
     * @param pageable pageable
     * @return 課別配分率マスタエンティティのリスト
     */
    @Query("SELECT t FROM MHrtmstEntity t"
            + " WHERE t.deletedAt IS NULL"
            + "   AND t.mntflg IN ('1', '2', '')"
            + "   AND t.hrtkbn <> 'A3'" // 配分率手入力は除外
            + "   AND t.brandCode = :brandCode"
            + "   AND ("
            + "       (t.itemCode = :itemCode AND t.season = :season)"
            + "    OR (t.itemCode = :itemCode AND t.season = '')"
            + "    OR (t.itemCode = '' AND t.season = '')"
            + ")")
    Page<MHrtmstEntity> findByBrandCodeAndItemCodeAndSeason(
            @Param("brandCode") String brandCode,
            @Param("itemCode") String itemCode,
            @Param("season") String season,
            Pageable pageable);

  //#PRD_0138 #10680 add JFE start
    /**
     * ブランド、アイテム、シーズン、配分率区分、配分課、配分率名を用いて配分率マスタにデータが存在する場合、削除
     *
     * @param nitymd 日計日
     */
    @Transactional
    @Modifying
    @Query(value = "delete"
    // PRD_0183 mod JFE start
    //+" from m_hrtmst"
    //+" where(brand,item,season,hrtkbn,shpcd,rtname) in(SELECT m.brand ,m.item ,m.season ,m.hrtkbn ,j.hka ,m.rtname"
    //+" FROM m_store_hrtmst m"
    //+" INNER JOIN m_junmst j"
    //+" ON m.brand = j.brand"
    //+" AND m.shpcd = j.shpcd"
    //+" where m.souymd = :nitymd"
    //+" and m.deleted_at IS NULL"
    //+" and j.deleted_at IS NULL"
    //+" group by m.brand ,m.item ,m.season ,m.hrtkbn ,j.hka ,rtname"
    //+" order by m.brand ,m.item ,m.season ,m.hrtkbn ,j.hka ,rtname)",nativeQuery=true)
      +" from m_hrtmst" ,nativeQuery=true)
    // PRD_0183 mod JFE end
	void deleteByHaibunSummaryTarget(@Param("nitymd") String nitymd);

    /**
     * 配分率マスタに下記の値を設定して挿入する
     *
     * @param nitymd 日計日
     * @param adminName 管理者ユーザ名
     * @param adminId 管理者ユーザid
     */
    @Transactional
    @Modifying
    @Query(value = "insert into m_hrtmst"
    		//PRD_0206 JFE mod start
//    		+" SELECT 0,m.brand ,m.item ,m.season ,m.hrtkbn ,j.hka ,m.rtname,SUM(m.hritu),0,:nitymd,:nitymd,:adminName,0,'',now(),:adminId,now(),:adminId,null"
			+" SELECT 0,m.brand ,m.item ,m.season ,m.hrtkbn ,j.hka ,m.rtname,SUM(m.hritu),1,:nitymd,:nitymd,:adminName,0,'',now(),:adminId,now(),:adminId,null"
    		//PRD_0206 JFE mod end
    		+" FROM m_store_hrtmst m"
    		+" INNER JOIN m_junmst j"
    		+" ON m.brand = j.brand"
    		+" AND m.shpcd = j.shpcd"
    		+" where m.souymd = :nitymd"
    		+" and m.hritu != 0"
            +" and j.hka != 19"
            +" and j.hka != 29"
    		+" and m.deleted_at IS NULL"
    	    +" and j.deleted_at IS NULL"
    		+" group by m.brand ,m.item ,m.season ,m.hrtkbn ,j.hka ,rtname"
    		+" order by m.brand ,m.item ,m.season ,m.hrtkbn ,j.hka ,rtname",nativeQuery=true)
	void insertByHaibunSummaryTarget(
            @Param("nitymd") String nitymd,
            @Param("adminName") String adminName,
            @Param("adminId") BigInteger adminId);
  //#PRD_0138 #10680 add JFE end
}

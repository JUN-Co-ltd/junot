package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import jp.co.jun.edi.entity.MKanmstEntity;

/**
 * 発注生産システムの管理マスタのリポジトリ.
 */
    //#PRD_0139_#10681 mod JFE start
public interface MKanmstRepository extends JpaRepository<MKanmstEntity, BigInteger>,JpaSpecificationExecutor<MKanmstEntity> {
    //#PRD_0139_#10681 mod JFE end
	//#PRD_0139 #10681 add JFE start
	/**
     * 管理マスタ情報を取得.
     * @return 管理マスタ情報
     */
    @Query("SELECT t FROM MKanmstEntity t"
    		+" order by id asc")

    Optional<MKanmstEntity> findByTop();

    //#PRD_0139 #10681 add JFE start
  //#PRD_0138 #10680 JFE add Start
  	/**
       * 管理マスタ情報の日計日のみを取得.
       * @return 日計日
       */
    @Query(value = " SELECT m.nitymd"
            + " FROM m_kanmst m "
            + " order by nitymd asc", nativeQuery = true)

      String getNitymd();

	//#PRD_0138 #10680 JFE add end
}

package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;

/**
 *
 * ExtendedTOrderRepository.
 * orderNumberは受注確定されるまで、"000000"が設定されるため.
 * orderNumberを条件にしてSQLを実行すると想定外のレコードも.
 * 編集される可能性があるので、orderNumberは条件に極力使用しないこと.
 *
 */
@Repository
public interface ExtendedTOrderRepository extends JpaRepository<ExtendedTOrderEntity, BigInteger> {

    /**
     * 発注Idから 発注情報+コード名称 を検索する.
     *
     * @param id
     *            発注Id
     * @return 拡張発注情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "   ,ms1.name as matl_maker_name"
            + "   ,ms2.name as mdf_maker_name"
            + "   ,mc1.item2 as mdf_staff_name"
            + "   ,mc2.item1 as coo_name"
            + " FROM t_order t"
            + "   LEFT JOIN m_sirmst ms1 "
          //  + "          ON ms1.sirkbn = '30' "
            + "          ON ms1.mntflg != '3' "
            + "         AND ms1.deleted_at is null "
            + "         AND t.matl_maker_code = ms1.sire "
            + "   LEFT JOIN m_sirmst ms2 "
          //  + "          ON ms2.sirkbn = '10' "
            + "          ON ms2.mntflg != '3' "
            + "         AND ms2.deleted_at is null "
            + "         AND t.mdf_maker_code = ms2.sire "
            + "   LEFT JOIN m_codmst mc1 "
            + "          ON mc1.tblid = '22' "
            + "         AND mc1.mntflg != '3' "
            + "         AND mc1.deleted_at is null "
            + "         AND t.mdf_staff_code = mc1.code1 "
            + "   LEFT JOIN m_codmst mc2 "
            + "          ON mc2.tblid = '05' "
            + "         AND mc2.mntflg != '3' "
            + "         AND mc2.deleted_at is null "
            + "         AND t.coo_code = mc2.code1 "
            + " WHERE t.id = :id "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Optional<ExtendedTOrderEntity> findById(
            @Param("id") BigInteger id);

}

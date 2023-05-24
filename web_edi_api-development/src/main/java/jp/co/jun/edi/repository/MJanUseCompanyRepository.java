package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MJanUseCompanyEntity;

/**
 *
 * MJanUseCompanyRepository.
 *
 */
@Repository
public interface MJanUseCompanyRepository
        extends JpaRepository<MJanUseCompanyEntity, BigInteger>, JpaSpecificationExecutor<MJanUseCompanyEntity> {
    /**
     * ブランドに紐づく会社のJANマスタIDのリストを取得する.
     *
     * @param brandCode ブランドコード
     * @return JANマスタIDのリスト
     */
    @Query("SELECT mjuc.janId FROM MJanUseCompanyEntity mjuc"
            + " INNER JOIN MCodmstEntity mc"
            + "    ON mjuc.companyCode = TRIM(mc.item3)"
            + " WHERE mc.tblid = '02'"
            + "   AND mc.code1 = :brandCode"
            + "   AND mc.mntflg IN ('1', '2', '')"
            + "   AND mc.deletedAt IS NULL"
            + "   AND mjuc.deletedAt IS NULL")
    List<BigInteger> getJanIdsByBrandCode(
            @Param("brandCode") String brandCode);
}

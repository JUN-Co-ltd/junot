package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFBrandDestinationEntity;
import jp.co.jun.edi.type.FukukitaruMasterDestinationType;

/**
 * フクキタル用ブランドコード別宛先マスタを検索するリポジトリ.
 */
public interface MFBrandDestinationRepository extends JpaRepository<MFBrandDestinationEntity, BigInteger>, JpaSpecificationExecutor<MFBrandDestinationEntity> {
    /**
     * ブランドコード、メーカーコード、宛先種別から宛先情報を取得.
     * @param brandCode ブランドコード
     * @param company メーカーコード
     * @param destinationType 宛先種別 {@link FukukitaruMasterDestinationType}
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM MFBrandDestinationEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.brandCode = :brandCode"
            + " AND t.company = :company"
            + " AND t.destinationType = :destinationType")
    Optional<MFBrandDestinationEntity> findByBrandCodeAndCompanyAndDestinationType(
            @Param("brandCode") String brandCode,
            @Param("company") String company,
            @Param("destinationType") FukukitaruMasterDestinationType destinationType);
}

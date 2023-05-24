package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFDestinationEntity;
import jp.co.jun.edi.type.BooleanType;

/**
 * フクキタル用宛先の会社情報を検索するリポジトリ.
 */
public interface MFDestinationRepository extends JpaRepository<MFDestinationEntity, BigInteger>, JpaSpecificationExecutor<MFDestinationEntity> {
    /**
     * 請求先IDが一致するフクキタル発注情報の承認需要フラグ取得.
     *
     * @param billingCompanyId 請求先ID
     * @return 請求先IDが合致するフクキタル発注情報の承認需要フラグ
     */
    @Query("SELECT m.isApprovalRequired FROM MFDestinationEntity m"
            + " WHERE m.id = :billingCompanyId"
            + " AND m.deletedAt is null")
    BooleanType findIsApprovalRequiredByBillingCompanyId(@Param("billingCompanyId") BigInteger billingCompanyId);
}

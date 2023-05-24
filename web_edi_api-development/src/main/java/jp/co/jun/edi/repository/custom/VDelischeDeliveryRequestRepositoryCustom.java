package jp.co.jun.edi.repository.custom;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import jp.co.jun.edi.entity.VDelischeDeliveryRequestEntity;

/**
 * デリスケ納品依頼Repositoryのカスタムインターフェース.
 */
public interface VDelischeDeliveryRequestRepositoryCustom {
    /**
     * VDelischeDeliveryRequestEntityを取得する.
     * @param orderId 発注ID
     * @param deliveryAtFrom 納期from
     * @param deliveryAtTo 納期to
     * @param deliveryAtDateFrom 納期from(年度・納品週から作成)
     * @param deliveryAtDateTo 納期to(年度・納品週から作成)
     * @param deliveryAtLateType 納期遅延フラグ
     * @return VDelischeDeliveryRequestEntity
     */
    List<VDelischeDeliveryRequestEntity> findBySpec(BigInteger orderId, Date deliveryAtFrom, Date deliveryAtTo,
            Date deliveryAtDateFrom, Date deliveryAtDateTo, boolean deliveryAtLateType);
}

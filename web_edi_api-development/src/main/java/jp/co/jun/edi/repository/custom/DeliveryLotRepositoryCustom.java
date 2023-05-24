package jp.co.jun.edi.repository.custom;

import java.math.BigInteger;

/**
 * DeliveryLotRepositoryのカスタムインターフェース.
 */
public interface DeliveryLotRepositoryCustom {

    /**
     * 発注IDに紐づく納品SKUの納品数量合計を取得する.
     * ただし指定した納品IDに紐づく納品SKUは除外する.
     * @param orderId 発注ID
     * @param excludeDeliveryId 除外する納品ID
     * @return 納品数量合計
     */
    int sumDeliveryLotByOrderId(BigInteger orderId, BigInteger excludeDeliveryId);
}

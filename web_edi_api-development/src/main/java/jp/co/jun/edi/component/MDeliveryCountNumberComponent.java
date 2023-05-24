package jp.co.jun.edi.component;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.MDeliveryCountNumberEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.MDeliveryCountNumberRepository;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品依頼回数採番マスタ関連のコンポーネント.
 */
@Component
public class MDeliveryCountNumberComponent extends GenericComponent {

    @Autowired
    private MDeliveryCountNumberRepository deliveryCountNumberRepository;

    // 最大登録件数
    private static final Integer LIMIT_DELIVERY_COUNT = 99;

    /**
     * 納品依頼回数の採番処理を行う.
     * 更新対象レコードが存在しない場合はResourceNotFoundExceptionをスロー.
     * 最大登録件数を超える場合は業務エラーをスロー.
     *
     * @param orderId 発注ID
     * @return 採番後納品依頼回数
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Integer numberDeliveryCount(final BigInteger orderId) {
        final MDeliveryCountNumberEntity deliveryCountNumberEntity = deliveryCountNumberRepository.findByOrderId(orderId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_DN_001)));

        final int currentDeliveryCount = deliveryCountNumberEntity.getDeliveryCount() + 1;
        // 登録上限を超える場合は業務エラー
        if (currentDeliveryCount > LIMIT_DELIVERY_COUNT) {
          throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_D_002));
        }

        deliveryCountNumberEntity.setDeliveryCount(currentDeliveryCount);
        deliveryCountNumberRepository.save(deliveryCountNumberEntity);

        return currentDeliveryCount;
    }
}

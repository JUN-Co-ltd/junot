package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TDeliveryOfficialSendMailEntity;
import jp.co.jun.edi.repository.TDeliveryOfficialSendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;

/***
 * 納品依頼正式メール送信バッチステータス更新用コンポーネント.
 */
@Component
public class DeliveryRequestOfficialStatusComponent {
    @Autowired
    private TDeliveryOfficialSendMailRepository tDeliveryOfficialSendMailRepository;

    /**
     * 処理対象の納品依頼正式メール送信情報のステータスを「処理中(1)」に更新.
     * @param listTDeliveryOfficialSendMailEntity 納品依頼正式メール送信情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TDeliveryOfficialSendMailEntity> listTDeliveryOfficialSendMailEntity, final BigInteger userId) {
        if (listTDeliveryOfficialSendMailEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTDeliveryOfficialSendMailEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tDeliveryOfficialSendMailRepository.updateStatusByIds(SendMailStatusType.PROCESSING.getValue(), ids, userId);
    }
}

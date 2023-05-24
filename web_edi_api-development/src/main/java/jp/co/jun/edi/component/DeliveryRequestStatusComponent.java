package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TDeliverySendMailEntity;
import jp.co.jun.edi.repository.TDeliverySendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;

/**
 * 納品依頼書用即時メール情報を取得するコンポーネント.
 */
@Component
public class DeliveryRequestStatusComponent {


    @Autowired
    private TDeliverySendMailRepository tDeliverySendMailRepository;




    /**
     * 処理対象の受注確定メール送信情報のステータスを「処理中(1)」に更新.
     * @param listTDeliverySendMailEntity 受信確定メール送信情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TDeliverySendMailEntity> listTDeliverySendMailEntity, final BigInteger userId) {
        if (listTDeliverySendMailEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTDeliverySendMailEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tDeliverySendMailRepository.updateStatusById(SendMailStatusType.PROCESSING.getValue(), ids, userId);
    }
}

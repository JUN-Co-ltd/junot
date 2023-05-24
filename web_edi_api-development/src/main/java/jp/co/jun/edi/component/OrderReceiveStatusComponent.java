package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TOrderSendMailEntity;
import jp.co.jun.edi.repository.TOrderSendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;

/***
 * 受注確定メール送信バッチステータス更新用コンポーネント.
 */
@Component
public class OrderReceiveStatusComponent {
    @Autowired
    private TOrderSendMailRepository tOrderSendMailRepository;

    /**
     * 処理対象の受注確定メール送信情報のステータスを「処理中(1)」に更新.
     * @param listTOrderSendMailEntity 受信確定メール送信情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TOrderSendMailEntity> listTOrderSendMailEntity, final BigInteger userId) {
        if (listTOrderSendMailEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTOrderSendMailEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tOrderSendMailRepository.updateStatusById(SendMailStatusType.PROCESSING.getValue(), ids, userId);
    }
}

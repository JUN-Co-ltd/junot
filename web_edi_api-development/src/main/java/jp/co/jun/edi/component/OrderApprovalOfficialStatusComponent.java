package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TOrderApprovalOfficialSendMailEntity;
import jp.co.jun.edi.repository.TOrderApprovalOfficialSendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;

/***
 * 発注承認正式メール送信バッチステータス更新用コンポーネント.
 */
@Component
public class OrderApprovalOfficialStatusComponent {
    @Autowired
    private TOrderApprovalOfficialSendMailRepository tOrderApprovalOfficialSendMailRepository;

    /**
     * 処理対象の発注承認正式メール送信情報のステータスを「処理中(1)」に更新.
     * @param listTOrderApprovalOfficialSendMailEntity 発注承認正式メール送信情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TOrderApprovalOfficialSendMailEntity> listTOrderApprovalOfficialSendMailEntity,
            final BigInteger userId) {
        if (listTOrderApprovalOfficialSendMailEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTOrderApprovalOfficialSendMailEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tOrderApprovalOfficialSendMailRepository.updateStatusByIds(SendMailStatusType.PROCESSING.getValue(), ids, userId);
    }
}

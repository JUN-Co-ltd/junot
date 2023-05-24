package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TOrderApprovalSendMailEntity;
import jp.co.jun.edi.repository.TOrderApprovalSendMailRepository;
import jp.co.jun.edi.type.SendMailStatusType;

/***
 * 発注承認メール送信バッチステータス更新用コンポーネント.
 */
@Component
public class OrderApprovalStatusComponent {
    @Autowired
    private TOrderApprovalSendMailRepository tOrderApprovalSendMailRepository;

    /**
     * 処理対象の発注承認メール送信情報のステータスを「処理中(1)」に更新.
     * @param listTOrderApprovalSendMailEntity 発注承認メール送信情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TOrderApprovalSendMailEntity> listTOrderApprovalSendMailEntity, final BigInteger userId) {
        if (listTOrderApprovalSendMailEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTOrderApprovalSendMailEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tOrderApprovalSendMailRepository.updateStatusById(SendMailStatusType.PROCESSING.getValue(), ids, userId);
    }
}

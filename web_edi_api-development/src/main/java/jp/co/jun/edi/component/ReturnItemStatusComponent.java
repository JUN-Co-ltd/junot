package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TReturnVoucherEntity;
import jp.co.jun.edi.repository.TReturnVoucherRepository;
import jp.co.jun.edi.type.SendMailStatusType;

/***
 * 返品伝票管理バッチステータス更新用コンポーネント.
 */
@Component
public class ReturnItemStatusComponent {
    @Autowired
    private TReturnVoucherRepository tReturnVoucherRepository;

    /**
     * 処理対象の返品伝票管理のステータスを「処理中(1)」に更新.
     * @param listTReturnVoucherEntity 返品伝票管理情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TReturnVoucherEntity> listTReturnVoucherEntity, final BigInteger userId) {
        if (listTReturnVoucherEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTReturnVoucherEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tReturnVoucherRepository.updateStatusById(SendMailStatusType.PROCESSING.getValue(), ids, userId);
    }

}

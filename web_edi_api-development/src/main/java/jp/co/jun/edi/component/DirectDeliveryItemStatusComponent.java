package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TDeliveryVoucherFileInfoEntity;
import jp.co.jun.edi.repository.TDeliveryVoucherFileInfoRepository;
import jp.co.jun.edi.type.FileInfoStatusType;

/***
 * 直送伝票管理バッチステータス更新用コンポーネント.
 */
@Component
public class DirectDeliveryItemStatusComponent {
    @Autowired
    private TDeliveryVoucherFileInfoRepository repository;

    /**
     * 処理対象の直送伝票管理のステータスを「処理中(1)」に更新.
     * @param entities 直送伝票管理情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TDeliveryVoucherFileInfoEntity> entities, final BigInteger userId) {
        if (entities.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = entities.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        repository.updateStatusByIds(FileInfoStatusType.FILE_PROCESSING.getValue(), ids, userId);
    }

}

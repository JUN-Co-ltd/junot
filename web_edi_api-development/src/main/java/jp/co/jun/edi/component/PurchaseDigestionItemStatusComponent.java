package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TPurchaseFileInfoEntity;
import jp.co.jun.edi.repository.TPurchasesFileInfoRepository;
import jp.co.jun.edi.type.SendMailStatusType;

//PRD_0134 #10654 add JEF start
/***
 * 仕入伝票管理バッチステータス更新用コンポーネント.
 */
@Component
public class PurchaseDigestionItemStatusComponent {
    @Autowired
    private TPurchasesFileInfoRepository tPurchasesFileInfoRepository;

    /**
     * 処理対象の仕入伝票管理のステータスを「処理中(1)」に更新.
     * @param listTReturnVoucherEntity 仕入伝票管理情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForBeingProcessed(final List<TPurchaseFileInfoEntity> listTPurchasesVoucherEntity, final BigInteger userId) {
        if (listTPurchasesVoucherEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTPurchasesVoucherEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tPurchasesFileInfoRepository.updateStatusById(SendMailStatusType.PROCESSING.getValue(), ids, userId);
    }


    /**
     * 処理対象の仕入伝票管理のステータスを「完了(2)」に更新.
     * @param listTReturnVoucherEntity 仕入伝票管理情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateStatusForCompleted(final List<TPurchaseFileInfoEntity> listTPurchasesVoucherEntity, final BigInteger userId) {
        if (listTPurchasesVoucherEntity.isEmpty()) {
            return;
        }
        final List<BigInteger> ids = listTPurchasesVoucherEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tPurchasesFileInfoRepository.updateStatusById(SendMailStatusType.COMPLETED.getValue(), ids, userId);
    }
}
//PRD_0134 #10654 add JEF end
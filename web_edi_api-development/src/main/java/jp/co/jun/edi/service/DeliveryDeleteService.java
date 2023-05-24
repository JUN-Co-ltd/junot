package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.entity.TDeliveryStoreEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TDeliveryStoreRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品依頼関連テーブル削除処理.
 */
@Service
public class DeliveryDeleteService
extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {

    @Autowired
    private TDeliveryRepository tDeliveryRepository;

    @Autowired
    private TDeliveryDetailRepository tDeliveryDetailRepository;

    @Autowired
    private TDeliverySkuRepository tDeliverySkuRepository;

    @Autowired
    private TDeliveryStoreRepository tDeliveryStoreRepository;

    @Autowired
    private TDeliveryStoreSkuRepository tDeliveryStoreSkuRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {

        // 削除日付
        final Date deleteAt = new Date();

        final BigInteger deliveryId = serviceParameter.getId();

        // 納品情報を取得
        final TDeliveryEntity tDeliveryEntity = tDeliveryRepository.findByIdAndDeletedAtIsNull(deliveryId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_D_018)));

        // バリデーション
        canDelete(tDeliveryEntity, serviceParameter);

        final BigInteger userId = serviceParameter.getLoginUser().getUserId();

        // 承認済の場合、発注SKUの納品依頼数量更新
        if (ApprovalType.APPROVAL.getValue().equals(tDeliveryEntity.getDeliveryApproveStatus())) {
            orderSkuRepository.subtractDeliveryLotByDeliveryDelete(deliveryId, userId);
        }

        // 納品情報の削除日更新
        tDeliveryEntity.setDeletedAt(deleteAt);
        tDeliveryRepository.save(tDeliveryEntity);

        // 納品明細情報の削除日更新
        deleteDetail(deleteAt, tDeliveryEntity);

        return DeleteServiceResponse.builder().build();
    }

    /**
     * 削除可能チェック.
     * @param serviceParameter パラメータ
     * @param tDeliveryEntity DB登録済納品情報
     */
    private void canDelete(final TDeliveryEntity tDeliveryEntity, final DeleteServiceParameter<BigInteger> serviceParameter) {
        // 承認済みの場合、バリデーションチェック
        if (ApprovalType.APPROVAL.getValue().equals(tDeliveryEntity.getDeliveryApproveStatus())) {
            deliveryComponent.judgeDeleteValidateAtApproval(serviceParameter, tDeliveryEntity);
        }

        final PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);

        // 納品情報を取得
        final BigInteger deliveryId = serviceParameter.getId();
        final Page<TDeliveryDetailEntity> deliveryDetailPage = tDeliveryDetailRepository.findByDeliveryId(deliveryId, pageRequest);

        if (!deliveryDetailPage.hasContent()) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_D_018));
        }

        final Page<TPurchaseEntity> purcahsePage = purchaseRepository.findByDeliveryId(tDeliveryEntity.getId(),
                                                                                         PageRequest.of(0, Integer.MAX_VALUE));

        final ResultMessages rsltMsg = ResultMessages.warning();

        // 仕入済チェック
        deliveryComponent.checkArrived(deliveryDetailPage.getContent(), purcahsePage.getContent(), rsltMsg);

        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }
    }

    /**
     * 納品明細を論理削除する.
     * @param deleteAt 削除日
     * @param tDeliveryEntity 納品依頼
     */
    private void deleteDetail(final Date deleteAt, final TDeliveryEntity tDeliveryEntity) {
        final List<TDeliveryDetailEntity> deliveryDetails =
                tDeliveryDetailRepository.findByDeliveryId(tDeliveryEntity.getId(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        final List<TDeliveryDetailEntity> deletedAtSetted = deliveryDetails.stream()
                .map(detail -> {
                    // 納品SKU情報の削除日更新
                    deleteSku(deleteAt, detail);
                    // 納品得意先情報の削除日更新
                    deleteStore(deleteAt, detail);

                    detail.setDeletedAt(deleteAt);
                    // 承認済みの場合連携ステータスに未連携をセット
                    if (deliveryComponent.isDeliveryApproved(tDeliveryEntity.getDeliveryApproveStatus())) {
                        detail.setLinkingStatus(LinkingStatusType.TARGET);
                    } else {
                        // 未承認の場合は、対象外をセット
                        detail.setLinkingStatus(LinkingStatusType.EXCLUDED);
                    }
                    return detail;
                    }
                ).collect(Collectors.toList());

        tDeliveryDetailRepository.saveAll(deletedAtSetted);
    }

    /**
     * 納品SKUを論理削除する.
     * @param deleteAt 削除日
     * @param deliveryDetailEntity 納品明細
     */
    private void deleteSku(final Date deleteAt, final TDeliveryDetailEntity deliveryDetailEntity) {
        final List<TDeliverySkuEntity> deliverySkus =
                tDeliverySkuRepository.findByDeliveryDetailId(deliveryDetailEntity.getId(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        final List<TDeliverySkuEntity> deletedAtSetted = deliverySkus.stream()
                .map(sku -> {
                    sku.setDeletedAt(deleteAt);
                    return sku;
                    }
                ).collect(Collectors.toList());

        tDeliverySkuRepository.saveAll(deletedAtSetted);
    }

    /**
     * 納品得意先を論理削除する.
     * @param deleteAt 削除日
     * @param deliveryDetailEntity 納品明細
     */
    private void deleteStore(final Date deleteAt, final TDeliveryDetailEntity deliveryDetailEntity) {
        final List<TDeliveryStoreEntity> deliveryStores =
                tDeliveryStoreRepository.findByDeliveryDetailId(deliveryDetailEntity.getId(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

        final List<TDeliveryStoreEntity> deletedAtSetted = deliveryStores.stream()
                .map(store -> {
                    // 納品得意先SKU情報の削除日更新
                    deleteStoreSku(deleteAt, store);
                    store.setDeletedAt(deleteAt);
                    return store;
                    }
                ).collect(Collectors.toList());

        tDeliveryStoreRepository.saveAll(deletedAtSetted);
    }

    /**
     * 納品得意先SKUを論理削除する.
     * @param deleteAt 削除日
     * @param deliveryStoreEntity 納品明細
     */
    private void deleteStoreSku(final Date deleteAt, final TDeliveryStoreEntity deliveryStoreEntity) {
        final List<TDeliveryStoreSkuEntity> deliveryStoreSkus =
                tDeliveryStoreSkuRepository.findByDeliveryStoreId(deliveryStoreEntity.getId(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).getContent();

         final List<TDeliveryStoreSkuEntity> deletedAtSetted = deliveryStoreSkus.stream()
                 .map(sku -> {
                     sku.setDeletedAt(deleteAt);
                     return sku;
                     }
                 ).collect(Collectors.toList());

        tDeliveryStoreSkuRepository.saveAll(deletedAtSetted);
    }
}

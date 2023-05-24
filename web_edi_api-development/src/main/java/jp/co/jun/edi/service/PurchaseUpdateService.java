package jp.co.jun.edi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.PurchaseComponent;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.PurchaseDivisionModel;
import jp.co.jun.edi.model.PurchaseModel;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 仕入更新処理.
 */
@Service
public class PurchaseUpdateService extends GenericUpdateService<UpdateServiceParameter<PurchaseModel>, UpdateServiceResponse<PurchaseModel>> {

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseComponent purchaseComponent;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Override
    protected UpdateServiceResponse<PurchaseModel> execute(final UpdateServiceParameter<PurchaseModel> serviceParameter) {
        final PurchaseModel purchaseRequest = serviceParameter.getItem();

        // バリデーションチェック
        final ResultMessages rsltMsg = checkValidate(purchaseRequest);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        final List<TPurchaseEntity> entities = purchaseComponent.toPurchaseEntitiesForUpsert(purchaseRequest);

        // 仕入情報の更新
        purchaseRepository.saveAll(entities);

        // 納品明細更新
        deliveryComponent.updateDeliveryDetailArrivalPlace(purchaseRequest, serviceParameter.getLoginUser().getUserId());

        // レスポンス作成
        final PurchaseModel res = purchaseComponent.toPurchaseModel(entities);

        return UpdateServiceResponse.<PurchaseModel>builder().item(res).build();
    }

    /**
     * 仕入更新バリデーションチェックを行う.
     * @param purchaseRequest 仕入フォーム値
     * @return ResultMessages
     */
    private ResultMessages checkValidate(final PurchaseModel purchaseRequest) {
        final DeliveryModel delivery = deliveryComponent.findDeliveryById(purchaseRequest.getDeliveryId());
        final ResultMessages rsltMsg = purchaseComponent.checkValidate(purchaseRequest, delivery);

        final Page<TPurchaseEntity> page = purchaseRepository.findByDeliveryId(purchaseRequest.getDeliveryId(), PageRequest.of(0, Integer.MAX_VALUE));
        // t_purchaseになければ、エラー
        if (!page.hasContent()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_002));
            return rsltMsg;
        }

        purchaseRequest.getPurchaseSkus().forEach(p ->
            p.getPurchaseDivisions().forEach(d -> {
                final Optional<TPurchaseEntity> opt = filterByDivisionCode(page.getContent(), d);
                final Object[] args = {d.getDivisionCode(), p.getColorCode(), p.getSize()};

                if (!opt.isPresent()) {
                    // t_purchaseになければ、エラー
                    rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_007,
                            purchaseComponent.getMessage("code.400_PC_07", args)));

                } else if (LgSendType.INSTRUCTION == opt.get().getLgSendType()) {
                    // リクエストパラメータにLG送信指示済がある場合、エラー
                    rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_003,
                            purchaseComponent.getMessage("code.400_PC_03", args)));
                }
            })
        );

        return rsltMsg;
    }

  /**
    *
    * @param dbPurchases DBの仕入情報リスト
    * @param div リクエストの仕入の課情報
    * @return 課で抽出した仕入情報
    */
   private Optional<TPurchaseEntity> filterByDivisionCode(final List<TPurchaseEntity> dbPurchases, final PurchaseDivisionModel div) {
       return dbPurchases.stream()
               .filter(db -> db.getDivisionCode().equals(div.getDivisionCode()))
               .findFirst();
   }

}

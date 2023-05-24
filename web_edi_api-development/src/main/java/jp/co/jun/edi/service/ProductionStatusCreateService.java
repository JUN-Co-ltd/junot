package jp.co.jun.edi.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TProductionStatusEntity;
import jp.co.jun.edi.entity.TProductionStatusHistoryEntity;
import jp.co.jun.edi.model.ProductionStatusModel;
import jp.co.jun.edi.repository.TProductionStatusHistoryRepository;
import jp.co.jun.edi.repository.TProductionStatusRepository;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;

/**
 * 生産ステータス登録処理.
 */
@Service
public class ProductionStatusCreateService
    extends GenericCreateService<CreateServiceParameter<ProductionStatusModel>, CreateServiceResponse<ProductionStatusModel>> {

    @Autowired
    private TProductionStatusRepository productionStatusRepository;

    @Autowired
    private TProductionStatusHistoryRepository productionStatusHistoryRepository;

    @Override
    protected CreateServiceResponse<ProductionStatusModel> execute(
            final CreateServiceParameter<ProductionStatusModel> serviceParameter) {

        final ProductionStatusModel model = serviceParameter.getItem();

        // 発注IDを基に生産ステータスを取得する
        Optional<TProductionStatusEntity> statusOptimal =
                productionStatusRepository.findByOrderId(model.getOrderId());

        // 取得できなければ、新規に作成する
        TProductionStatusEntity entity = new TProductionStatusEntity();
        if (statusOptimal.isPresent()) {
            entity = statusOptimal.get();
        } else {

            // 発注IDをセット
            entity.setOrderId(model.getOrderId());
            // 発注Noをセット
            entity.setOrderNumber(model.getOrderNumber());
        }

        // 生産ステータスに、最新の情報をセット
        entity = setLatestStatus(model, entity);

        // 生産ステータスを更新
        productionStatusRepository.save(entity);

        // 生産ステータス履歴を作成
        final TProductionStatusHistoryEntity historyEntity = new TProductionStatusHistoryEntity();
        BeanUtils.copyProperties(model, historyEntity);

        // 生産ステータスIDをセット
        historyEntity.setProductionStatusId(entity.getId());

        productionStatusHistoryRepository.save(historyEntity);

        return CreateServiceResponse.<ProductionStatusModel>builder().item(model).build();
    }

    /**
     * 最新ステータス セット処理.
     * @param productionStatusModel 画面から引き渡された更新情報
     * @param productionStatusEntity 現在のDBの情報
     * @return 最新情報に更新した生産ステータス
     */
    private TProductionStatusEntity setLatestStatus(final ProductionStatusModel productionStatusModel,
            final TProductionStatusEntity productionStatusEntity) {
        switch (productionStatusModel.getProductionStatusType()) {
        // サンプル
        case SAMPLE:
            productionStatusEntity.setSampleCompletionAt(productionStatusModel.getSampleCompletionAt());
            productionStatusEntity.setSampleCompletionFixAt(productionStatusModel.getSampleCompletionFixAt());
            break;
        // 仕様確定
        case SPECIFICATION_FIX:
            productionStatusEntity.setSpecificationAt(productionStatusModel.getSpecificationAt());
            productionStatusEntity.setSpecificationFixAt(productionStatusModel.getSpecificationFixAt());
            break;
        // 生地入荷
        case TEXTURE_ARRIVAL:
            productionStatusEntity.setTextureArrivalAt(productionStatusModel.getTextureArrivalAt());
            productionStatusEntity.setTextureArrivalFixAt(productionStatusModel.getTextureArrivalFixAt());
            break;
        // 付属入荷
        case ATTACHMENT_ARRIVAL:
            productionStatusEntity.setAttachmentArrivalAt(productionStatusModel.getAttachmentArrivalAt());
            productionStatusEntity.setAttachmentArrivalFixAt(productionStatusModel.getAttachmentArrivalFixAt());
            break;
        // 縫製中
        case SEWING_IN:
            productionStatusEntity.setCompletionAt(productionStatusModel.getCompletionAt());
            productionStatusEntity.setCompletionFixAt(productionStatusModel.getCompletionFixAt());
            productionStatusEntity.setCompletionCount(productionStatusModel.getCompletionCount());
            break;
        // 縫製検品
        case SEW_INSPECTION:
            productionStatusEntity.setSewInspectionAt(productionStatusModel.getSewInspectionAt());
            productionStatusEntity.setSewInspectionFixAt(productionStatusModel.getSewInspectionFixAt());
            break;
        // 検品
        case INSPECTION:
            productionStatusEntity.setInspectionAt(productionStatusModel.getInspectionAt());
            productionStatusEntity.setInspectionFixAt(productionStatusModel.getInspectionFixAt());
            productionStatusEntity.setCompletionAt(productionStatusModel.getCompletionAt());
            productionStatusEntity.setCompletionFixAt(productionStatusModel.getCompletionFixAt());
            productionStatusEntity.setCompletionCount(productionStatusModel.getCompletionCount());
            break;
        // SHIP
        case SHIP:
            productionStatusEntity.setLeavePortAt(productionStatusModel.getLeavePortAt());
            productionStatusEntity.setLeavePortFixAt(productionStatusModel.getLeavePortFixAt());
            productionStatusEntity.setEnterPortAt(productionStatusModel.getEnterPortAt());
            productionStatusEntity.setEnterPortFixAt(productionStatusModel.getEnterPortFixAt());
            productionStatusEntity.setCustomsClearanceAt(productionStatusModel.getCustomsClearanceAt());
            productionStatusEntity.setCustomsClearanceFixAt(productionStatusModel.getCustomsClearanceFixAt());
            break;
        // DISTA入荷日
        case DISTA_ARRIVAL:
            productionStatusEntity.setDistaArrivalAt(productionStatusModel.getDistaArrivalAt());
            productionStatusEntity.setDistaArrivalFixAt(productionStatusModel.getDistaArrivalFixAt());
            break;
        default:
            break;
        }
        return productionStatusEntity;
    }
}

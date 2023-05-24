package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.component.FukukitaruOrderComponent;
import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * フクキタル品番情報を取得するサービス.
 *
 */
@Service
public class FukukitaruOrderGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<FukukitaruOrderModel>> {
    @Autowired
    private TFOrderRepository tfOrderRepository;

    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;
    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;
    @Autowired
    private FukukitaruOrderComponent fukukitaruOrderComponent;
    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Override
    protected GetServiceResponse<FukukitaruOrderModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {

        // フクキタル発注情報を取得し、データが存在しない場合は例外をスローする
        TFOrderEntity tfOrderEntity = tfOrderRepository.findByIdDeletedAtIsNull(serviceParameter.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // フクキタル品番情報を取得し、データが存在しない場合は例外をスローする
        final FukukitaruItemModel fkItemModel = fukukitaruItemComponent.generatedFukukitaruItemModelSearchFItemId(tfOrderEntity.getFItemId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 品番情報を取得し、データが存在しない場合は例外をスローする
        final ExtendedTItemEntity extendedTItemEntity = extendedTItemRepository.findById(tfOrderEntity.getPartNoId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 発注情報を取得する
        final ExtendedTOrderEntity tOrderEntity = extendedTOrderRepository.findById(tfOrderEntity.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // レスポンスモデルの生成
        final FukukitaruOrderModel fukukitaruOrderModel = fukukitaruOrderComponent.setTFOrderEntityForModel(tfOrderEntity);

        // フクキタル発注SKUを取得し、モデルに設定する
        fukukitaruOrderComponent.setFOrderSkuEntityForModel(fukukitaruOrderModel);

        // フクキタル品番情報セット
        fukukitaruOrderModel.setFkItem(fkItemModel);

        // 品番情報セット:品番ID
        fukukitaruOrderModel.setPartNoId(extendedTItemEntity.getId());
        // 品番情報セット:品番
        fukukitaruOrderModel.setPartNo(extendedTItemEntity.getPartNo());
        // 品番情報セット:品名
        fukukitaruOrderModel.setProductName(extendedTItemEntity.getProductName());

        // 発注情報セット:発注ID
        fukukitaruOrderModel.setOrderId(tOrderEntity.getId());
        // 発注情報セット:発注No
        fukukitaruOrderModel.setOrderNumber(tOrderEntity.getOrderNumber());
        // 発注情報セット:製品修正納期
        fukukitaruOrderModel.setProductCorrectionDeliveryAt(tOrderEntity.getProductCorrectionDeliveryAt());
        // 品番情報セット:生産メーカー名
        fukukitaruOrderModel.setMdfMakerName(tOrderEntity.getMdfMakerName());

        return GetServiceResponse.<FukukitaruOrderModel>builder().item(fukukitaruOrderModel).build();

    }

}

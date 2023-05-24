package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.extended.ExtendedDistributionShipmentSearchResultEntity;
import jp.co.jun.edi.model.DistributionShipmentSearchConditionModel;
import jp.co.jun.edi.model.DistributionShipmentSearchResultModel;
import jp.co.jun.edi.repository.DistributionShipmentSearchResultCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.DistributionShipmentSendType;

/**
 * 画面で指定された検索条件を基に納品予定情報を取得するサービス.
 */
@Service
public class DistributionShipmentSearchListService
extends GenericListService<ListServiceParameter<DistributionShipmentSearchConditionModel>,
ListServiceResponse<DistributionShipmentSearchResultModel>> {
    @Autowired
    private DistributionShipmentSearchResultCompositeRepository extendedDistributionShipmentDeliveryStoreRepository;

    @Override
    protected ListServiceResponse<DistributionShipmentSearchResultModel>
    execute(final ListServiceParameter<DistributionShipmentSearchConditionModel> serviceParameter) {

        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<ExtendedDistributionShipmentSearchResultEntity> page =
                extendedDistributionShipmentDeliveryStoreRepository
                .findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        final List<DistributionShipmentSearchResultModel> items = new ArrayList<>();
        for (final ExtendedDistributionShipmentSearchResultEntity deliveryDetailEntity : page) {

            // 納品依頼情報をコピー
            final DistributionShipmentSearchResultModel distributionShipmentModel
            = new DistributionShipmentSearchResultModel();
            BeanUtils.copyProperties(deliveryDetailEntity, distributionShipmentModel);

            // 送信ステータス更新
            final BooleanType blShippingInstructionsFlg = deliveryDetailEntity.getShippingInstructionsFlg();
            DistributionShipmentSendType sendStatus = DistributionShipmentSendType.NOT_SEND;
            if (blShippingInstructionsFlg == BooleanType.TRUE) {
                sendStatus = DistributionShipmentSendType.SEND;
            }
            distributionShipmentModel.setSendStatus(sendStatus);

            // 数量・合計金額更新
            distributionShipmentModel.setRetailPriceSum(deliveryDetailEntity.getTotalPrice());

            // レスポンスに返却する
            items.add(distributionShipmentModel);
        }
        return ListServiceResponse.<DistributionShipmentSearchResultModel>builder()
                .nextPage(page.hasNext()).items(items).build();
    }
}

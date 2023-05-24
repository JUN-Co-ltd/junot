package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.component.FukukitaruOrderComponent;
import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.FukukitaruOrderSearchConditionModel;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 画面で指定された検索条件を基にフクキタル発注情報を取得するサービス.
 */
@Service
public class FukukitaruOrderListService
        extends GenericListService<ListServiceParameter<FukukitaruOrderSearchConditionModel>, ListServiceResponse<FukukitaruOrderModel>> {
    @Autowired
    private TFOrderRepository tfOrderRepository;
    @Autowired
    private FukukitaruOrderComponent fukukitaruOrderComponent;
    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Override
    protected ListServiceResponse<FukukitaruOrderModel> execute(final ListServiceParameter<FukukitaruOrderSearchConditionModel> serviceParameter) {
        FukukitaruOrderSearchConditionModel fukukitaruSearchOrderConditionModel = serviceParameter.getSearchCondition();

        // フクキタル品番情報を取得し、データが存在しない場合は例外をスローする
        final FukukitaruItemModel fkItemModel = fukukitaruItemComponent
                .generatedFukukitaruItemModelSearchPartNoId(fukukitaruSearchOrderConditionModel.getPartNoId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // フクキタル発注情報を取得する
        final List<TFOrderEntity> listTFOrderEntity = tfOrderRepository
                .findByOrderId(fukukitaruSearchOrderConditionModel.getOrderId(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("orderAt"), Order.asc("id"))))
                .getContent();

        final List<FukukitaruOrderModel> listFukukitaruOrderModel = new ArrayList<>();

        for (TFOrderEntity tfOrderEntity : listTFOrderEntity) {

            // レスポンスモデルの生成
            final FukukitaruOrderModel fukukitaruOrderModel = fukukitaruOrderComponent.setTFOrderEntityForModel(tfOrderEntity);
            // フクキタル品番情報セット
            fukukitaruOrderModel.setFkItem(fkItemModel);
            // フクキタル発注SKUを取得し、モデルに設定する
            fukukitaruOrderComponent.setFOrderSkuEntityForModel(fukukitaruOrderModel);
            // 合計発注数を設定する
            fukukitaruOrderModel.setTotalOrderLot(fukukitaruOrderComponent.getTotalOrderLot(tfOrderEntity.getOrderType(), tfOrderEntity.getId()));

            listFukukitaruOrderModel.add(fukukitaruOrderModel);
        }

        return ListServiceResponse.<FukukitaruOrderModel>builder().items(listFukukitaruOrderModel).build();
    }
}

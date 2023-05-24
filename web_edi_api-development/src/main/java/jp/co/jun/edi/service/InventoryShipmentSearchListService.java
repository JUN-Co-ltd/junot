package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.extended.ExtendedTInventoryShipmentSearchResultEntity;
import jp.co.jun.edi.model.InventoryShipmentSearchConditionModel;
import jp.co.jun.edi.model.InventoryShipmentSearchResultModel;
import jp.co.jun.edi.repository.InventoryShipmentSearchResultCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基に在庫出荷情報を取得するサービス.
 */
@Service
public class InventoryShipmentSearchListService
extends GenericListService<ListServiceParameter<InventoryShipmentSearchConditionModel>,
ListServiceResponse<InventoryShipmentSearchResultModel>> {
    @Autowired
    private InventoryShipmentSearchResultCompositeRepository inventoryShipmentDeliveryStoreRepository;

    @Override
    protected ListServiceResponse<InventoryShipmentSearchResultModel>
    execute(final ListServiceParameter<InventoryShipmentSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<ExtendedTInventoryShipmentSearchResultEntity> page =
                inventoryShipmentDeliveryStoreRepository
                .findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        final List<InventoryShipmentSearchResultModel> items = new ArrayList<>();
        for (final ExtendedTInventoryShipmentSearchResultEntity entity : page) {
            // 納品依頼情報をコピー
            final InventoryShipmentSearchResultModel model
            = new InventoryShipmentSearchResultModel();
            BeanUtils.copyProperties(entity, model);
            BeanUtils.copyProperties(entity.getWReplenishmentShippingInstructionKey(),
                    model);
            items.add(model);
        }
        return ListServiceResponse.<InventoryShipmentSearchResultModel>builder()
                .nextPage(page.hasNext()).items(items).build();
    }
}

package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TProductionStatusHistoryEntity;
import jp.co.jun.edi.model.ProductionStatusHistoryModel;
import jp.co.jun.edi.model.ProductionStatusHistorySearchConditionModel;
import jp.co.jun.edi.repository.TProductionStatusHistoryRepository;
import jp.co.jun.edi.repository.specification.TProductStatusHistorySpecification;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 生産ステータス履歴取得処理.
 */
@Service
public class ProductionStatusHistoryListService extends
            GenericListService<ListServiceParameter<ProductionStatusHistorySearchConditionModel>, ListServiceResponse<ProductionStatusHistoryModel>> {

    @Autowired
    private TProductStatusHistorySpecification productHistoryStatusSpec;
    @Autowired
    private TProductionStatusHistoryRepository productionStatusHistoryRepository;

    @Override
    protected ListServiceResponse<ProductionStatusHistoryModel>
                    execute(final ListServiceParameter<ProductionStatusHistorySearchConditionModel> serviceParameter) {

        final List<ProductionStatusHistoryModel> items = new ArrayList<>();

        // TOrderSpecificationを利用して動的に条件文を生成し、データ取得する。
        for (final TProductionStatusHistoryEntity tProductionStatusHistoryEntity : productionStatusHistoryRepository.findAll(Specification
                .where(productHistoryStatusSpec.notDeleteContains())
                .and(productHistoryStatusSpec.orderIdContains(serviceParameter.getSearchCondition().getOrderId())),
                Sort.by(Order.desc("createdAt")))
                ) {

            final ProductionStatusHistoryModel productionStatusHistoryModel = new ProductionStatusHistoryModel();
            BeanUtils.copyProperties(tProductionStatusHistoryEntity, productionStatusHistoryModel);

            // レスポンスに返却する
            items.add(productionStatusHistoryModel);
        }

        return ListServiceResponse.<ProductionStatusHistoryModel>builder().items(items).build();
    }
}

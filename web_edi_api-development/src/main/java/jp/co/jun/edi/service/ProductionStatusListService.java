package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TProductionStatusEntity;
import jp.co.jun.edi.model.ProductionStatusModel;
import jp.co.jun.edi.model.ProductionStatusSearchConditionModel;
import jp.co.jun.edi.repository.TProductionStatusRepository;
import jp.co.jun.edi.repository.specification.TProductStatusSpecification;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 画面で指定された検索条件を基に生産ステータスを取得するサービス.
 */
@Service
@Slf4j
public class ProductionStatusListService extends
                GenericListService<ListServiceParameter<ProductionStatusSearchConditionModel>, ListServiceResponse<ProductionStatusModel>> {

    @Autowired
    private TProductStatusSpecification productStatusSpec;
    @Autowired
    private TProductionStatusRepository tProductionStatusRepository;


    @Override
    protected ListServiceResponse<ProductionStatusModel> execute(final ListServiceParameter<ProductionStatusSearchConditionModel> serviceParameter) {
        final List<ProductionStatusModel> items = new ArrayList<>();

        // 検索用Limit、Offset
        final Integer limit = 100;
        final Integer offset = 0;

        // TOrderSpecificationを利用して動的に条件文を生成し、データ取得する。
        for (final TProductionStatusEntity tProductionStatusEntity : tProductionStatusRepository.findAll(Specification
                .where(productStatusSpec.notDeleteContains())
                .and(productStatusSpec.orderIdContains(serviceParameter.getSearchCondition().getOrderId())),
                PageRequest.of(offset, limit, Sort.by(Order.asc("id"))))
                ) {

            final ProductionStatusModel productionStatusModeld = new ProductionStatusModel();
            BeanUtils.copyProperties(tProductionStatusEntity, productionStatusModeld);

            // レスポンスに返却する
            items.add(productionStatusModeld);
        }

        log.info(items.toString());

        return ListServiceResponse.<ProductionStatusModel>builder().items(items).build();
    }
}

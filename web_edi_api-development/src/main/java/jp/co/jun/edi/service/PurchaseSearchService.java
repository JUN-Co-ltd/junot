package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.PurchaseCompositeEntity;
import jp.co.jun.edi.model.PurchaseSearchConditionModel;
import jp.co.jun.edi.model.PurchaseSearchResultModel;
import jp.co.jun.edi.repository.PurchaseCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 指定された検索条件を基に仕入一覧用検索結果情報を取得するサービス.
 */
@Service
public class PurchaseSearchService
extends GenericListService<ListServiceParameter<PurchaseSearchConditionModel>, ListServiceResponse<PurchaseSearchResultModel>> {

    @Autowired
    private PurchaseCompositeRepository purchaseCompositeRepository;

    @Override
    protected ListServiceResponse<PurchaseSearchResultModel> execute(final ListServiceParameter<PurchaseSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<PurchaseCompositeEntity> page = purchaseCompositeRepository.findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        return ListServiceResponse.<PurchaseSearchResultModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(this::toModel).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param entity PurchaseCompositeEntity
     * @return PurchaseSearchResultModel
     */
    public PurchaseSearchResultModel toModel(final PurchaseCompositeEntity entity) {
        final PurchaseSearchResultModel model = new PurchaseSearchResultModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }
}

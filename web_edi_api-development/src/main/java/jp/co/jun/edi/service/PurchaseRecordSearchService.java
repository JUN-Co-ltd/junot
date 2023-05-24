//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.PurchaseRecordCompositeEntity;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;
import jp.co.jun.edi.model.PurchaseRecordSearchResultModel;
import jp.co.jun.edi.repository.PurchaseRecordCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 指定された検索条件を基に仕入実績一覧用検索結果情報を取得するサービス.
 */
@Service
public class PurchaseRecordSearchService
extends GenericListService<ListServiceParameter<PurchaseRecordSearchConditionModel>, ListServiceResponse<PurchaseRecordSearchResultModel>> {

    @Autowired
    private PurchaseRecordCompositeRepository purchaseRecordCompositeRepository;

    @Override
    protected ListServiceResponse<PurchaseRecordSearchResultModel> execute(final ListServiceParameter<PurchaseRecordSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<PurchaseRecordCompositeEntity> page = purchaseRecordCompositeRepository.findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        return ListServiceResponse.<PurchaseRecordSearchResultModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(this::toModel).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param entity PurchaseCompositeEntity
     * @return PurchaseSearchResultModel
     */
    public PurchaseRecordSearchResultModel toModel(final PurchaseRecordCompositeEntity entity) {
        final PurchaseRecordSearchResultModel model = new PurchaseRecordSearchResultModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }
}
//PRD_0133 #10181 add JFE end
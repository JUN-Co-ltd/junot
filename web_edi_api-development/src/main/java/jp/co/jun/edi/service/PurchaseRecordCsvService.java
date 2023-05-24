//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.PurchaseRecordCsvEntity;
import jp.co.jun.edi.model.PurchaseRecordCsvModel;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;
import jp.co.jun.edi.repository.PurchaseRecordCsvCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 指定された検索条件を基に仕入実績CSV用検索結果情報を取得するサービス.
 */
@Service
public class PurchaseRecordCsvService
extends GenericListService<ListServiceParameter<PurchaseRecordSearchConditionModel>, ListServiceResponse<PurchaseRecordCsvModel>> {

    @Autowired
    private PurchaseRecordCsvCompositeRepository purchaseRecordCsvCompositeRepository;

    @Override
    protected ListServiceResponse<PurchaseRecordCsvModel> execute(final ListServiceParameter<PurchaseRecordSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());
        final Page<PurchaseRecordCsvEntity> page = purchaseRecordCsvCompositeRepository.findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        return ListServiceResponse.<PurchaseRecordCsvModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(this::toModel).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param entity PurchaseCompositeEntity
     * @return PurchaseSearchResultModel
     */
    public PurchaseRecordCsvModel toModel(final PurchaseRecordCsvEntity entity) {
        final PurchaseRecordCsvModel model = new PurchaseRecordCsvModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }
}
//PRD_0133 #10181 add JFE end
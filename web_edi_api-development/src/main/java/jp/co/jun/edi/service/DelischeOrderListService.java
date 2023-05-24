package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DelischeComponent;
import jp.co.jun.edi.entity.VDelischeOrderEntity;
import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;
import jp.co.jun.edi.model.VDelischeOrderModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.VDelischeOrderRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 画面で指定された検索条件を基にデリスケ発注情報を取得するサービス.
 */
@Service
public class DelischeOrderListService extends GenericListService<ListServiceParameter<DelischeOrderSearchConditionModel>,
ListServiceResponse<VDelischeOrderModel>> {
    @Autowired
    private DelischeComponent delischeComponent;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Autowired
    private VDelischeOrderRepository vDelischeOrderRepository;

    @Override
    protected ListServiceResponse<VDelischeOrderModel> execute(final ListServiceParameter<DelischeOrderSearchConditionModel> serviceParameter) {

        final DelischeOrderSearchConditionModel searchCondition = serviceParameter.getSearchCondition();

        final List<String> brandCodeListFromDivision =
                mCodmstRepository.findBrandCodesByDivisionCode(MCodmstTblIdType.BRAND.getValue(), searchCondition.getDivisionCode());
        searchCondition.setBrandCodeListFromDivision(brandCodeListFromDivision);

        final Page<VDelischeOrderEntity> pageVDelischeOrder = vDelischeOrderRepository.findBySpec(searchCondition);
        final List<VDelischeOrderModel> vDelischeOrderModelList = delischeComponent.listDelischeOrder(pageVDelischeOrder);

        return ListServiceResponse.<VDelischeOrderModel>builder().nextPage(pageVDelischeOrder.hasNext()).items(vDelischeOrderModelList).build();
    }
}

package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliverySearchListComponent;
import jp.co.jun.edi.entity.TDeliverySearchResultEntity;
import jp.co.jun.edi.model.DeliverySearchListConditionModel;
import jp.co.jun.edi.model.DeliverySearchResultModel;
import jp.co.jun.edi.repository.TDeliverySearchListRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基に配分一覧(納品依頼情報)を取得するサービス.
 */
@Service
public class DeliverySearchListService extends GenericListService<ListServiceParameter<DeliverySearchListConditionModel>,
ListServiceResponse<DeliverySearchResultModel>> {
    @Autowired
    private DeliverySearchListComponent deliverySearchListComponent;

    @Autowired
    private TDeliverySearchListRepository deliverySearchListRepository;

    @Override
    protected ListServiceResponse<DeliverySearchResultModel> execute(final ListServiceParameter<DeliverySearchListConditionModel> serviceParameter) {

        final DeliverySearchListConditionModel searchCondition = serviceParameter.getSearchCondition();
        final CustomLoginUser loginUser = serviceParameter.getLoginUser();

        // メーカー権限の場合は仕入先の条件に会社コードをセット
        setMdfMakerCodeByCompany(searchCondition, loginUser);

        Page<TDeliverySearchResultEntity> pageDeliverySearchList = deliverySearchListRepository.findBySpec(searchCondition);
        final List<DeliverySearchResultModel> deliverySearchResultModelList = deliverySearchListComponent.listDeliverySearchResult(pageDeliverySearchList);

        return ListServiceResponse.<DeliverySearchResultModel>builder().nextPage(pageDeliverySearchList.hasNext()).items(deliverySearchResultModelList).build();
    }

    /**
     * 社外の場合、仕入先に会社コードを設定する.
     *
     * @param searchCondition 検索条件
     * @param loginUser ログインユーザー
     */
    private void setMdfMakerCodeByCompany(final DeliverySearchListConditionModel searchCondition, final CustomLoginUser loginUser) {
        if (!loginUser.isAffiliation()) {
            searchCondition.setMdfMakerCode(loginUser.getCompany());
        }
    }
}

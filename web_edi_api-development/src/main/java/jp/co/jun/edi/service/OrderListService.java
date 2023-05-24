package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.entity.DeliveryCompositeEntity;
import jp.co.jun.edi.entity.MFAvailableCompanyEntity;
import jp.co.jun.edi.entity.OrderCompositeEntity;
import jp.co.jun.edi.entity.TOrderFileInfoEntity;
import jp.co.jun.edi.model.OrderFileInfoModel;
import jp.co.jun.edi.model.OrderSearchConditionModel;
import jp.co.jun.edi.model.VDeliveryModel;
import jp.co.jun.edi.model.VOrderModel;
import jp.co.jun.edi.repository.DeliveryCompositeRepository;
import jp.co.jun.edi.repository.OrderCompositeRepository;
import jp.co.jun.edi.repository.TOrderFileInfoRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基に発注情報を取得するサービス.
 */
@Service
public class OrderListService extends GenericListService<ListServiceParameter<OrderSearchConditionModel>, ListServiceResponse<VOrderModel>> {
    @Autowired
    private OrderCompositeRepository orderCompositeRepository;

    @Autowired
    private TOrderFileInfoRepository tOrderFileInfoRepository;

    @Autowired
    private DeliveryCompositeRepository deliveryCompositeRepository;

    @Autowired
    private FukukitaruComponent fukukitaruComponent;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Override
    protected ListServiceResponse<VOrderModel> execute(final ListServiceParameter<OrderSearchConditionModel> serviceParameter) {
        final PageRequest pageable = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        // 発注一覧を取得する
        final Page<OrderCompositeEntity> page = orderCompositeRepository
                .findBySearchCondition(serviceParameter.getSearchCondition(),
                        loginUserComponent.getSupplierCode(serviceParameter.getLoginUser()), pageable);

        final List<OrderCompositeEntity> entities = page.getContent();

        // フクキタル資材発注利用可能ブランドリストを取得
        final List<MFAvailableCompanyEntity> listMFAvailableCompanyEntity = fukukitaruComponent
                .findByCompany(serviceParameter.getLoginUser().getCompany());

        // 発注IDリスト
        final List<BigInteger> orderIdList = new ArrayList<>(entities.size());

        // 発注情報リスト
        final List<VOrderModel> list = entities.stream().map(entity -> {
            final VOrderModel model = new VOrderModel();

            BeanUtils.copyProperties(entity, model);

            //フクキタル資材発注利用可能の判定結果を設定する
            model.setMaterialOrderAvailable(fukukitaruComponent.isMaterialOrderAvailable(listMFAvailableCompanyEntity, model.getBrandCode()));

            // 発注IDリストに発注ID追加
            orderIdList.add(entity.getId());

            return model;
        }).collect(Collectors.toList());

        // 発注検索結果がある場合(発注IDリストがある場合)
        if (CollectionUtils.isNotEmpty(orderIdList)) {
            // 発注ファイル情報リストを取得する
            final List<TOrderFileInfoEntity> orderFileInfoList = tOrderFileInfoRepository.findByOrderIdsAndExistsTfile(orderIdList);
            // 納品情報を取得する
            final List<DeliveryCompositeEntity> deliveryList = deliveryCompositeRepository.findByOrderIds(orderIdList);

            list.stream().map(order -> {
                // 発注ファイル情報をセットする
                final OrderFileInfoModel orderFileInfoModel = filterOrderFileInfo(order, orderFileInfoList);
                order.setOrderFileInfo(orderFileInfoModel);

                // 納品依頼情報Viewをセットする
                final List<VDeliveryModel> deliveryModelList = filterDeliveries(order, deliveryList);
                order.setDeliverys(deliveryModelList);
                return order;
            }).collect(Collectors.toList());
        }

        return ListServiceResponse.<VOrderModel>builder().nextPage(page.hasNext()).items(list).build();
    }

    /**
     * 発注IDに紐づく発注ファイル情報をフィルターする.
     * @param vOrderModel 発注情報Model
     * @param orderFileInfoList 発注ファイル情報リスト
     * @return 発注ファイル情報Model
     */
    private OrderFileInfoModel filterOrderFileInfo(final VOrderModel vOrderModel,
            final List<TOrderFileInfoEntity> orderFileInfoList) {
        OrderFileInfoModel filteredOrderFileInfo = new OrderFileInfoModel();

        final TOrderFileInfoEntity orderFileInfo = orderFileInfoList.stream()
                .filter(file -> file.getOrderId().equals(vOrderModel.getId()))
                .findFirst()
                .orElse(null);

        if (orderFileInfo != null) {
            BeanUtils.copyProperties(orderFileInfo, filteredOrderFileInfo);
        }
        return filteredOrderFileInfo;
    }

    /**
     * 発注IDに紐づく納品依頼情報をフィルターする.
     * @param vOrderModel 発注情報Model
     * @param deliveryList 納品依頼情報リスト
     * @return 納品依頼情報Modelのリスト
     */
    private List<VDeliveryModel> filterDeliveries(final VOrderModel vOrderModel,
            final List<DeliveryCompositeEntity> deliveryList) {

        List<VDeliveryModel> filteredDeliveryList = deliveryList.stream()
                .filter(vDelivery -> vDelivery.getOrderId().equals(vOrderModel.getId()))
                .map(delivery -> {
                    final VDeliveryModel vDeliveryModel = new VDeliveryModel();
                    BeanUtils.copyProperties(delivery, vDeliveryModel);
                    return vDeliveryModel;
                }).collect(Collectors.toList());

        return filteredDeliveryList;
    }
}

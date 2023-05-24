package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TDeliveryPlanEntity;
import jp.co.jun.edi.model.DeliveryPlanCutModel;
import jp.co.jun.edi.model.DeliveryPlanDetailModel;
import jp.co.jun.edi.model.DeliveryPlanModel;
import jp.co.jun.edi.model.DeliveryPlanSearchConditionModel;
import jp.co.jun.edi.model.DeliveryPlanSkuModel;
import jp.co.jun.edi.repository.TDeliveryPlanCutRepository;
import jp.co.jun.edi.repository.TDeliveryPlanDetailRepository;
import jp.co.jun.edi.repository.TDeliveryPlanRepository;
import jp.co.jun.edi.repository.TDeliveryPlanSkuRepository;
import jp.co.jun.edi.repository.specification.TDeliveryPlanSpecification;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基に納品予定情報を取得するサービス.
 */
@Service
public class DeliveryPlanListService
extends GenericListService<ListServiceParameter<DeliveryPlanSearchConditionModel>, ListServiceResponse<DeliveryPlanModel>> {

    @Autowired
    private TDeliveryPlanRepository deliveryPlanRepository;
    @Autowired
    private TDeliveryPlanSpecification deliveryPlanSpec;
    @Autowired
    private TDeliveryPlanSkuRepository deliveryPlanSkuRepository;
    @Autowired
    private TDeliveryPlanDetailRepository deliveryPlanDetailRepository;
    @Autowired
    private TDeliveryPlanCutRepository deliveryPlanCutRepository;

    @Override
    protected ListServiceResponse<DeliveryPlanModel> execute(final ListServiceParameter<DeliveryPlanSearchConditionModel> serviceParameter) {

        final List<DeliveryPlanModel> items = new ArrayList<>();

        Page<TDeliveryPlanEntity> pageDeliveryPlan = findTDeliveryPlan(serviceParameter);

        for (final TDeliveryPlanEntity deliveryPlanEntity : pageDeliveryPlan) {

            // 納品依頼情報をコピー
            final DeliveryPlanModel deliveryPlanModel = new DeliveryPlanModel();
            BeanUtils.copyProperties(deliveryPlanEntity, deliveryPlanModel);

            // 納品予定明細・納品予定SKUを取得
            deliveryPlanModel.setDeliveryPlanDetails(findTDeliveryPlanDetail(deliveryPlanEntity.getId()));

            // 納品予定裁断を取得
            deliveryPlanModel.setDeliveryPlanCuts(findTDeliveryPlanCut(deliveryPlanEntity.getId()));

            // レスポンスに返却する
            items.add(deliveryPlanModel);
        }
        return ListServiceResponse.<DeliveryPlanModel>builder().nextPage(pageDeliveryPlan.hasNext()).items(items).build();
    }


    /**
     * 納品予定を取得する.
     * @param serviceParameter 検索条件
     * @return 納品予定の配列
     */
    private Page<TDeliveryPlanEntity> findTDeliveryPlan(final ListServiceParameter<DeliveryPlanSearchConditionModel> serviceParameter) {

        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults(),
                Sort.by(Order.asc("id")));

        // 納品予定明細情報を取得する
        return deliveryPlanRepository.findAll(Specification
                .where(deliveryPlanSpec.notDeleteContains())
                .and(deliveryPlanSpec.orderIdContains(serviceParameter.getSearchCondition().getOrderId())),
                pageRequest
                );
    }


    /**
     * 納品予定裁断を取得する.
     * @param deliveryPlanId 納品予定ID
     * @return 納品予定裁断の配列
     */
    private List<DeliveryPlanCutModel> findTDeliveryPlanCut(final BigInteger deliveryPlanId) {

        // 納品予定裁断情報を取得する
        return deliveryPlanCutRepository.findByDeliveryPlanId(
                deliveryPlanId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).stream()
                .map(tDeliveryPlanCut -> {
                    final DeliveryPlanCutModel deliveryPlanCut = new DeliveryPlanCutModel();

                    // 納品予定裁断情報のコピー
                    BeanUtils.copyProperties(tDeliveryPlanCut, deliveryPlanCut);

                    return deliveryPlanCut;
                }).collect(Collectors.toList());
    }

    /**
     * 納品予定明細を取得する.
     * @param deliveryPlanId 納品予定ID
     * @return 納品予定明細の配列
     */
    private List<DeliveryPlanDetailModel> findTDeliveryPlanDetail(final BigInteger deliveryPlanId) {

        // 納品予定明細情報を取得する
        return deliveryPlanDetailRepository.findByDeliveryPlanId(
                deliveryPlanId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).stream()
                .map(tDeliveryPlanDetail -> {
                    final DeliveryPlanDetailModel deliveryPlanDetail = new DeliveryPlanDetailModel();

                    // 納品SKU情報のコピー
                    BeanUtils.copyProperties(tDeliveryPlanDetail, deliveryPlanDetail);

                    // 納品予定SKUを取得
                    deliveryPlanDetail.setDeliveryPlanSkus(findTDeliveryPlanSku(tDeliveryPlanDetail.getId()));

                    return deliveryPlanDetail;
                }).collect(Collectors.toList());
    }

    /**
     * 納品予定SKUを取得する.
     *
     * @param deliveryPlanDetailId 納品予定明細ID
     * @return 納品予定SKUの配列
     */
    private List<DeliveryPlanSkuModel> findTDeliveryPlanSku(final BigInteger deliveryPlanDetailId) {

      // 納品予定SKU情報を取得する
      return deliveryPlanSkuRepository.findByDeliveryPlanDetailId(
                deliveryPlanDetailId,
              PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("id")))).stream()
              .map(tDeliveryPlanSku -> {
                  final DeliveryPlanSkuModel deliveryPlanSku = new DeliveryPlanSkuModel();

                  // 納品SKU情報のコピー
                  BeanUtils.copyProperties(tDeliveryPlanSku, deliveryPlanSku);

                  return deliveryPlanSku;
              }).collect(Collectors.toList());
    }


}

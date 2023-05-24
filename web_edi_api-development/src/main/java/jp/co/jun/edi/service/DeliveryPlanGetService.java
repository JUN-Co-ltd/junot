package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TDeliveryPlanEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryPlanCutModel;
import jp.co.jun.edi.model.DeliveryPlanDetailModel;
import jp.co.jun.edi.model.DeliveryPlanModel;
import jp.co.jun.edi.model.DeliveryPlanSkuModel;
import jp.co.jun.edi.repository.TDeliveryPlanCutRepository;
import jp.co.jun.edi.repository.TDeliveryPlanDetailRepository;
import jp.co.jun.edi.repository.TDeliveryPlanRepository;
import jp.co.jun.edi.repository.TDeliveryPlanSkuRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品予定を取得するサービス.
 *
 */
@Service
public class DeliveryPlanGetService
extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<DeliveryPlanModel>> {

    @Autowired
    private TDeliveryPlanRepository tDeliveryPlanRepository;

    @Autowired
    private TDeliveryPlanSkuRepository tDeliveryPlanSkuRepository;

    @Autowired
    private TDeliveryPlanDetailRepository tDeliveryPlanDetailRepository;

    @Autowired
    private TDeliveryPlanCutRepository tDeliveryPlanCutRepository;

    @Override
    protected GetServiceResponse<DeliveryPlanModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        // 納品予定情報を取得し、データが存在しない場合は例外を投げる
        final TDeliveryPlanEntity tDeliveryPlanEntity = tDeliveryPlanRepository.findById(serviceParameter.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final DeliveryPlanModel deliveryPlanModel = new DeliveryPlanModel();

        BeanUtils.copyProperties(tDeliveryPlanEntity, deliveryPlanModel);

        // 納品予定明細を取得する
        deliveryPlanModel.setDeliveryPlanDetails(tDeliveryPlanDetailRepository.findByDeliveryPlanId(
                tDeliveryPlanEntity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(tDeliveryPlanDetail -> {
                    final DeliveryPlanDetailModel deliveryPlanDetailModel = new DeliveryPlanDetailModel();

                    // 納品予定明細のコピー
                    BeanUtils.copyProperties(tDeliveryPlanDetail, deliveryPlanDetailModel);

                    // 納品予定SKU情報を取得する
                    deliveryPlanDetailModel.setDeliveryPlanSkus(tDeliveryPlanSkuRepository.findByDeliveryPlanDetailId(
                            tDeliveryPlanDetail.getId(),
                            PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("colorCode")))).stream()
                            .map(tDeliveryPlanSku -> {
                                final DeliveryPlanSkuModel deliveryPlanSkuModel = new DeliveryPlanSkuModel();

                                // 納品予定SKUのコピー
                                BeanUtils.copyProperties(tDeliveryPlanSku, deliveryPlanSkuModel);

                                return deliveryPlanSkuModel;
                            }).collect(Collectors.toList()));

                    return deliveryPlanDetailModel;
                }).collect(Collectors.toList()));

        // 納品予定裁断を取得する
        deliveryPlanModel.setDeliveryPlanCuts(tDeliveryPlanCutRepository.findByDeliveryPlanId(tDeliveryPlanEntity.getId(),
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("colorCode")))).stream()
                .map(tDeliveryPlanCut -> {
                    final DeliveryPlanCutModel deliveryPlanCutModel = new DeliveryPlanCutModel();

                    // 納品予定裁断のコピー
                    BeanUtils.copyProperties(tDeliveryPlanCut, deliveryPlanCutModel);

                    return deliveryPlanCutModel;
                }).collect(Collectors.toList()));


        return GetServiceResponse.<DeliveryPlanModel>builder().item(deliveryPlanModel).build();
    }
}

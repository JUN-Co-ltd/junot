package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.entity.TOrderFileInfoEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.OrderFileInfoModel;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.OrderSkuModel;
import jp.co.jun.edi.repository.TOrderFileInfoRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderSkuRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 発注情報を取得するサービス.
 *
 */
@Service
public class OrderGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<OrderModel>> {
    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;

    @Autowired
    private ExtendedTOrderSkuRepository extendedTOrderSkuRepository;

    @Autowired
    private TOrderFileInfoRepository tOrderFileInfoRepository;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private LoginUserComponent loginUserComponent;

    private static final int PART_NO_KIND_LENGHT = 3;

    @Override
    protected GetServiceResponse<OrderModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        // 発注情報を取得し、データが存在しない場合は例外を投げる
        final ExtendedTOrderEntity extendedTOrderEntity = extendedTOrderRepository.findById(serviceParameter.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // メーカー権限チェック
        loginUserComponent.validateSupplierAuthority(serviceParameter.getLoginUser(), extendedTOrderEntity.getMdfMakerCode());

        final OrderModel orderModel = new OrderModel();

        BeanUtils.copyProperties(extendedTOrderEntity, orderModel);

        // 読み取り専用設定
        orderModel.setReadOnly(orderComponent.isReadOnly(extendedTOrderEntity.getExpenseItem()));

        // 発注SKU情報を取得する
        orderModel.setOrderSkus(extendedTOrderSkuRepository.findByOrderId(
                extendedTOrderEntity.getId(),
                StringUtils.left(extendedTOrderEntity.getPartNo(), PART_NO_KIND_LENGHT),
                PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(tOrderSku -> {
                    final OrderSkuModel orderSku = new OrderSkuModel();

                    // 発注SKU情報のコピー
                    BeanUtils.copyProperties(tOrderSku, orderSku);

                    return orderSku;
                }).collect(Collectors.toList()));

        // 発注ファイル情報を取得する
        final List<TOrderFileInfoEntity> tOrderFileInfoEntityList = tOrderFileInfoRepository
                .findByOrderIdAndExistsTfile(extendedTOrderEntity.getId(), PageRequest.of(0, 1, Sort.by(Order.desc("id")))).getContent();
        TOrderFileInfoEntity tOrderFileInfoEntity = null;
        if (tOrderFileInfoEntityList.size() > 0) {
            tOrderFileInfoEntity = tOrderFileInfoEntityList.get(0);
        }

        final OrderFileInfoModel orderFileInfoModel = new OrderFileInfoModel();
        if (tOrderFileInfoEntity != null) {
            BeanUtils.copyProperties(tOrderFileInfoEntity, orderFileInfoModel);
        }
        orderModel.setOrderFileInfo(orderFileInfoModel);

        return GetServiceResponse.<OrderModel>builder().item(orderModel).build();
    }
}

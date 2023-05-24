package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TProductionStatusEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ProductionStatusModel;
import jp.co.jun.edi.repository.TProductionStatusRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 生産ステータス取得処理.
 */
@Service
public class ProductionStatusGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<ProductionStatusModel>> {

    @Autowired
    private TProductionStatusRepository productionStatusRepository;

    @Override
    protected GetServiceResponse<ProductionStatusModel> execute(
           final GetServiceParameter<BigInteger> serviceParameter) {

        // 生産ステータスを取得し、データが存在しない場合は例外を投げる
        final TProductionStatusEntity  entity = productionStatusRepository.findById(serviceParameter.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final ProductionStatusModel item = new ProductionStatusModel();

        BeanUtils.copyProperties(entity, item);

        return GetServiceResponse.<ProductionStatusModel>builder().item(item).build();
    }
}

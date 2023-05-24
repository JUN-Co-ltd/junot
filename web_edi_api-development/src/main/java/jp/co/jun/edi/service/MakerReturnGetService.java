package jp.co.jun.edi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.model.MakerReturnKeyModel;
import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.MakerReturnModel;
import jp.co.jun.edi.model.MakerReturnProductCompositeModel;
import jp.co.jun.edi.repository.extended.ExtendedTMakerReturnRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * メーカー返品情報を取得するサービス.
 */
@Service
public class MakerReturnGetService extends GenericGetService<GetServiceParameter<MakerReturnKeyModel>, GetServiceResponse<MakerReturnModel>> {

    @Autowired
    private ExtendedTMakerReturnRepository extendedMakerReturnRepository;

    @Override
    protected GetServiceResponse<MakerReturnModel> execute(final GetServiceParameter<MakerReturnKeyModel> serviceParameter) {
        final MakerReturnKeyModel key = serviceParameter.getId();
        final List<ExtendedTMakerReturnEntity> list = extendedMakerReturnRepository.findByVoucherNumberAndOrderId(key.getVoucherNumber(), key.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final MakerReturnModel res = toMakerReturnModel(list);

        return GetServiceResponse.<MakerReturnModel>builder().item(res).build();
    }

    /**
     * @param resultList 取得結果リスト
     * @return MakerReturnModel
     */
    private MakerReturnModel toMakerReturnModel(final List<ExtendedTMakerReturnEntity> resultList) {
        final MakerReturnModel res = new MakerReturnModel();

        // 共通項目コピー
        BeanUtils.copyProperties(resultList.get(0), res);

        // メーカー返品商品情報
        final List<MakerReturnProductCompositeModel> makerReturnProducts = resultList
                .stream()
                .map(this::toMakerReturnProductCompositeModel)
                .collect(Collectors.toList());
        res.setMakerReturnProducts(makerReturnProducts);

        return res;
    }

    /**
     * @param entity 処理中のメーカー返品情報
     * @return MakerReturnProductCompositeModel
     */
    private MakerReturnProductCompositeModel toMakerReturnProductCompositeModel(final ExtendedTMakerReturnEntity entity) {
        final MakerReturnProductCompositeModel model = new MakerReturnProductCompositeModel();

        // メーカー返品商品情報リストコピー
        BeanUtils.copyProperties(entity, model);

        return model;
    }
}

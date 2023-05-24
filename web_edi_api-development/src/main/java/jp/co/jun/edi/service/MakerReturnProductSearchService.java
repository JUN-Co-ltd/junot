package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MakerReturnProductCompositeEntity;
import jp.co.jun.edi.entity.key.MakerReturnProductCompositeKey;
import jp.co.jun.edi.model.MakerReturnProductCompositeModel;
import jp.co.jun.edi.model.MakerReturnProductSearchConditionModel;
import jp.co.jun.edi.repository.MakerReturnProductCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基にメーカー返品商品情報を取得するサービス.
 */
@Service
public class MakerReturnProductSearchService
extends GenericListService<ListServiceParameter<MakerReturnProductSearchConditionModel>, ListServiceResponse<MakerReturnProductCompositeModel>> {

    @Autowired
    private MakerReturnProductCompositeRepository repository;

    @Override
    protected ListServiceResponse<MakerReturnProductCompositeModel> execute(
            final ListServiceParameter<MakerReturnProductSearchConditionModel> serviceParameter) {

        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<MakerReturnProductCompositeEntity> page = repository.findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        return ListServiceResponse.<MakerReturnProductCompositeModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(this::toModel).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param entity MakerReturnProductCompositeEntity
     * @return model MakerReturnProductCompositeModel
     */
    private MakerReturnProductCompositeModel toModel(final MakerReturnProductCompositeEntity entity) {
        final MakerReturnProductCompositeModel model = new MakerReturnProductCompositeModel();

        BeanUtils.copyProperties(entity, model);

        final MakerReturnProductCompositeKey key = entity.getKey();
        model.setOrderId(key.getOrderId());
        model.setColorCode(key.getColorCode());
        model.setSize(key.getSize());

        return model;
    }
}

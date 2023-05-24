package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MakerReturnCompositeEntity;
import jp.co.jun.edi.model.MakerReturnModel;
import jp.co.jun.edi.model.MakerReturnSearchResultModel;
import jp.co.jun.edi.repository.MakerReturnCompositeRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基にメーカ返品一覧を取得するサービス.
 */
@Service
public class MakerReturnSearchService extends GenericListService<ListServiceParameter<MakerReturnSearchResultModel>,
ListServiceResponse<MakerReturnModel>> {

    @Autowired
    private MakerReturnCompositeRepository makerReturnCompositeRepository;

    @Override
    protected ListServiceResponse<MakerReturnModel> execute(final ListServiceParameter<MakerReturnSearchResultModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<MakerReturnCompositeEntity> page = makerReturnCompositeRepository.findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        return ListServiceResponse.<MakerReturnModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(this::toModel).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param entity PurchaseCompositeEntity
     * @return PurchaseSearchResultModel
     */
    public MakerReturnModel toModel(final MakerReturnCompositeEntity entity) {
        final MakerReturnModel model = new MakerReturnModel();
        BeanUtils.copyProperties(entity, model);

        model.setOrderId(entity.getKey().getOrderId());
        model.setVoucherNumber(entity.getKey().getVoucherNumber());

        return model;
    }
}

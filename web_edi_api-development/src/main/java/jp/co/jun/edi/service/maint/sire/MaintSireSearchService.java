package jp.co.jun.edi.service.maint.sire;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.master.SireEntity;
import jp.co.jun.edi.model.maint.MaintSireSearchConditionModel;
import jp.co.jun.edi.model.maint.MaintSireSearchResultModel;
import jp.co.jun.edi.repository.master.SireRepository;
import jp.co.jun.edi.service.GenericListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基に取引先一覧を取得するサービス.
 */
@Service
public class MaintSireSearchService extends GenericListService<ListServiceParameter<MaintSireSearchConditionModel>,
ListServiceResponse<MaintSireSearchResultModel>> {

    @Autowired
    private SireRepository sireRepository;

    @Override
    protected ListServiceResponse<MaintSireSearchResultModel> execute(final ListServiceParameter<MaintSireSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<SireEntity> page = sireRepository.findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        return ListServiceResponse.<MaintSireSearchResultModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(this::toModel).collect(Collectors.toList()))
                .build();
    }

    /**
     * @param entity SireEntity
     * @return MaintSireSearchResultModel
     */
    public MaintSireSearchResultModel toModel(final SireEntity entity) {
        final MaintSireSearchResultModel model = new MaintSireSearchResultModel();
        BeanUtils.copyProperties(entity, model);

        return model;
    }
}
package jp.co.jun.edi.service.maint.news;

import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.model.maint.MaintNewsSearchConditionModel;
import jp.co.jun.edi.model.maint.MaintNewsSearchResultModel;
import jp.co.jun.edi.repository.TNewsRepository;
import jp.co.jun.edi.service.GenericListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基にマスタメンテナンス用のお知らせ情報を取得するサービス.
 */
@Service
public class MaintNewsSearchService
    extends GenericListService<ListServiceParameter<MaintNewsSearchConditionModel>, ListServiceResponse<MaintNewsSearchResultModel>> {

    @Autowired
    private TNewsRepository tNewsRepository;

    @Override
    protected ListServiceResponse<MaintNewsSearchResultModel> execute(final ListServiceParameter<MaintNewsSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<TNewsEntity> page = tNewsRepository.findByDeletedAtIsNullOrderByOpenStartAtDesc(pageRequest);

        return ListServiceResponse.<MaintNewsSearchResultModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(entity -> {
                    final MaintNewsSearchResultModel model = new MaintNewsSearchResultModel();

                    BeanUtils.copyProperties(entity, model);

                    return model;
                }).collect(Collectors.toList()))
                .build();
    }
}

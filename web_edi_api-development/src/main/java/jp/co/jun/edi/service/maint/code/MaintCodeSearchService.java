package jp.co.jun.edi.service.maint.code;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintCodeComponent;
import jp.co.jun.edi.entity.MScreenStructureEntity;
import jp.co.jun.edi.service.GenericListService;
import jp.co.jun.edi.service.parameter.MaintCodeSearchServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基にメンテナンスコード情報を取得するサービス.
 */
@Service
public class MaintCodeSearchService extends GenericListService<MaintCodeSearchServiceParameter, ListServiceResponse<Map<String, Object>>> {

    @Autowired
    private MaintCodeComponent maintCodeComponent;

    @Override
    protected ListServiceResponse<Map<String, Object>> execute(final MaintCodeSearchServiceParameter serviceParameter) {
        // 画面構成情報取得
        final MScreenStructureEntity entity = maintCodeComponent.getMScreenStructureEntity(serviceParameter.getTblId());

        // 検索
        final PageRequest pageRequest = PageRequest.of(serviceParameter.getSearchCondition().getPage(), serviceParameter.getSearchCondition().getMaxResults());
        final Page<Map<String, Object>> page = maintCodeComponent.maintCodeSearch(entity, serviceParameter.getSearchCondition(),
                pageRequest);

        return ListServiceResponse.<Map<String, Object>>builder().nextPage(page.hasNext()).revisionedAt(entity.getUpdatedAt()).items(page.getContent()).build();
    }
}

package jp.co.jun.edi.service.maint.user;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintUserComponent;
import jp.co.jun.edi.entity.master.UserEntity;
import jp.co.jun.edi.model.maint.MaintUserSearchConditionModel;
import jp.co.jun.edi.model.maint.MaintUserSearchResultModel;
import jp.co.jun.edi.repository.master.UserRepository;
import jp.co.jun.edi.service.GenericListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基にマスタメンテナンス用のユーザ情報を取得するサービス.
 */
@Service
public class MaintUserSearchService
        extends GenericListService<ListServiceParameter<MaintUserSearchConditionModel>, ListServiceResponse<MaintUserSearchResultModel>> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaintUserComponent maintUserComponent;

    @Override
    protected ListServiceResponse<MaintUserSearchResultModel> execute(final ListServiceParameter<MaintUserSearchConditionModel> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        final Page<UserEntity> page = userRepository.findBySearchCondition(serviceParameter.getSearchCondition(), pageRequest);

        return ListServiceResponse.<MaintUserSearchResultModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(entity -> {
                    final MaintUserSearchResultModel model = new MaintUserSearchResultModel();

                    // エンティティからモデルへコピー
                    maintUserComponent.copyEntityToModel(entity, model);

                    return model;
                }).collect(Collectors.toList()))
                .build();
    }
}

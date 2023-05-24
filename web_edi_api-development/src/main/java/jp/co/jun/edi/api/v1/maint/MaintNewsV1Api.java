package jp.co.jun.edi.api.v1.maint;

import java.math.BigInteger;

import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.maint.MaintNewsModel;
import jp.co.jun.edi.model.maint.MaintNewsSearchConditionModel;
import jp.co.jun.edi.model.maint.MaintNewsSearchResultModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.maint.news.MaintNewsCreateService;
import jp.co.jun.edi.service.maint.news.MaintNewsDeleteService;
import jp.co.jun.edi.service.maint.news.MaintNewsGetService;
import jp.co.jun.edi.service.maint.news.MaintNewsSearchService;
import jp.co.jun.edi.service.maint.news.MaintNewsUpdateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * マスタメンテナンス用のお知らせ情報API.
 * <p>
 * JUN権限のみでも操作できるように権限を解放。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/maint/news")
@PreAuthorize("hasRole('ROLE_JUN')")
public class MaintNewsV1Api {
    @Autowired
    private MaintNewsCreateService createService;

    @Autowired
    private MaintNewsDeleteService deleteService;

    @Autowired
    private MaintNewsGetService getService;

    @Autowired
    private MaintNewsSearchService searchService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    @Autowired
    private MaintNewsUpdateService updateService;

    /**
     * お知らせ情報を作成する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link MaintNewsModel} instance
     * @return {@link MaintNewsModel} instance
     */
    @PostMapping
    public MaintNewsModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({CreateValidationGroup.class, Default.class}) final MaintNewsModel item) {
        return createService.call(CreateServiceParameter.<MaintNewsModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * お知らせ情報を削除する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param id ID
     */
    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("id") final BigInteger id) {
        deleteService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(id).build());

        return;
    }

    /**
     * お知らせ情報を取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param id ID
     * @return {@link MaintNewsModel} instance
     */
    @GetMapping("/{id}")
    public MaintNewsModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("id") final BigInteger id) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(id).build()).getItem();
    }

    /**
     * お知らせ情報を検索する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link MaintNewsSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<MaintNewsSearchResultModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(Default.class) final MaintNewsSearchConditionModel searchCondition) {
        final MaintNewsSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, MaintNewsSearchConditionModel.class);

        final ListServiceResponse<MaintNewsSearchResultModel> serviceResponse = searchService
                .call(ListServiceParameter.<MaintNewsSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<MaintNewsSearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * お知らせ情報を更新する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param id ID
     * @param item {@link MaintNewsModel} instance
     * @return {@link MaintNewsModel} instance
     */
    @PutMapping("/{id}")
    public MaintNewsModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("id") final BigInteger id,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final MaintNewsModel item) {
        item.setId(id);

        return updateService.call(UpdateServiceParameter.<MaintNewsModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }
}

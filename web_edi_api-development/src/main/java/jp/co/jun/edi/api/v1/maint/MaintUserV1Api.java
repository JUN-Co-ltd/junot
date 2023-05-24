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
import jp.co.jun.edi.model.maint.MaintUserModel;
import jp.co.jun.edi.model.maint.MaintUserSearchConditionModel;
import jp.co.jun.edi.model.maint.MaintUserSearchResultModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.maint.user.MaintUserCreateService;
import jp.co.jun.edi.service.maint.user.MaintUserDeleteService;
import jp.co.jun.edi.service.maint.user.MaintUserGetService;
import jp.co.jun.edi.service.maint.user.MaintUserSearchService;
import jp.co.jun.edi.service.maint.user.MaintUserUpdateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * マスタメンテナンス用のユーザ情報API.
 */
@RestController
@RequestMapping("/api/v1/maint/users")
@PreAuthorize("hasRole('ROLE_JUN') and hasRole('ROLE_ADMIN')")
public class MaintUserV1Api {
    @Autowired
    private MaintUserCreateService createService;

    @Autowired
    private MaintUserDeleteService deleteService;

    @Autowired
    private MaintUserGetService getService;

    @Autowired
    private MaintUserSearchService searchService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    @Autowired
    private MaintUserUpdateService updateService;

    /**
     * ユーザ情報を作成する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link MaintUserModel} instance
     * @return {@link MaintUserModel} instance
     */
    @PostMapping
    public MaintUserModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({CreateValidationGroup.class, Default.class}) final MaintUserModel item) {
        return createService.call(CreateServiceParameter.<MaintUserModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * ユーザ情報を削除する.
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
     * ユーザ情報を取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param id ID
     * @return {@link MaintUserModel} instance
     */
    @GetMapping("/{id}")
    public MaintUserModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("id") final BigInteger id) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(id).build()).getItem();
    }

    /**
     * ユーザ情報を検索する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link MaintUserSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<MaintUserSearchResultModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(Default.class) final MaintUserSearchConditionModel searchCondition) {
        final MaintUserSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, MaintUserSearchConditionModel.class);

        final ListServiceResponse<MaintUserSearchResultModel> serviceResponse = searchService
                .call(ListServiceParameter.<MaintUserSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<MaintUserSearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * ユーザ情報を更新する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param id ID
     * @param item {@link MaintUserModel} instance
     * @return {@link MaintUserModel} instance
     */
    @PutMapping("/{id}")
    public MaintUserModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("id") final BigInteger id,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final MaintUserModel item) {
        item.setId(id);

        return updateService.call(UpdateServiceParameter.<MaintUserModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }
}

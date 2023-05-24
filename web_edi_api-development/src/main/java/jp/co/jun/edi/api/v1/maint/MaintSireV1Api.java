package jp.co.jun.edi.api.v1.maint;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.component.model.MaintSireKeyModel;
import jp.co.jun.edi.component.model.MaintSireReckbnKeyModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.maint.MaintSireModel;
import jp.co.jun.edi.model.maint.MaintSireSearchConditionModel;
import jp.co.jun.edi.model.maint.MaintSireSearchResultModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.maint.sire.MaintSireCreateService;
import jp.co.jun.edi.service.maint.sire.MaintSireDeleteService;
import jp.co.jun.edi.service.maint.sire.MaintSireGetService;
//import jp.co.jun.edi.service.maint.sire.MaintSireCreateService;
//import jp.co.jun.edi.service.maint.sire.MaintSireDeleteService;
import jp.co.jun.edi.service.maint.sire.MaintSireSearchService;
import jp.co.jun.edi.service.maint.sire.MaintSireUpdateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * マスタメンテナンス用の取引先情報API.
 */
@RestController
@RequestMapping("/api/v1/maint/sires")
@PreAuthorize("hasRole('ROLE_JUN') and hasRole('ROLE_ADMIN')")
public class MaintSireV1Api {
    @Autowired
    private MaintSireCreateService createService;

    @Autowired
    private MaintSireDeleteService deleteService;

    @Autowired
    private MaintSireGetService getService;

    @Autowired
    private MaintSireSearchService searchService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    @Autowired
    private MaintSireUpdateService updateService;

    /**
     * 取引先情報を作成する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link MaintSireModel} instance
     * @return {@link MaintSireModel} instance
     */
    @PostMapping
    public MaintSireModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({CreateValidationGroup.class, Default.class}) final MaintSireModel item) {
        return createService.call(CreateServiceParameter.<MaintSireModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 取引先情報を削除する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param sireCode 仕入先コード
     * @param kojCode 工場コード
     */
    @DeleteMapping("/{sireCode}")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("sireCode") final String sireCode,
            @RequestParam final String kojCode, String reckbn) {
    	final MaintSireReckbnKeyModel key = new MaintSireReckbnKeyModel(sireCode, kojCode, reckbn);
        deleteService.call(DeleteServiceParameter.<MaintSireReckbnKeyModel>builder().loginUser(loginUser).id(key).build());

        return;
    }

    /**
     * 取引先情報を検索する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link MaintSireSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<MaintSireSearchResultModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(Default.class) final MaintSireSearchConditionModel searchCondition) {
        final MaintSireSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, MaintSireSearchConditionModel.class);

        final ListServiceResponse<MaintSireSearchResultModel> serviceResponse = searchService
                .call(ListServiceParameter.<MaintSireSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<MaintSireSearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * 取引先情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param sireCode 仕入先コード
     * @param kojCode 工場コード
     * @return {@link ItemModel} instance
     */
    @GetMapping("/{sireCode}")
    public MaintSireModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("sireCode") final String sireCode,
            @RequestParam final String kojCode) {
        final MaintSireKeyModel key = new MaintSireKeyModel(sireCode, kojCode);
        return getService.call(GetServiceParameter.<MaintSireKeyModel>builder().loginUser(loginUser).id(key).build()).getItem();
    }

    /**
     * 取引先情報を更新する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param sireCode 仕入先コード
     * @param item {@link MaintSireModel} instance
     * @return {@link MaintSireModel} instance
     */
    @PutMapping("/{sireCode}")
    public MaintSireModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("sireCode") final String sireCode,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final MaintSireModel item) {
        return updateService.call(UpdateServiceParameter.<MaintSireModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }
}

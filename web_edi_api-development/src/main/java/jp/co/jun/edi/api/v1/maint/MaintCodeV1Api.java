package jp.co.jun.edi.api.v1.maint;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.GenericItemModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.maint.code.MaintCodeBulkUpdateModel;
import jp.co.jun.edi.model.maint.code.MaintCodeBulkResponseModel;
import jp.co.jun.edi.model.maint.code.MaintCodeListModel;
import jp.co.jun.edi.model.maint.code.MaintCodeScreenSettingModel;
import jp.co.jun.edi.model.maint.code.MaintCodeSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.maint.code.MaintCodeBulkDeleteService;
import jp.co.jun.edi.service.maint.code.MaintCodeBulkUpdateService;
import jp.co.jun.edi.service.maint.code.MaintCodeListService;
import jp.co.jun.edi.service.maint.code.MaintCodeScreenSettingService;
import jp.co.jun.edi.service.maint.code.MaintCodeSearchService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.MaintCodeBulkUpdateServiceParameter;
import jp.co.jun.edi.service.parameter.MaintCodeSearchServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * マスタメンテナンス用のコード情報API.
 */
@RestController
@RequestMapping("/api/v1/maint/maint-code")
@PreAuthorize("hasRole('ROLE_JUN') and hasRole('ROLE_ADMIN')")
public class MaintCodeV1Api {
    @Autowired
    private MaintCodeListService maintCodeListService;
    @Autowired
    private MaintCodeScreenSettingService maintCodeScreenSettingService;
    @Autowired
    private MaintCodeSearchService maintCodeSearchService;
    @Autowired
    private MaintCodeBulkUpdateService maintCodeBulkUpdateService;
    @Autowired
    private MaintCodeBulkDeleteService maintCodeBulkDeleteService;
    @Autowired
    private SearchConditionComponent searchConditionComponent;

    /**
     * マスタメンテナンス用のコード一覧を取得する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping("/list")
    public GenericListMobel<MaintCodeListModel> list(@AuthenticationPrincipal final CustomLoginUser loginUser) {

        final ListServiceResponse<MaintCodeListModel> serviceResponse = maintCodeListService
                .call(ListServiceParameter.<MaintCodeListModel>builder().loginUser(loginUser).build());

        final GenericListMobel<MaintCodeListModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        return response;
    }

    /**
     * マスタメンテナンス用の画面構成情報を取得する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param tblid
     *            テーブル区分
     * @return {@link GenericItemModel} instance
     */
    @GetMapping("/{tblid}/screenSettings")
    public GenericItemModel<MaintCodeScreenSettingModel> screenSetting(@AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("tblid") final String tblid) {

        final GetServiceResponse<MaintCodeScreenSettingModel> serviceResponse = maintCodeScreenSettingService
                .call(GetServiceParameter.<MCodmstTblIdType>builder().loginUser(loginUser).id(MCodmstTblIdType.convertToType(tblid)).build());

        final GenericItemModel<MaintCodeScreenSettingModel> response = new GenericItemModel<>();
        response.setItem(serviceResponse.getItem());

        return response;

    }

    /**
     * マスタメンテナンス用のコード情報をリストで取得する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param tblid
     *            テーブル区分
     * @param searchCondition
     *            {@link MaintCodeSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/{tblid}/search")
    public GenericListMobel<Map<String, Object>> search(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("tblid") final String tblid,
            @RequestBody final MaintCodeSearchConditionModel searchCondition) {
        final MaintCodeSearchConditionModel localSearchCondition = searchConditionComponent.getSearchCondition(searchCondition,
                MaintCodeSearchConditionModel.class);

        final ListServiceResponse<Map<String, Object>> serviceResponse = maintCodeSearchService.call(MaintCodeSearchServiceParameter.builder()
                .loginUser(loginUser).tblId(MCodmstTblIdType.convertToType(tblid)).searchCondition(localSearchCondition).build());

        final GenericListMobel<Map<String, Object>> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setRevisionedAt(serviceResponse.getRevisionedAt());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(localSearchCondition, serviceResponse.isNextPage()));

        return response;
    }

    /**
     * マスタメンテナンス用のコード情報を一括更新する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param tblid
     *            テーブル区分
     * @param bulkUpdateModel
     *            {@link MaintCodeBulkUpdateModel} instance
     * @return {@link MaintCodeBulkResponseModel} instance
     */
    @PostMapping("/{tblid}/bulkUpdate")
    public MaintCodeBulkResponseModel bulkUpdate(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("tblid") final String tblid,
            @RequestBody final MaintCodeBulkUpdateModel bulkUpdateModel) {

        final GetServiceResponse<MaintCodeBulkResponseModel> serviceResponse = maintCodeBulkUpdateService.call(MaintCodeBulkUpdateServiceParameter.builder()
                .loginUser(loginUser).bulkUpdateModel(bulkUpdateModel).tblId(MCodmstTblIdType.convertToType(tblid)).build());

        return serviceResponse.getItem();
    }

    /**
     * マスタメンテナンス用のコード情報を一括削除する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param tblid
     *            テーブル区分
     * @param bulkUpdateModel
     *            {@link MaintCodeBulkUpdateModel} instance
     * @return {@link MaintCodeBulkResponseModel} instance
     */
    @PostMapping("/{tblid}/bulkDelete")
    public MaintCodeBulkResponseModel bulkDelete(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("tblid") final String tblid,
            @RequestBody final MaintCodeBulkUpdateModel bulkUpdateModel) {

        final GetServiceResponse<MaintCodeBulkResponseModel> serviceResponse = maintCodeBulkDeleteService.call(MaintCodeBulkUpdateServiceParameter.builder()
                .loginUser(loginUser).bulkUpdateModel(bulkUpdateModel).tblId(MCodmstTblIdType.convertToType(tblid)).build());

        return serviceResponse.getItem();
    }
}

//PRD_0137 #10669 add start
package jp.co.jun.edi.api.v1.maint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.maint.MaintSizeBulkResponseModel;
import jp.co.jun.edi.model.maint.MaintSizeBulkUpdateModel;
import jp.co.jun.edi.model.maint.MaintSizeListModel;
import jp.co.jun.edi.model.maint.MaintSizeSearchConditionModel;
import jp.co.jun.edi.model.maint.code.MaintCodeBulkResponseModel;
import jp.co.jun.edi.model.maint.code.MaintCodeBulkUpdateModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.maint.size.MaintSizeBulkDeleteService;
import jp.co.jun.edi.service.maint.size.MaintSizeBulkUpdateService;
import jp.co.jun.edi.service.maint.size.MaintSizeListService;
import jp.co.jun.edi.service.parameter.MaintSizeBulkUpdateServiceParameter;
import jp.co.jun.edi.service.parameter.MaintSizeSearchServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * マスタメンテナンス用のコード情報API.
 */
@RestController
@RequestMapping("/api/v1/maint/maint-size")
@PreAuthorize("hasRole('ROLE_JUN') and hasRole('ROLE_ADMIN')")
public class MaintSizeV1Api {
    @Autowired
    private MaintSizeListService maintSizeListService;
    @Autowired
    private MaintSizeBulkUpdateService maintSizeBulkUpdateService;
    @Autowired
    private MaintSizeBulkDeleteService maintSizeBulkDeleteService;
    @Autowired
    private SearchConditionComponent searchConditionComponent;

    /**
     * 品種コードに基づくサイズマスタ情報をリストで取得する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param hscd
     *            品種コード
     * @param searchCondition
     *            {@link MaintSizeSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/{hscd}/search")
    public GenericListMobel<MaintSizeListModel> search(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("hscd") final String hscd,
            @RequestBody final MaintSizeSearchConditionModel searchCondition) {
        final MaintSizeSearchConditionModel localSearchCondition = searchConditionComponent.getSearchCondition(searchCondition,
        		MaintSizeSearchConditionModel.class);

        final ListServiceResponse<MaintSizeListModel> serviceResponse = maintSizeListService.call(MaintSizeSearchServiceParameter.builder().loginUser(loginUser).hscd (hscd).build());

        final GenericListMobel<MaintSizeListModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setRevisionedAt(serviceResponse.getRevisionedAt());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(localSearchCondition, serviceResponse.isNextPage()));

        return response;
    }


    /**
     * 画面で選択されたサイズ情報を一括更新する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param bulkUpdateModel
     *            {@link MaintSizeBulkUpdateModel} instance
     * @return {@link MaintSizeBulkResponseModel} instance
     */
    @PostMapping("/bulkUpdate")
    public MaintSizeBulkResponseModel bulkUpdate(@AuthenticationPrincipal final CustomLoginUser loginUser, @RequestBody final MaintSizeBulkUpdateModel bulkUpdateModel) {

        final GetServiceResponse<MaintSizeBulkResponseModel> serviceResponse = maintSizeBulkUpdateService.call(MaintSizeBulkUpdateServiceParameter.builder()
                .loginUser(loginUser).bulkUpdateModel(bulkUpdateModel).copyFlg(false).build());

        return serviceResponse.getItem();
    }

    /**
     * 画面で選択されたサイズ情報を一括削除する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param tblid
     *            テーブル区分
     * @param bulkUpdateModel
     *            {@link MaintCodeBulkUpdateModel} instance
     * @return {@link MaintCodeBulkResponseModel} instance
     */
    @PostMapping("/bulkDelete")
    public MaintSizeBulkResponseModel bulkDelete(@AuthenticationPrincipal final CustomLoginUser loginUser,@RequestBody final MaintSizeBulkUpdateModel bulkUpdateModel) {

        final GetServiceResponse<MaintSizeBulkResponseModel> serviceResponse = maintSizeBulkDeleteService.call(MaintSizeBulkUpdateServiceParameter.builder()
                .loginUser(loginUser).bulkUpdateModel(bulkUpdateModel).build());

        return serviceResponse.getItem();
    }

    /**
     * 画面で選択されたサイズ情報をコピー新規する.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param bulkUpdateModel
     *            {@link MaintSizeBulkUpdateModel} instance
     * @return {@link MaintSizeBulkResponseModel} instance
     */
    @PostMapping("/bulkCopy")
    public MaintSizeBulkResponseModel bulkCopy(@AuthenticationPrincipal final CustomLoginUser loginUser, @RequestBody final MaintSizeBulkUpdateModel bulkUpdateModel) {

        final GetServiceResponse<MaintSizeBulkResponseModel> serviceResponse = maintSizeBulkUpdateService.call(MaintSizeBulkUpdateServiceParameter.builder()
                .loginUser(loginUser).bulkUpdateModel(bulkUpdateModel).copyFlg(true).build());

        return serviceResponse.getItem();
    }
}
//PRD_0137 #10669 add end
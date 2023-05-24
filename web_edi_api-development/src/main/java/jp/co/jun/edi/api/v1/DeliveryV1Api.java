package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.component.DeliveryStoreUploadCsvReadComponent;
import jp.co.jun.edi.model.DeliveryLocationModel;
//PRD_0123 #7054 add JFE start
import jp.co.jun.edi.model.DeliveryLocationSearchConditionModel;
//PRD_0123 #7054 add JFE end
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.DeliverySearchConditionModel;
import jp.co.jun.edi.model.DeliverySearchListConditionModel;
import jp.co.jun.edi.model.DeliverySearchResultModel;
import jp.co.jun.edi.model.DeliveryStoreUploadCsvModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.DeliveryApprovalService;
import jp.co.jun.edi.service.DeliveryCorrectService;
import jp.co.jun.edi.service.DeliveryCreateService;
import jp.co.jun.edi.service.DeliveryDeleteService;
import jp.co.jun.edi.service.DeliveryDirectConfirmService;
import jp.co.jun.edi.service.DeliveryGetService;
import jp.co.jun.edi.service.DeliveryListService;
import jp.co.jun.edi.service.DeliverySearchListService;
import jp.co.jun.edi.service.DeliveryStoreDeleteService;
import jp.co.jun.edi.service.DeliveryUpdateService;
//PRD_0123 #7054 add JFE start
import jp.co.jun.edi.service.MDeliveryLocationGetService;
//PRD_0123 #7054 add JFE end
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.parameter.CorrectServiceParameter;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;
//import jp.co.jun.edi.util.DataBaseSelectHolder;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品API.
 */
@RestController
@RequestMapping("/api/v1/deliveries")
@Slf4j
public class DeliveryV1Api {
    @Autowired
    private DeliveryCreateService createService;

    @Autowired
    private DeliveryGetService getService;

    @Autowired
    private DeliveryUpdateService updateService;

    @Autowired
    private DeliveryDeleteService deleteService;

    @Autowired
    private DeliveryStoreDeleteService deleteStoreService;

    @Autowired
    private DeliveryListService listService;

    @Autowired
    private DeliveryApprovalService approvalService;

    @Autowired
    private DeliveryCorrectService correctService;

    @Autowired
    private DeliveryDirectConfirmService directConfirmService;

    @Autowired
    private DeliverySearchListService listSearchService;
    // PRD_0123 #7054 add JFE start
    @Autowired
    private MDeliveryLocationGetService mDeliveryLocationGetService;
    // PRD_0123 #7054 add JFE end

    @Autowired
    private ObjectMapper objectMapper;

    // PRD_0031 add SIT start
    @Autowired
    private DeliveryStoreUploadCsvReadComponent deliveryStoreUploadCsvReadComponent;
    // PRD_0031 add SIT end

    /**
     * 納品情報を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link DeliveryModel} instance
     * @return {@link DeliveryModel} instance
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER') or hasRole('ROLE_DISTA')")
    public DeliveryModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final DeliveryModel item) {
        return createService.call(CreateServiceParameter.<DeliveryModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 納品情報を削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryId 納品ID
     */
    @DeleteMapping("/{deliveryId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER') or hasRole('ROLE_DISTA')")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId) {
        deleteService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(deliveryId).build());

        return;
    }

    /**
     * 納品得意先情報を削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryId 納品ID
     */
    @DeleteMapping("/store/{deliveryId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_DISTA')")
    public void deleteStore(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId) {
        deleteStoreService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(deliveryId).build());

        return;
    }

    /**
     * 納品情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryId 納品ID
     * @return {@link DeliveryModel} instance
     */
    @GetMapping("/{deliveryId}")
    public DeliveryModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(deliveryId).build()).getItem();
    }

    /**
     * 納品情報をリストで取得します.
     * ※過去納品数
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ItemSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<DeliveryModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final DeliverySearchConditionModel searchCondition) {
        final ListServiceResponse<DeliveryModel> serviceResponse = listService
                .call(ListServiceParameter.<DeliverySearchConditionModel>builder().loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<DeliveryModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }

    /**
     * (未承認)納品情報を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryId 納品ID
     * @param item {@link DeliveryModel} instance
     * @return {@link DeliveryModel} instance
     */
    @PutMapping("/{deliveryId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER') or hasRole('ROLE_DISTA')")
    public DeliveryModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId,
            @RequestBody @Validated(UpdateValidationGroup.class) final DeliveryModel item) {
        item.setId(deliveryId);

        return updateService.call(UpdateServiceParameter.<DeliveryModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 納品情報を承認します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryId 納品ID
     * @param item {@link DeliveryModel} instance
     */
    @PutMapping("/{deliveryId}/approval")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_DISTA')")
    public void approval(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId,
            @RequestBody @Validated(UpdateValidationGroup.class) final DeliveryModel item) {
        item.setId(deliveryId);

        approvalService.call(ApprovalServiceParameter.<DeliveryModel>builder().loginUser(loginUser).item(item).build());

        return;
    }

    /**
    * (承認済)納品情報を訂正します.
    *
    * @param loginUser {@link CustomLoginUser} instance
    * @param deliveryId 納品ID
    * @param item {@link DeliveryModel} instance
    * @return {@link DeliveryModel} instance
    */
    @PutMapping("/{deliveryId}/correct")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_DISTA')")
    public DeliveryModel correct(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId,
            @RequestBody @Validated(UpdateValidationGroup.class) final DeliveryModel item) {
        item.setId(deliveryId);

        return correctService.call(CorrectServiceParameter.<DeliveryModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
    * (承認済)納品情報を直送確定します.
    *
    * @param loginUser {@link CustomLoginUser} instance
    * @param deliveryId 納品ID
    * @param item {@link DeliveryModel} instance
    * @return {@link DeliveryModel} instance
    */
    @PutMapping("/{deliveryId}/directConfirm")
    @PreAuthorize("hasRole('ROLE_EDI')")
    public ApprovalServiceResponse directConfirm(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId,
            @RequestBody @Validated(UpdateValidationGroup.class) final DeliveryModel item) {
        item.setId(deliveryId);

        return directConfirmService.call(ApprovalServiceParameter.<DeliveryModel>builder().loginUser(loginUser).item(item).build());
    }

    /**
     * 納品依頼情報をリストで取得します.
     * ※配分一覧
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DeliverySearchListConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/searchList")
    public GenericListMobel<DeliverySearchResultModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final DeliverySearchListConditionModel searchCondition) {

        final DeliverySearchListConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<DeliverySearchResultModel> serviceResponse = listSearchService
                .call(ListServiceParameter.<DeliverySearchListConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<DeliverySearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * 配分一覧検索用のモデルを取得します.
     *
     * @param searchCondition {@link DeliverySearchListConditionModel} instance
     * @return {@link DeliverySearchListConditionModel} instance
     */
    private DeliverySearchListConditionModel getSearchCondition(
            final DeliverySearchListConditionModel searchCondition) {
        if (StringUtils.isEmpty(searchCondition.getPageToken())) {
            return searchCondition;
        }

        try {
            final DeliverySearchListConditionModel localSearchCondition = objectMapper.readValue(
                    Base64.getDecoder().decode(searchCondition.getPageToken()),
                    DeliverySearchListConditionModel.class);

            log.info(localSearchCondition.toString());

            return localSearchCondition;
        } catch (IOException e) {
            log.warn("IOException.", e);
        }
        return searchCondition;
    }

    /**
     * NextPageTokenを取得します.
     *
     * @param searchCondition {@link DeliverySearchListConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final DeliverySearchListConditionModel searchCondition,
            final boolean nextPage) {

        String nextPageToken = "";

        if (!nextPage) {
            return nextPageToken;
        }

        searchCondition.setPageToken(null);
        searchCondition.setPage(searchCondition.getPage() + 1);

        try {
            nextPageToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(searchCondition));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException.", e);
        }

        return nextPageToken;
    }

    // PRD_0031 add SIT start
    /**
     * 店舗配分アップロードファイルを読み込みます.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param file {@link MultipartFile} instance
     * @return {@link DeliveryStoreUploadModel} instance
     */
    @PostMapping("/deliveryStoreUploadCsvRead")
  //PRD_0120#8343 mod JFE start
//    public List<DeliveryStoreUploadCsvModel> deliveryStoreUploadCsvRead(
//            @AuthenticationPrincipal final CustomLoginUser loginUser,
//            @RequestParam("file") final MultipartFile file){
//    	final List<DeliveryStoreUploadCsvModel> model = deliveryStoreUploadCsvReadComponent.readCsvData(file);
    public DeliveryStoreUploadCsvModel deliveryStoreUploadCsvRead(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestParam("file") final MultipartFile file){
    	final DeliveryStoreUploadCsvModel model = deliveryStoreUploadCsvReadComponent.readCsvData(file);

        return model;
    }
  //PRD_0120#8343 mod JFE end
    // PRD_0031 add SIT end

    // PRD_0123 #7054 add JFE start
    /**
     * 品番情報のIDから納入場所マスタを取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DeliveryLocationSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping("/locationlist/{id}")
    public GenericListMobel<DeliveryLocationModel> list(
    		@AuthenticationPrincipal final CustomLoginUser loginUser,
    		@PathVariable("id") final BigInteger id) {

        final GetServiceResponse<List<DeliveryLocationModel>> serviceResponse = mDeliveryLocationGetService
                .call(GetServiceParameter.<BigInteger>builder().id(id).build());

        final GenericListMobel<DeliveryLocationModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItem());

        return response;
    }
    // PRD_0123 #7054 add JFE end
}

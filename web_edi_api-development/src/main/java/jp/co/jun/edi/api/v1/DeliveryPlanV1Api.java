package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.DeliveryPlanModel;
import jp.co.jun.edi.model.DeliveryPlanSearchConditionModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.DeliveryPlanCreateService;
import jp.co.jun.edi.service.DeliveryPlanGetService;
import jp.co.jun.edi.service.DeliveryPlanListService;
import jp.co.jun.edi.service.DeliveryPlanUpdateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 納品予定API.
 */
@RestController
@RequestMapping("/api/v1/deliveryPlans")
@Slf4j
public class DeliveryPlanV1Api {

    @Autowired
    private DeliveryPlanGetService getService;

    @Autowired
    private DeliveryPlanCreateService createService;

    @Autowired
    private DeliveryPlanUpdateService updateService;

    @Autowired
    private DeliveryPlanListService listService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 納品予定を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryPlanId 納品予定ID
     * @return {@link DeliveryPlanModel} instance
     */
    @GetMapping("/{deliveryPlanId}")
    public DeliveryPlanModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryPlanId") final BigInteger deliveryPlanId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(deliveryPlanId).build()).getItem();
    }

    /**
     * 納品予定を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link DeliveryPlanModel} instance
     * @return {@link DeliveryPlanModel} instance
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public DeliveryPlanModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final DeliveryPlanModel item) {
        return createService.call(CreateServiceParameter.<DeliveryPlanModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 納品予定を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryPlanId 納品予定ID
     * @param item {@link DeliveryPlanModel} instance
     * @return {@link DeliveryPlanModel} instance
     */
    @PutMapping("/{deliveryPlanId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public DeliveryPlanModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryPlanId") final BigInteger deliveryPlanId,
            @RequestBody @Validated(UpdateValidationGroup.class) final DeliveryPlanModel item) {
        item.setId(deliveryPlanId);

        return updateService.call(UpdateServiceParameter.<DeliveryPlanModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 納品情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DeliveryPlanSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<DeliveryPlanModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final DeliveryPlanSearchConditionModel searchCondition) {
        final ListServiceResponse<DeliveryPlanModel> serviceResponse = listService
                .call(ListServiceParameter.<DeliveryPlanSearchConditionModel>builder().loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<DeliveryPlanModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                getSearchCondition(searchCondition),
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * 納品予定検索モデルを取得します.
     *
     * @param searchCondition {@link DeliveryPlanSearchConditionModel} instance
     * @return {@link DeliveryPlanSearchConditionModel} instance
     */
    private DeliveryPlanSearchConditionModel getSearchCondition(
            final DeliveryPlanSearchConditionModel searchCondition) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final DeliveryPlanSearchConditionModel localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        DeliveryPlanSearchConditionModel.class);

                log.info(localSearchCondition.toString());

                return localSearchCondition;
            } catch (IOException e) {
                log.warn("IOException.", e);
            }
        }

        return searchCondition;
    }


    /**
     * NextPageTokenを取得します.
     *
     * @param searchCondition {@link DeliveryPlanSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final DeliveryPlanSearchConditionModel searchCondition,
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
}

package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;

import javax.validation.groups.Default;

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
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemSearchConditionModel;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.OrderSearchConditionModel;
import jp.co.jun.edi.model.VOrderModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.OrderApproveService;
import jp.co.jun.edi.service.OrderConfirmService;
import jp.co.jun.edi.service.OrderCreateService;
import jp.co.jun.edi.service.OrderDeleteService;
import jp.co.jun.edi.service.OrderGetService;
import jp.co.jun.edi.service.OrderListService;
import jp.co.jun.edi.service.OrderUpdateService;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注API.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1Api {
    @Autowired
    private OrderCreateService createService;

    @Autowired
    private OrderGetService getService;

    @Autowired
    private OrderUpdateService updateService;

    @Autowired
    private OrderDeleteService deleteService;

    @Autowired
    private OrderListService listService;

    @Autowired
    private OrderConfirmService confirmService;

    @Autowired
    private OrderApproveService approveService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 発注情報を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link OrderModel} instance
     * @return {@link OrderModel} instance
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public OrderModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({CreateValidationGroup.class, Default.class}) final OrderModel item) {
        return createService.call(CreateServiceParameter.<OrderModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 発注情報を削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param orderId 発注ID
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("orderId") final BigInteger orderId) {
        deleteService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(orderId).build());

        return;
    }

    /**
     * 発注情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param orderId 発注ID
     * @return {@link OrderModel} instance
     */
    @GetMapping("/{orderId}")
    public OrderModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("orderId") final BigInteger orderId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(orderId).build()).getItem();
    }

    /**
     * 発注情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ItemSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<VOrderModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final OrderSearchConditionModel searchCondition) {

        final OrderSearchConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<VOrderModel> serviceResponse = listService
                .call(ListServiceParameter.<OrderSearchConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<VOrderModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * 発注情報を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param orderId 発注ID
     * @param item {@link OrderModel} instance
     * @return {@link OrderModel} instance
     */
    @PutMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public OrderModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("orderId") final BigInteger orderId,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final OrderModel item) {
        item.setId(orderId);

        return updateService.call(UpdateServiceParameter.<OrderModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 発注情報を確定します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param orderId 発注ID
     * @param item {@link OrderModel} instance
     * @return {@link OrderModel} instance
     */
    @PutMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('ROLE_EDI')")
    public ApprovalServiceResponse confirm(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("orderId") final BigInteger orderId,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final OrderModel item) {
        item.setId(orderId);

        return confirmService.call(ApprovalServiceParameter.<OrderModel>builder().loginUser(loginUser).item(item).build());

    }

    /**
     * 発注情報を承認します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param orderId 発注ID
     * @param item {@link OrderModel} instance
     * @return {@link OrderModel} instance
     */
    @PutMapping("/{orderId}/approve")
    @PreAuthorize("hasRole('ROLE_EDI')")
      public ApprovalServiceResponse approve(
              @AuthenticationPrincipal final CustomLoginUser loginUser,
              @PathVariable("orderId") final BigInteger orderId,
              @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final OrderModel item) {
        item.setId(orderId);

        return approveService.call(ApprovalServiceParameter.<OrderModel>builder().loginUser(loginUser).item(item).build());
    }

    /**
     * モデルを取得します.
     *
     * @param searchCondition {@link ItemSearchConditionModel} instance
     * @return {@link ItemSearchConditionModel} instance
     */
    private OrderSearchConditionModel getSearchCondition(final OrderSearchConditionModel searchCondition) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final OrderSearchConditionModel localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        OrderSearchConditionModel.class);

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
     * @param searchCondition {@link OrderSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final OrderSearchConditionModel searchCondition,
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
